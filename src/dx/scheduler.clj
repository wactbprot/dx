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
     (let [a (agent {:state state
                     :ctrl ctrl
                     :launch [nil nil]})]
       (swap! mem assoc-in [mp-id struct ndx] a)))
   (range) states ctrls))

(defn down [mp-id struct] (swap! mem update-in [mp-id] dissoc struct))

(defn state-agent [mp-id struct ndx] (get-in @mem [mp-id struct ndx]))

(defn state-fn [idx jdx kw]
  (fn [m]
    (assoc-in m [:state idx jdx] kw)
    
    ;; check state for error
    ;; update :ctrl if :error
    ;; check state for next worker start
    ;; ...
    ;;
    ;; update :ctrl
    ;; update :launch (worker)
    ;; 
    ))

(defn set-state [mp-id struct ndx idx jdx kw]
  (let [a (state-agent mp-id struct ndx)
        f (state-fn idx jdx kw)]
    (send a f)
    (await a)
    ;; check launch and launch
    ))
  

(comment

  (get-in @mem [:mpd-ref 0])
  (send (cont-agent :mpd-ref 0) (fn [m] (assoc-in m [:state 0 0] :working)))
  (.getWatches (cont-agent :mpd-ref 0)))

