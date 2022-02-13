(ns dx.core
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "The dx command line interface."}
  (:require [dx.db :as db]
            [dx.config :as c]
            [dx.exch :as e]
            [dx.model :as m]
            [dx.task :as t]
            [dx.worker :as w]
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

(defn build-task [mem loc]
  (let [conf           (:conf @mem)
        {task-name     :TaskName
         use-map       :Use
         replace-map   :Replace} (m/pre-task mem loc)
        {defaults-map  :Defaults
         from-exch-map :FromExchange
         :as db-task} (get-task task-name conf)]
    (if (map? db-task)
      (merge
       (t/assemble {:Task (dissoc db-task :Defaults :Use :Replace) 
                    :Replace replace-map
                    :Use use-map
                    :Defaults defaults-map
                    :FromExchange (e/from mem loc from-exch-map)
                    :Globals (t/globals conf)})
       loc)
      (s/state mem (assoc loc :state :error)))))

(defn check-task [mem task]
  (if (e/run-if mem task)
    (if (e/only-if-not mem task)
      task
      (assoc task :state :executed))
    (assoc task :state :ready)))

(defn exec-fn 
  "Returns a function that closes over `mem`. The Function is called
  when the scheduler finds a new task to start."
  [mem]
  (fn [loc]
  (s/state mem (->> loc
                    (build-task mem)
                    (check-task mem)
                    (w/dispatch mem)))))


(defn down [mem mp-id]
  (let [m {:mp-id mp-id}]
    (-> mem
        (s/down (assoc m :struct :Container))
        (s/down (assoc m :struct :Definitions))
        (e/down m)
        (m/down m))))


