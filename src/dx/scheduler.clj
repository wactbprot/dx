(ns dx.scheduler
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dx scheduler. "}
  (:require [clojure.string :as string]))


(defonce mem (atom {}))

;; ....................................................................................................
;; all in, all out
;; ....................................................................................................

(defn dispatch  [k r old-value new-value]
  (prn ".....")
  (prn old-value)
  (prn new-value))

(defn up [mp-id struct states ctrls]
  (prn states)
  (mapv
   (fn [idx state ctrl]
     (let [a (agent {:state state :ctrl ctrl})]
       (add-watch a :worker  dispatch)
       (swap! mem assoc-in [mp-id struct idx] a)))
   (range) states ctrls))

(defn down [mp-id struct] (swap! mem update-in [mp-id] dissoc struct))

(defn cont-agent [mp-id no-idx] (get-in @mem [mp-id :cont no-idx]))

(comment

  (get-in @mem [:mpd-ref 0])
  (send (cont-agent :mpd-ref 0) (fn [m] (assoc-in m [:state 0 0] :working)))
  (.getWatches (cont-agent :mpd-ref 0)))
