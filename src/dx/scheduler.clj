(ns dx.scheduler
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dx scheduler. "}
  (:require [clojure.string :as string]))


(defonce mem (atom {}))

;; ....................................................................................................
;; all in, all out
;; ....................................................................................................
(defn flattenv [coll] (into [] (flatten coll))) 

(defn state-vec 
  "Turns the state vector `s` (a vector of vectors with keywords in
  it) into a vector of maps which can be analysed more easy.

  Example:
  ```clojure
  (s-vec [[:foo] [:bar :baz]])

  ;; =>
  ;; [{:idx 0, :jdx 0, :state :foo}
  ;; {:idx 1, :jdx 0, :state :bar}
  ;; {:idx 1, :jdx 1, :state :baz}]
  ```"
  [vv]
  (flattenv (mapv (fn [v i]
                    (mapv (fn [s j] {:idx i :jdx j :state s})
                          v (range)))
                  vv (range))))

(defn up 
  "Builds up the state and ctrl interface. For the mutating parts agents are used."
  [mp-id struct states ctrls]
  (mapv (fn [ndx state ctrl]
          (swap! mem assoc-in [mp-id struct ndx] (agent {:states (state-vec state)
                                                         :ctrl   ctrl})))
        (range) states ctrls))

(defn down 
  "Takes down the state and ctrl interface."
  [mp-id struct]
  (swap! mem update-in [mp-id] dissoc struct))

(defn interface-agent [mp-id struct ndx] (get-in @mem [mp-id struct ndx]))

(defn update-state-fn [{:keys [idx jdx state]}]
  (fn [{i :idx j :jdx :as m}]
    (if (and (= idx i) (= jdx j))
      (assoc m :state state)
      m)))

;; ....................................................................................................
;; runtime tests
;; ....................................................................................................
(defn ctrl-go? [{ctrl :ctrl}]
  (and (some (partial = ctrl) [:run :cycle])
       (not= ctrl :error)))

(defn launch? [{{i :idx j :jdx} :launch}] (and (int? i) (int? j)))

(defn all-pre-exec? [{idx :idx} v]
  (or (zero? idx)
      (empty? (filterv
               (fn [{i :idx s :state}] (and (< i idx) (not= s :executed)))
               v))))

;; ....................................................................................................
;; dispatch
;; ....................................................................................................
(defn dispatch
  ([m] (dispatch m prn))
  ([m f] (when (and (ctrl-go? m) (launch? m)) (f (:launch m)))))

;; ....................................................................................................
;; interfacs update funs
;; ....................................................................................................
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

;; ....................................................................................................
;; state
;; ....................................................................................................
(defn state-fn 
  "Returns a function which should be used as the agents send-function." 
  [idx jdx kw]
  (fn [{states :states :as m}]
    (let [f (update-state-fn {:idx idx :jdx jdx :state kw})]
      (-> (assoc m :states (mapv f states))
          ->error
          ->end
          ->launch))))
 
(defn set-state 
  "The `set-state` function should be used by the worker to set new
  states.  This re-evaluats the interface by means
  of [[set-state]]. Afterwards, the function waits for concurrent
  modifications of other threads by `(await a)` followed by the call
  to the dispatch function.
  
  Example:
  ```clojure
  (set-state :mpd-ref :cont 0 0 0 :working)
  ```"
  [mp-id struct ndx idx jdx kw]
  (let [a (interface-agent mp-id struct ndx)]
    (send a (state-fn idx jdx kw))
    (await a)
    (dispatch (deref a))))

;; ....................................................................................................
;; ctrl
;; ....................................................................................................
(defn ctrl-fn 
  "Returns a function which should be used as the agents send-function." 
  [kw]
  (fn [m]
    (-> (assoc m :ctrl kw)
        ->error
        ->end
        ->launch)))

(defn set-ctrl
  "The `set-ctrl` function should be used by the user to trigger actions.
  This re-evaluats the interface by means of [[set-ctrl]]. Afterwards,
  the function waits for concurrent modifications of other threads
  by `(await a)` followed by the call to the dispatch function.
  
  Example:
  ```clojure
  (set-ctrl :mpd-ref :cont 0 :run)
  ```"
  [mp-id struct ndx kw]
  (let [a (interface-agent mp-id struct ndx)]
    (send a (ctrl-fn kw))
    (await a)
    (dispatch (deref a))))

(comment

  (get-in @mem [:mpd-ref :cont 0])
  (send (cont-agent :mpd-ref 0) (fn [m] (assoc-in m [:state 0 0] :working)))
)

