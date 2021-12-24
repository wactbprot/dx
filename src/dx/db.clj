(ns dx.db
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Basic database interop. Plain HTTP powerd by httpkit."}
  (:require [cheshire.core :as che]
            [clojure.string :as string]
            [dx.config :as c]
            [org.httpkit.client :as http]))
;; ................................................................................
;; utils
;; ................................................................................
(defn doc-url [{db-url :db-url rev :rev} id]
  (when (and db-url id) (str db-url "/" id (when rev (str "?rev=" rev)))))

(defn view-url [{db-url :db-url design :db-design view :db-view}]
  (when (and db-url design view) (str db-url "/_design/" design "/_view/" view )))

(defn result [{body :body status :status}]
  (let [body (try (che/parse-string-strict body true )
               (catch Exception e {:error (.getMessage e)}))]
    (if (< status 400)
      body
      {:error (:error body) :reason (:reason body)})))

(defn get-rev [{opt :db-opt :as conf} id]
  (let [res @(http/head (doc-url conf id) opt)]
    (when (< (:status res) 400)
      (string/replace (get-in  res [:headers :etag]) #"\"" ""))))

;; ................................................................................
;; crud ops
;; ................................................................................
(defn get-doc [{opt :db-opt :as conf} id]
  (result @(http/get (doc-url conf id) opt)))

(defn del-doc [{opt :db-opt :as conf} id]
  (result @(http/delete (doc-url (assoc conf :rev (get-rev conf id)) id) opt)))

(defn put-doc [{opt :db-opt :as conf} {id :_id :as doc}]
  (result @(http/put (doc-url conf id) (assoc opt :body (che/encode doc)))))

;; ................................................................................
;; view
;; ................................................................................
(defn get-view [{opt :db-opt :as conf}]
  (:rows (result @(http/get (view-url conf) opt))))
