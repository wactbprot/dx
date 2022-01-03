(ns dx.exch
  (:require [clojure.string :as string]))

(defn up [mem {id :mp-id} exch]
  (swap! mem assoc-in [id :Exchange] (agent exch)))

(defn down [mem {id :mp-id}] (swap! mem dissoc id :Exchange))

(defn exch-agent [mem {id :mp-id}] (get-in @mem [id :Exchange]))

;; ................................................................................
;; utils
;; ................................................................................

(defn path->kw
  [s f]
  {:pre [(string? s)]}
  (-> (string/split s #"\.") f keyword))


(defn path->first-kw
  "Returns the first part of `path` as keyword or nil.

  ```clojure
  (path->first-kw \"foo\" )
  ;; nil
  (path->first-kw \"foo.bar\" )
  ;; :bar
  ```"  
  [s]
  (path->kw s first))

(defn path->second-kw
  "Returns the second part of `path` as keyword or nil.
  ```clojure
  (path->second-kw \"foo\" )
  ;; nil
  (path->second-kw \"foo.bar\" )
  ;; :bar
  ```"  
  [s]
  (path->kw s second))

;; ................................................................................
;; read, from
;; ................................................................................
(defn rd
  "Reads the value at `path`. `path` must be a `string` and may be
  concatenated by a dot (`.`).  `

  Example
  ``clojure
  (read mem {:mp-id :mpd-ref :exch \"A.Unit\"})
  ;; \"Pa\"
  ```"
  [mem {mp-id :mp-id path :exch :as m}]
  (let [a (exch-agent mem m)]
    (await a)
    (if-let [x (get @a (keyword path))]
      x
      (let [first-kw (path->first-kw path)
            second-kw (path->second-kw path)]
        (if-let [x (get-in @a [first-kw second-kw])]
          x
          (get-in @a [first-kw]))))))
        

(defn from
  "Replaces the values of the given `from-map` by means of
  [[rd]].
  
  Example:
  ```clojure

  (from mem {:mp-id :mpd-ref} {:%check \"A\"})
  ;; =>
  ;; {:%check {:Type \"ref\" :Unit \"Pa\" :Value 100.0}}
  ```"
  [mem m from-map]
  (when (map? from-map)
    (into {}
          (mapv (fn [[k v]]
                  {k (rd mem (assoc m :exch v))})
                from-map))))

(comment
  (def complete-task-view  (:rows (che/decode (slurp "test/dx/tasks.json") true)))
  
  (defn vfv [m] (->> m :value :FromExchange vals))

  (def all-exchange-vals (->> complete-task-view
                              (mapv vfv)
                              flatten
                              (filterv some?)
                              (mapv name)    
                              (distinct))))
