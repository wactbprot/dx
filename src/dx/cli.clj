(ns dx.cli
    ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
      :doc "The dx command line interface. "}
  (:require [dx.exch :as e]
            [dx.model :as m]
            [dx.task :as t]
            [dx.scheduler :as s]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

;; entire mem
(defonce mem (atom {})) 

(defn get-ref-mpd []
  (->  (io/resource "mpd-ref.edn")
       slurp
       edn/read-string))

(comment
  (defn get-task
  "Trys to gather all information belonging to `m`. Calls `prepair` and
  `assemble` function.`
  
  TODO: The Tasks should be provided by the ltmem. Storing the Tasks
  at the stmem was not the bet idea." 
  [e-mem pre-task]
  (try
    (let [pre-task    (stmem/get-val (assoc m :func :defin))
          raw-task    (ltmem/get-task (:TaskName pre-task))
          from-map    (exch/from (exch/all m) (:FromExchange raw-task))
          globals-map (utils/date-map)]
       (prepair pre-task raw-task from-map globals-map m)))
    (catch Exception e
      (stmem/set-state-error (assoc m :message (.getMessage e))))))

(t/assemble [m]
 {:Task (dissoc raw-task :Defaults :Use :Replace) 
   :Replace rep-m
   :Use use-m
   :Defaults (:Defaults raw-task)
   :FromExchange from-m
   :Globals globals-m})

(defn up [{mp-id :_id mp :Mp}]
  (let [m {:mp-id (keyword mp-id)}]
    (m/up m-mem m mp)
    (e/up e-mem m (m/exch m-mem mp-id))
    (s/up s-mem (assoc m :struct :cont) (m/cont-states m-mem m) prn)
    (s/up s-mem (assoc m :struct :defi) (m/defi-states m-mem m) prn)))

(defn down [mp-id]
  (let [m {:mp-id mp-id}]
    (s/down s-mem (assoc m :struct :cont))
    (s/down s-mem (assoc m :struct :defi))
    (e/down e-mem m)
    (m/down m-mem m)))
    
(defn stop [mp-id ndx]
  (s/ctrl s-mem  {:mp-id mp-id
                  :struct :cont
                  :ndx ndx
                  :ctrl :stop}))

(defn run [mp-id ndx]
  (s/ctrl s-mem {:mp-id mp-id
                 :struct :cont
                 :ndx ndx
                 :ctrl :run}))
  
(defn set-cont-state [mp-id ndx idx jdx state]
  (s/state s-mem {:mp-id mp-id
                  :struct :cont
                  :ndx ndx
                  :idx idx
                  :jdx jdx
                  :state state}))
  
(comment
  (up (get-ref-mpd))
  (run :mpd-ref 0)
  (set-cont-state :mpd-ref 0 0 0 :executed)
  (down :mpd-ref))
