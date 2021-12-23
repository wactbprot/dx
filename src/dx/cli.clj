(ns dx.cli
    ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
      :doc "The dx command line interface. "}
  (:require [dx.exch :as exch]
            [dx.model :as model]
            [dx.scheduler :as s]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn get-ref-mpd []
  (->  (io/resource "mpd-ref.edn")
       slurp
       edn/read-string))

(defn up [{mp-id :_id mp :Mp}]
  (let [mp-id (keyword mp-id)]
    (model/up mp-id mp)
    (s/up mp-id :cont (model/cont-states mp-id) (model/cont-ctrls mp-id))
    (s/up mp-id :defi (model/defi-states mp-id) (model/defi-ctrls mp-id))
    (exch/up mp-id (model/exch mp-id))))

(defn down [mp-id]
  (s/down mp-id :cont)
  (s/down mp-id :defi)
  (exch/down mp-id)
  (model/down mp-id))

(defn stop [mp-id ndx]
  (s/ctrl! {:mp-id mp-id
            :struct :cont
            :ndx ndx
            :ctrl :stop}))

(defn run [mp-id ndx]
  (s/ctrl! {:mp-id mp-id
            :struct :cont
            :ndx ndx
            :ctrl :run}))

(comment
  (up (get-ref-mpd))
  (run :mpd-ref 0))
