(ns dx.scheduler
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dx scheduler. "}
  (:require [clojure.string :as string]
            [dx.worker :as w]))


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

(defn struct->future-struct [s] (keyword (str (name s) "-future")))

(defn interface-agent [mem {:keys [mp-id struct ndx]}]
  (get-in @mem [mp-id :State struct ndx]))

(defn iterface-future [mem {:keys [mp-id struct ndx]}]
  (get-in @mem [mp-id :State (struct->future-struct struct) ndx]))

(defn update-state-fn [{:keys [idx jdx state]}]
  (fn [{i :idx j :jdx :as m}]
    (if (and (= idx i) (= jdx j))
      (assoc m :state state)
      m)))

;; ................................................................................
;; runtime tests
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
  "Checks for positions to launch next and updates the interface.
  New in dx: if a `next-ready` is found: the state of it is already
  set here to `:working`."
  [{states :states :as m}]
  (if-let [next-ready (first (filterv (fn [{s :state}] (= s :ready)) states))]
    (if (all-pre-exec? next-ready states)
      (let [f (update-state-fn (assoc next-ready :state :working))]
        (assoc m
               :states (mapv f states)
               :launch next-ready))
      (dissoc m :launch))
    (dissoc m :launch)))

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

(defn state [mem m] (send (interface-agent mem m) (state-fn m)))

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

(defn ctrl [mem m] (send (interface-agent mem m) (ctrl-fn m)))

(defn observe [a f]
  (loop []
    (await a)
    (when-let [m (:launch @a)]
      (send a dissoc :launch)
      (f m))
    (when-not  (Thread/interrupted)
      (Thread/sleep 1000)
      (recur))))

(defn up 
  "Builds up the `ndx` `struct`ures interface. For the mutating parts,
  agents are used. The structures runs in `futures` stored "
  [mem {:keys [mp-id struct]} states f]
  (mapv (fn [ndx state]
          (let [a (agent {:states (state-vec {:mp-id  mp-id
                                              :struct struct
                                              :ndx    ndx} state)
                          :ctrl :ready})]
            (swap! mem assoc-in [mp-id :State struct ndx] a)
            (swap! mem assoc-in [mp-id :State (struct->future-struct struct) ndx] (future (observe a f)))) 
        (range) states)))

(defn down 
  "Takes down the state and ctrl interface of `struct`ure."
  [mem {:keys [mp-id struct]}]
  (let [f-struct (struct->future-struct struct)]
    (mapv (fn [[_ f]] (future-cancel f)) (get-in @mem [mp-id f-struct]))
    (swap! mem update-in [mp-id] dissoc :State f-struct)
    (swap! mem update-in [mp-id] dissoc :State struct)))


