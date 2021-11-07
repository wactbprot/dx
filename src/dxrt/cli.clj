(ns dxrt.cli
    ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
      :doc "The dxrt command line interface. "}
  (:require [dxrt.mem :as mem]
            [dxrt.scheduler :as scheduler]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn mpd-ref-ini []
  (->  (io/resource "mpd-ref.edn")
       slurp
       edn/read-string
       mem/up))
