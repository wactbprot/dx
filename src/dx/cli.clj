(ns dx.cli
    ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
      :doc "The dx command line interface. "}
  (:require [dx.model :as model]
            [dx.scheduler :as scheduler]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn get-ref []
  (->  (io/resource "mpd-ref.edn")
       slurp
       edn/read-string
       mem/up))

(defn up [{mp-id :_id mp :Mp}]
  (model/up mp-id mp)
  (scheduler/up mp-id :cont (mem/cont-states mp-id) (mem/cont-ctrls mp-id))
  (scheduler/up mp-id :defi (mem/defi-states mp-id) (mem/defi-ctrls mp-id))) 

(defn down [mp-id]
  (scheduler/down mp-id :cont)
  (scheduler/down mp-id :defi)
  (model/down mp-id))

(comment
  (up (get-ref)))
