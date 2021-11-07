(ns dxrt.scheduler
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dxrt scheduler. "}
  (:require [dxrt.mem :as mem]))

(defonce ctrls (atom {}))
(defonce states (atom {}))

;; ....................................................................................................
;; all in, all out
;; ....................................................................................................
(defn template [a mp-id idx value] (swap! a assoc-in [mp-id idx] (agent value)))

(defn up [mp-id]
  (mapv #(template states mp-id %1 %2) (range) (mem/cont-states mp-id))
  (mapv #(template ctrls  mp-id %1 %2) (range) (mem/cont-ctrls mp-id)))
