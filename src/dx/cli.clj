(ns dx.cli
    ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
      :doc "The dx command line interface. "}
  (:require [dx.exch :as exch]
            [dx.model :as model]
            [dx.scheduler :as scheduler]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn get-ref-mpd []
  (->  (io/resource "mpd-ref.edn")
       slurp
       edn/read-string))

(defn up [{mp-id :_id mp :Mp}]
  (let [mp-id (keyword mp-id)]
    (model/up mp-id mp)
    (scheduler/up mp-id :cont (model/cont-states mp-id) (model/cont-ctrls mp-id))
    (scheduler/up mp-id :defi (model/defi-states mp-id) (model/defi-ctrls mp-id))
    (exch/up mp-id (model/exch mp-id))))

(defn down [mp-id]
  (scheduler/down mp-id :cont)
  (scheduler/down mp-id :defi)
  (exch/down mp-id)
  (model/down mp-id))

(defn run [mp-id ndx] (scheduler/set-ctrl mp-id :cont ndx :run))

(defn stop [mp-id ndx] (scheduler/set-ctrl mp-id :cont ndx :stop))

(comment
  (up (get-ref-mpd))
  (run :mpd-ref 0))
