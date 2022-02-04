(ns dx.scheduler
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dx scheduler. "}
  (:require [clojure.string :as string]))



;; ................................................................................
;; all in, all out
;; ................................................................................
(defn flattenv [coll] (into [] (flatten coll))) 

(defn state-vec 
  "Turns the state vector `s` (a vector of vectors with keywords in
  it) into a vector of maps which can be analysed more easy.

  Example:
  ```clojure
  (state-vec {:mp-id :mpe-ref} [[:foo] [:bar :baz]])

  ;; =>
  ;; [{:mp-id :mpe-ref :idx 0 :jdx 0, :state :foo}
  ;;  {:mp-id :mpe-ref :idx 1 :jdx 0, :state :bar}
  ;;  {:mp-id :mpe-ref :idx 1 :jdx 1, :state :baz}]
  ```"
  [m vv]
  (flattenv (mapv (fn [v i]
                    (mapv (fn [s j] (merge m {:idx i :jdx j :state s}))
                          v (range)))
                  vv (range))))

(defn get-agent [mem {:keys [mp-id struct ndx]}]
  (get-in mem [mp-id struct ndx :State]))

(defn add-agent [mem {:keys [mp-id struct ndx]} a]
  (assoc-in mem [mp-id struct ndx :State] a))

(defn update-state-fn [{:keys [idx jdx state]}]
  (fn [{i :idx j :jdx :as m}]
    (if (and (= idx i) (= jdx j))
      (assoc m :state state)
      m)))


;; ................................................................................
;; state tests
;; ................................................................................
(defn ctrl-go? [{ctrl :ctrl}]
  (and (some (partial = ctrl) [:run :cycle])
       (not= ctrl :error)))

(defn launch? [{{i :idx j :jdx} :launch}] (and (int? i) (int? j)))

(defn all-pre-exec? [{idx :idx} v]
  (or (zero? idx)
      (empty? (filterv
               (fn [{i :idx s :state}] (and (< i idx) (not= s :executed)))
               v))))

;; ................................................................................
;; interfacs update funs
;; ................................................................................
(defn ->launch
  "If `ctrl` is `:run` or `:cycle: checks for positions to launch next
  and updates the interface. New in dx: if a `next-ready` is found:
  the state of it is already set here to `:working`."
  
  [{states :states ctrl :ctrl :as m}]
  (if (or (= ctrl :run) (= ctrl :cycle))

    (if-let [next-ready (first (filterv (fn [{s :state}] (= s :ready)) states))]
      (if (all-pre-exec? next-ready states)
        (let [f (update-state-fn (assoc next-ready :state :working))]
          (assoc m
                 :states (mapv f states)
                 :launch next-ready))
        (dissoc m :launch))
      (dissoc m :launch))
    m))

(defn ->error
  "Checks for error states. Sets `:ctrl` interface to `:error` if any."
  [{states :states :as m}]
  (if (not-empty (filterv (fn [{s :state}] (= s :error)) states))
    (assoc m :ctrl :error)
    m))

(defn ->end
  "Checks if all states are executed. Sets all states to :ready if so.
  Sets `:ctrl` interface to `:ready` if it was `:run` if any."
  [{states :states ctrl :ctrl :as m}]
  (if (= (count states)
         (count (filterv (fn [{s :state}] (= s :executed)) states)))
    (assoc m
           :states (mapv #(assoc % :state :ready) states)
           :ctrl (if (= ctrl :run) :ready ctrl))
    m))

;; ................................................................................
;; state
;; ................................................................................
(defn state-fn 
  "Returns a function which should be used as the agents send-function." 
  [{:keys [idx jdx state]}]
  (fn [{states :states :as m}]
    (let [f (update-state-fn {:idx idx :jdx jdx :state state})]
      (-> (assoc m :states (mapv f states))
          ->error
          ->end
          ->launch))))

(defn state [mem m] (send (get-agent mem m) (state-fn m)))


;; ................................................................................
;; ctrl
;; ................................................................................
(defn ctrl-fn 
  "Returns a function which should be used as the agents send-function." 
  [{kw :ctrl}]
  (fn [m]
    (-> (assoc m :ctrl kw)
        ->error
        ->end
        ->launch)))

(defn ctrl [mem m] (send (get-agent mem m) (ctrl-fn m)))


;; ................................................................................
;; observer f
;; ................................................................................
(defn observe
  "Observe function loops when thread is not interupted. Invokes the
  `launch-fn` when `:launch` is not `nil`. If so, `:launch` is removed
  followed by the invocation of `->launch` in order to start tasks in
  parallel. The latter means: it takes at least `:heartbeat`msec for
  the next task is launched."
  [{h :heartbeat :or {h 1000 }} a launch-fn]
  (loop []
    (when-not (Thread/interrupted)
      (await a)
      (when-let [l (:launch @a)]
        (launch-fn l)
        (send a (fn [m]
                  (dissoc m :launch)   
                  (->launch m))))
      (Thread/sleep h)
      (recur))))



;; ................................................................................
;; up
;; ................................................................................

(defn ->observer [mem {:keys [mp-id struct ndx] :as m} f]
  (let [p [mp-id struct ndx :Observer]
        o (get-in mem p)]
    (when (future? o) (future-cancel o))
    (assoc-in mem p (future (observe (:conf mem) (get-agent mem m) f)))))

(defn up 
  "Builds up the `ndx` `struct`ures interface. For the mutating parts (the states),
  agents are used (see [[add-agent]]. The agents are observed by
  the [[observe]] function. The observer executes the launch function
  `f` on the next state to start. All is assoced to `mem` 

  Example:
  ```clojure
  ;; states looks like this:
  [[[:ready :ready :ready :ready]]
   [[:ready :ready]]]
  
  ;; m carries the position:
  {:mp-id :mpd-ref :struct :Container}
  ```"
  [mem m states f]
  (reduce-kv (fn [res ndx state]
               (let [m (assoc m :ndx ndx)
                     a (agent {:states (state-vec m state) :ctrl :ready})]
                 (-> res
                     (add-agent m a)
                     (->observer m f))))
             mem states))


;; ................................................................................
;; down
;; ................................................................................
(defn down 
  "Takes down the state and ctrl interface of `struct`ure."
  [mem {:keys [mp-id struct]}]
  (mapv (fn [{f :Observer}] (future-cancel f)) (get-in mem [mp-id struct]))
  (update-in mem [mp-id] dissoc struct))


