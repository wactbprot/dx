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
  and updates the interface."
  
  [{states :states ctrl :ctrl :as m}]
  (if (or (= ctrl :run) (= ctrl :cycle))
    (if-let [next-ready (first (filterv (fn [{s :state}] (= s :ready)) states))]
      (if (all-pre-exec? next-ready states)
        (assoc m :launch next-ready)
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
;; agent(s)
;; ................................................................................
(defn get-agents [mem {:keys [mp-id struct]}]
  (get-in mem [mp-id struct]))

(defn get-agent [mem {:keys [ndx] :as m}]
  (get (get-agents mem m) ndx))

(defn add-agent [mem {:keys [mp-id struct ndx]} a]
  (assoc-in mem [mp-id struct ndx] a))

;; ................................................................................
;; state
;; ................................................................................
(defn state-fn 
  "Returns a function which should be used as the agents send-function." 
  [{:keys [idx jdx state]} launch!]
  (fn [{states :states :as m}]
    (let [f (update-state-fn {:idx idx :jdx jdx :state state})]
      (-> (assoc m :states (mapv f states))
          ->error
          ->end
          ->launch
          launch!))))

(defn state [mem m launch!] (send (get-agent mem m) (state-fn m launch!)))


;; ................................................................................
;; ctrl
;; ................................................................................
(defn ctrl-fn 
  "Returns a function which should be used as the agents send-function." 
  [{kw :ctrl} launch!]
  (fn [m]
    (-> (assoc m :ctrl kw)
        ->error
        ->end
        ->launch
        launch!)))

(defn ctrl [mem m launch!] (send (get-agent mem m) (ctrl-fn m launch!)))



;; ................................................................................
;; up
;; ................................................................................
(defn state-up 
  "Builds up the `ndx` `struct`ure state interface for the mutating parts.
  Agents are used (see [[add-agent]]. 

  Example:
  ```clojure
  ;; states looks like this:
  (def s [[[:ready :ready]]
   [[:ready]]])
  
  ;; m carries the position:
  (def m {:mp-id :a :struct :b})

  ;; call up returns:
  (state-up m s)
  {:a
  {:b
  {0 #<Agent@149953c4: 
     {:states
      [{:mp-id :a, :struct :b, :ndx 0, :idx 0, :jdx 0, :state :ready}
       {:mp-id :a,
        :struct :b,
        :ndx 0,
        :idx 0,
        :jdx 1,
        :state :ready}],
      :ctrl :ready}>,
   1 #<Agent@3c3eb708: 
     {:states
      [{:mp-id :a,
        :struct :b,
        :ndx 1,
        :idx 0,
        :jdx 0,
        :state :ready}],
      :ctrl :ready}>}}}
  ```"
  [m states]
  (reduce-kv (fn [res ndx state]
               (let [m (assoc m :ndx ndx)
                     a (agent {:states (state-vec m state)
                               :ctrl :ready})]
                 (add-agent res m a)))
             {} states))

;; ................................................................................
;; down
;; ................................................................................
(defn down 
  "Takes down the state and ctrl interface of `struct`ure.
  TODO: rewrite
  "
  [mem {:keys [mp-id struct]}]
  (mapv (fn [{f :Observer}] (future-cancel f)) (get-in mem [mp-id struct]))
  (update-in mem [mp-id] dissoc struct))


