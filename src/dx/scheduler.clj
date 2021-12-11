(ns dx.scheduler
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dx scheduler. "}
  (:require [clojure.string :as string]))


(defonce mem (atom {}))

;; ....................................................................................................
;; all in, all out
;; ....................................................................................................
(defn flattenv [coll] (into [] (flatten coll))) 

(defn s-vec [s]
  "Turns the state vector `s` (a vector of vectors with keywords in
  it) into a vector of maps which can be analysed more easy."
  (flattenv
   (mapv
    (fn [v i]
      (mapv
       (fn [w j] {:idx i :jdx j :state w })
       v (range)))
    s (range))))

(defn up [mp-id struct states ctrls]
  (mapv
   (fn [ndx state ctrl]
     (let [a (agent {:states (s-vec state)
                     :ctrl   ctrl})]
       (swap! mem assoc-in [mp-id struct ndx] a)))
   (range) states ctrls))

(defn down [mp-id struct]
  (swap! mem update-in [mp-id] dissoc struct))

(defn state-agent [mp-id struct ndx] (get-in @mem [mp-id struct ndx]))

(defn update-state-fn [idx jdx kw]
  (fn [{i :idx j :jdx :as m}]
    (if (and (= idx i) (= jdx j))
      (assoc m :state kw)
      m)))

(defn all-pre-exec? [{idx :idx} v]
  (empty? (filterv (fn [{i :idx s :state}]
             (and (< i idx) (not= s :executed)))
           v)))
                   
(defn check-launch [{states :states :as m}]
  (if-let [next-ready (first (filterv (fn [{s :state}](= s :ready)) states))]
    (if (or (zero? (:idx next-ready))
            (all-pre-exec? next-ready states)) 
      (assoc m :launch next-ready)
      (dissoc m :launch))
    m))

(defn check-error
  "Checks for error states. Sets `:ctrl` interface to `:error` if any."
  [{states :states :as m}]
  (if (not-empty (filterv (fn [{s :state}](= s :error)) states))
    (assoc m :ctrl :error)
    m))

(defn state-fn 
  "Returns a function which should be used in the agents send-function." 
  [idx jdx kw]
  (fn [{states :states :as m}]
    (let [f (update-state-fn idx jdx kw)]
      (-> (assoc m :states (mapv f states))
          check-error
          check-launch))))

(defn set-state 
  "The `set-state` function should be used by the worker to set new
  states. This triggers the re-evaluation of the state map and starts
  next worker etc.
  
  Example:
  ```clojure
  (set-state :mpd-ref :cont 0 0 0 :working)
  ```"
  [mp-id struct ndx idx jdx kw]
  (let [a (state-agent mp-id struct ndx)
        f (state-fn idx jdx kw)]
    (send a f)
    (await a)
    ;; check launch and launch
    ))

(defn set-state-error [mp-id struct ndx idx jdx] (set-state mp-id struct ndx idx jdx :error))

(defn set-state-working [mp-id struct ndx idx jdx] (set-state mp-id struct ndx idx jdx :working))

(defn set-state-exec [mp-id struct ndx idx jdx] (set-state mp-id struct ndx idx jdx :executed))

(defn set-state-ready [mp-id struct ndx idx jdx] (set-state mp-id struct ndx idx jdx :ready))

(defn ctrl-fn 
  "Returns a function which should be used in the agents send-function." 
  [kw]
  (fn [m]
    (-> (assoc m :ctrl kw)
        check-error
        check-launch)))

(defn set-ctrl
  [mp-id struct ndx kw]
  (let [a (state-agent mp-id struct ndx)
        f (ctrl-fn  kw)]
    (send a f)
    (await a)
    ;; check launch and launch
    (prn a)
    ))
(comment

  (get-in @mem [:mpd-ref :cont 0])
  (send (cont-agent :mpd-ref 0) (fn [m] (assoc-in m [:state 0 0] :working)))
  (.getWatches (cont-agent :mpd-ref 0)))

