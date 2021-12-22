(ns dx.config
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Provides configuration data. Reads and assoc env vars."}
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn get-config
  "Reads a `edn` configuration in file `f`." 
  ([] (get-config (io/resource "config.edn")))
  ([f] (-> f slurp edn/read-string)))

(defn global-log-context [{app-name :app-name}]
  {:facility (System/getenv "DX_FACILITY")
   :app-name app-name})

(defn db-base-url [{:keys [db-prot db-srv db-port]}]
  (let [usr (System/getenv "CAL_USR")
        pwd (System/getenv "CAL_PWD")]
    (str db-prot  "://"
         (when (and usr pwd) (str usr ":" pwd "@"))
         db-srv ":" db-port)))

(defn db-conn [{db-name :db-name :as c}] (str (db-base-url c) "/"  db-name ))

(def config
  (let [c (get-config)]
    (assoc c
           :global-log-context (global-log-context c)
           :db-base-url (db-base-url c)
           :db-conn (db-conn c))))
