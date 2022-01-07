(ns dx.dx
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dx command line interface."}
  (:require [dx.db :as db]
            [dx.config :as c]
            [dx.exch :as e]
            [dx.model :as m]
            [dx.task :as t]
            [dx.scheduler :as s]))

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

(defn up [mem {mp-id :_id mp :Mp}]
  (let [m {:mp-id (keyword mp-id)}]
    (m/up mem m mp)
    (e/up mem m (m/exch mem m))
    (s/up mem (assoc m :struct :Container) (m/cont-states mem m) (exec-fn mem))
    (s/up mem (assoc m :struct :Definitions) (m/defi-states mem m) (exec-fn mem))))

(defn down [mem mp-id]
  (let [m {:mp-id mp-id}]
    (s/down mem (assoc m :struct :Container))
    (s/down mem (assoc m :struct :Definitions))
    (e/down mem m)
    (m/down mem m)))

(defn replace-launch-fns [mem m f] (s/add-future mem m f))
