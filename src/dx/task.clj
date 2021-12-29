(ns dx.task
  (:require [dx.db :as db]
            [cheshire.core :as che]
            [clojure.string :as string]))

(defn apply-to-map-values
  "Applies function `f` to the values of the map `m`."
  [f m]
  (into {} (map (fn [[k v]]
                  (if (map? v)
                    [k (apply-to-map-values f v)]
                    [k (f v)]))
                m)))

(defn apply-to-map-keys
  "Applies function `f` to the keys of the map `m`."
  [f m]
  (into {} (map (fn [[k v]]
                  (if (map? v)
                    [(f k) (apply-to-map-keys f v)]
                    [(f k) v]))
                m)))

(defn outer-replace-map
  "Replaces tokens (given in the m) in the task.
  This kind of replacement is used during the task build up at the
  beginning of its life cycle.
  
  Example:
  ```clojure
  (outer-replace-map (globals) {:TaskName \"foo\" :Value \"%time\"})
  ;; {:TaskName \"foo\", :Value \"1580652820247\"}
  (outer-replace-map nil {:TaskName \"foo\" :Value \"%time\"})
  ;; {:TaskName \"foo\", :Value \"%time\"}
  ```"
  [m task]
  (if (map? m)
    (che/decode
     (reduce
      (fn [s [k v]] (string/replace s (re-pattern (name k)) (str v)))
      (che/encode task) m) true)
    task))

(defn inner-replace-map
  "Applies the generated function `f` to the values `v` of the `task`
  map. `f`s input is `v`.  If `m` has a key `v` the value of this key
  is returned.  If `m` has no key `v` the `v` returned.  This kind of
  replacement is used during the runtime."
  [m task]
  (let [nm (apply-to-map-keys name m)
        f (fn [v]
            (if-let [r (get nm  v)]
              (if (map? r) (apply-to-map-keys keyword r) r)
              v))]
    (apply-to-map-values f task)))

(defn extract-use-value
  "TODO: write test, refactor to `(k m)`."
  [task m k]
  ((keyword (m k)) (task k)))

(defn str->singular-kw
  "Takes a keyword or string and removes the tailing letter (most likely
  a s). Turns the result to a keyword.
  
  ```clojure
  (str->singular-kw :Values)
  ;; :Value
  (str->singular-kw \"Values\")
  ;; :Value
  ``` "
  [s]
  (->> s name (re-matches #"^(\w*)(s)$") second keyword))

(defn merge-use-map
  "The use keyword enables a replace mechanism.
  It works like this: proto-task:
  
  ```clojure
  Use: {Values: med_range}
  ;; should lead to:
  task: { Value: rangeX.1}
  ```"
  [m task]
  (if (map? m)
    (merge task (into {} (mapv
                          #(hash-map (str->singular-kw %) (extract-use-value task m %))
                          (keys m))))
    task))

(defn assemble
  "Assembles the `task` from the given `meta-m`aps in a special order:

  * merge Use
  * replace from Replace
  * replace from Defaults
  * replace from Globals
   ```"
  [{from-m :FromExchange globals-m :Globals def-m :Defaults use-m :Use rep-m :Replace task :Task}]
   (assoc 
    (->> task
         (merge-use-map use-m)
         (inner-replace-map from-m)
         (outer-replace-map rep-m)
         (outer-replace-map def-m)
         (outer-replace-map globals-m)
         (outer-replace-map from-m))
    :Use use-m
    :Replace rep-m))

(defn task-conf [task-name {d :db-task-design  v :db-task-view :as conf}]
  (assoc conf
        :db-design d 
        :db-view v
        :view-key task-name))

