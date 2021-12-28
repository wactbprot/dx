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


(defn db-base-url [{:keys [db-prot db-srv db-port db-usr db-pwd]}]
  (str db-prot  "://"
       (when (and db-usr db-pwd) (str db-usr ":" db-pwd "@"))
       db-srv ":" db-port))

(defn db-conn [{db-name :db-name :as c}]
  (str (db-base-url c) "/"  db-name ))

(defn db-url [{db-name :db-name :as c}]
  (str (db-base-url c) "/" db-name))

(def conf
  (let [defaults (assoc (get-config)
                        :db-usr (System/getenv "CAL_USR")
                        :db-pwd (System/getenv "CAL_PWD"))] 
    (assoc defaults
           :db-base-url (db-base-url defaults)
           :db-url (db-url defaults)
           :db-opt {:headers {"Content-Type" "application/json"}
                    :timeout 2000
                    :basic-auth [(:db-usr defaults) (:db-pwd defaults)]})))
  
