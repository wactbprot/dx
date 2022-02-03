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
  (ig/init {::mem {}
          ::log {:mulog {:type :multi
                         :publishers[{:type :console :pretty? true}
                                     {:type :elasticsearch
                                      :url "http://a75438:9200/"
                                      :els-version  :v7.x
                                      :publish-delay 1000
                                      :data-stream  "vl-log-stream"
                                      :name-mangling false}]}}}))

(defmethod ig/init-key ::mem [_ mem] mem)
          
;; entire mem initialized with conf
;; mem should become a simple map
;; and id/init ed
(defonce mem (atom {:conf c/conf})) 

(defn get-ref-mpd []
  (->  (io/resource "mpd-ref.edn")
       slurp
       edn/read-string))

(comment
  (up (get-ref-mpd))
  (m/pre-task mem {:mp-id :mpd-ref
                   :struct :Container
                   :ndx 0
                   :idx 0
                   :jdx 0})
  
  (get-task "Common-wait" (:conf @mem)))

(defn up [mpd] (dx/up mem mpd))

(defn down [mp-id] (dx/down mem mp-id))

(defn stop [mp-id ndx]
  (s/ctrl mem  {:mp-id mp-id
                :struct :Container
                :ndx ndx
                :ctrl :stop}))

(defn run [mp-id ndx]
  (s/ctrl mem {:mp-id mp-id
               :struct :Container
               :ndx ndx
               :ctrl :run}))

(defn set-cont-state [mp-id ndx idx jdx state]
  (s/state mem {:mp-id mp-id
                :struct :Container
                :ndx ndx
                :idx idx
                :jdx jdx
                :state state}))

(defn replace-launch-fns 
  "Enables the replacement of the periodically invoked launch function
  `f`. `f` is the function which is called to launch new resp. next
  tasks.
  
  Example:
  ```clojure
  (replace-future :mpd-nn-generic 0 prn)
  ;; =>
  ;; prints
  ;; {:mp-id :mpd-nn-generic
  ;; :struct :Container
  ;;  ...
  ;; }
  ```"
  [mp-id ndx f]
  (let [m {:mp-id mp-id :struct :Container :ndx ndx}]
    (dx/replace-launch-fns mem m f)))

(comment
  ;; generate a fresh mpd
  (up  (-> (mpd/base)
           mpd/descr->
           mpd/exch->
           mpd/cont->
           mpd/defi->))
  
  (set-cont-state :mpd-nn-generic 0 0 0 :executed)
  
  ;; or
  (up (get-ref-mpd))
  (run :mpd-ref 0)
  (set-cont-state :mpd-ref 0 0 0 :executed)
  (down :mpd-ref))
