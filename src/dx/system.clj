(ns dx.system
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dx command line interface."}
  (:require [dx.core :as dx]
            [dx.db :as db]
            [dx.mpd :as mpd]
            [dx.config :as c]
            [dx.scheduler :as s]
            [integrant.core :as ig]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [portal.api :as p]))

(comment
  (def p (p/open))
  (add-tap #'p/submit))

(comment
  (def config
    (ig/read-string (slurp "config.edn"))))

(comment
  (ig/init {::mem {}}))

(defmethod ig/init-key ::mem [_ mem] mem)

(defn get-ref-mpd []
  (->  (io/resource "mpd-ref.edn")
       slurp
       edn/read-string))

(comment
  (def mem (dx/up {} (get-ref-mpd)))
