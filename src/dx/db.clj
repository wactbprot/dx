(ns dx.db
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Basic database interop. Plain HTTP powerd by httpkit."}
  (:require [cheshire.core :as che]
            [clojure.string :as string]
            [org.httpkit.client :as http]))

;; ................................................................................
;; utils
;; ................................................................................
(defn safe [{r :at-replace} s]
  (string/replace s #"@([a-zA-Z])" (str r "$1")))

(defn db-base-url [{:keys [db-prot db-srv db-port]}]
  (str db-prot  "://" db-srv ":" db-port))

(defn db-url [{db-name :db-name :as conf}]
  (str (db-base-url conf) "/" db-name))

(defn doc-url [{rev :rev :as conf} id]
  (when (and (db-url conf) id)
    (str db-url "/" id (when rev (str "?rev=" rev)))))

(defn view-url [{:keys [db-design view db-view view-key] :as conf}]
  (when (and db-design db-view)
    (str (db-url conf) "/_design/" db-design "/_view/" db-view
         (when view-key (str "?key=%22" view-key "%22" )))))

(defn result [conf {body :body}]
  (try (che/parse-string-strict (safe conf body) true )
       (catch Exception e {:error (.getMessage e)})))

(defn get-rev [{opt :db-opt :as conf} id]
  (let [res @(http/head (doc-url conf id) opt)]
    (when (< (:status res) 400)
      (string/replace (get-in  res [:headers :etag]) #"\"" ""))))

;; ................................................................................
;; crud ops
;; ................................................................................
(defn get-doc [{opt :db-opt :as conf} id]
  (result conf @(http/get (doc-url conf id) opt)))

(defn del-doc [{opt :db-opt :as conf} id]
  (result conf @(http/delete (doc-url (assoc conf :rev (get-rev conf id)) id) opt)))

(defn put-doc [{opt :db-opt :as conf} {id :_id :as doc}]
  (result conf @(http/put (doc-url conf id) (assoc opt :body (che/encode doc)))))

;; ................................................................................
;; view
;; ................................................................................
(defn get-view [{opt :db-opt :as conf}]
  (:rows (result conf @(http/get (view-url conf) opt))))
