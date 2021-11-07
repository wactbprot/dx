(ns dxrt.cli
    ^{:author "Thomas Bock wactbprot@gmail.com"
      :doc "dxrt command line interface. "}
  (:require [dxrt.mem :as mem]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn mpd-ref-ini [] (->  (io/resource "mpd-ref.edn")
                         slurp
                         edn/read-string
                         mem/add-mpd))
