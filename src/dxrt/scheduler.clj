(ns dxrt.scheduler
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dxrt scheduler. "}
  (:require [clojure.string :as string]))



(def mem (atom {}))

;; ....................................................................................................
;; all in, all out
;; ....................................................................................................

(defn dispatch  [ctx k r old-value new-value]
  (prn ".....")
  (prn ctx)
  (prn old-value)
  (prn new-value))
               

(defn up [mp-id states ctrls]
  (mapv
   (fn [idx state ctrl]
     (let [a (agent {:state state :ctrl ctrl})]
       (add-watch a :worker  dispatch)
       (swap! mem assoc-in [mp-id idx] a)))
   (range) states ctrls))

(comment
  ;in cli ns
  (get-in scheduler/mem [:mpd-ref 0]))
