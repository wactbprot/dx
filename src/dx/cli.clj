(ns dx.cli
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dx command line interface."}
  (:require [dx.db :as db]
            [dx.config :as c]
            [dx.exch :as e]
            [dx.model :as m]
            [dx.mpd :as mpd]
            [dx.task :as t]
            [dx.scheduler :as s]
            [clojure.edn :as edn]
            [portal.api :as p]
            [clojure.java.io :as io]))
(comment
  (def p (p/open))
  (add-tap #'p/submit))

;; entire mem initialized with conf
(defonce mem (atom {:conf c/conf})) 

(comment
  (swap! mem assoc :conf c/conf))

(defn get-ref-mpd []
  (->  (io/resource "mpd-ref.edn")
       slurp
       edn/read-string))

;; ................................................................................
;; exec workflow
;; ................................................................................
;;; todo: how to end exec thread (e.g. if (map? db-task) gives false
;;; --> return value of s/state!?
;;;
(defn get-task [task-name conf]
  (-> task-name
      (t/task-conf conf)
      (db/get-view) 
      first
      :value))

(comment
  (up (get-ref-mpd))
  (m/pre-task mem {:mp-id :mpd-ref
                   :struct :Container
                   :ndx 0
                   :idx 0
                   :jdx 0})
  
  (get-task "Common-wait" (:conf @mem)))

(defn build-task [mem m]
  (let [conf           (:conf @mem)
        {task-name     :TaskName
         use-map       :Use
         replace-map   :Replace} (m/pre-task mem m)
        {defaults-map  :Defaults
         from-exch-map :FromExchange
         :as db-task} (get-task task-name conf)]
    (if (map? db-task)
      (merge
       (t/assemble {:Task (dissoc db-task :Defaults :Use :Replace) 
                    :Replace replace-map
                    :Use use-map
                    :Defaults defaults-map
                          :FromExchange (e/from mem m from-exch-map)
                    :Globals (t/globals conf)})
       m)
      (s/state mem (assoc m :state :error)))))

(defn check-task [mem task]
  (if (e/run-if mem task)
    (if (e/only-if-not mem task)
      (prn task)
      (s/state mem (assoc task :state :executed)))
    (s/state mem (assoc task :state :ready))))

(defn exec-fn [mem]
  (fn [m]
    (->> m
         (build-task mem)
         (check-task mem))))

(defn up [{mp-id :_id mp :Mp}]
  (let [m {:mp-id (keyword mp-id)}]
    (m/up mem m mp)
    (e/up mem m (m/exch mem m))
    (s/up mem (assoc m :struct :Container) (m/cont-states mem m) (exec mem))
    (s/up mem (assoc m :struct :Definitions) (m/defi-states mem m) (exec mem))))

(defn down [mp-id]
  (let [m {:mp-id mp-id}]
    (s/down mem (assoc m :struct :Container))
    (s/down mem (assoc m :struct :Definitions))
    (e/down mem m)
    (m/down mem m)))

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

(defn replace-launch-fn
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
  (let [m {:mp-id mp-id :ndx ndx}]
    (s/add-future mem (assoc m :struct :Container) f)
    (s/add-future mem (assoc m :struct :Definitions) f)))

(comment
  ;; generate a fresh mpd
  (up (-> {}
          mpd/standard->
          mpd/name->
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
