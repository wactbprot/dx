(ns dx.cli
    ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
      :doc "The dx command line interface. "}
  (:require [dx.exch :as e]
            [dx.model :as m]
            [dx.scheduler :as s]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

;; scheduler mem
(defonce s-mem (atom {})) 

;; model mem
(defonce m-mem (atom {})) 

;; exchange mem
(defonce e-mem (atom {})) 

(defn get-ref-mpd []
  (->  (io/resource "mpd-ref.edn")
       slurp
       edn/read-string))

(defn up [{mp-id :_id mp :Mp}]
  (let [m {:mp-id (keyword mp-id)}]
    (m/up m-mem m mp)
    (s/up s-mem (assoc m :struct :cont) (m/cont-states m-mem m))
    (s/up s-mem (assoc m :struct :defi) (m/defi-states m-mem m))
    #_(e/up e-mem mp-id (m/exch m-mem  mp-id))))

(defn down [mp-id]
  (let [m {:mp-id mp-id}]
    (s/down s-mem (assoc m :struct :cont))
    (s/down s-mem (assoc m :struct :defi))
    #_(e/down e-mem m)
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
