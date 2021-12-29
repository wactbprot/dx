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

(def conf
  (let [c (get-config)] 
    (assoc c :db-opt {:headers {"Content-Type" "application/json"}
                      :timeout 2000
                      :basic-auth [(System/getenv "CAL_USR")
                                   (System/getenv "CAL_PWD")]})))

