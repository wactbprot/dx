(ns dx.task
  (:require [dx.db :as db]
            [cheshire.core :as che]
            [clojure.string :as string]
            [clj-time.core :as tm]
            [clj-time.format :as tm-f]
            [clj-time.coerce :as tm-c]))

(defn get-date-object [] (tm/now))
(defn get-hour  [d] (tm-f/unparse (tm-f/formatter "HH")   d))
(defn get-min   [d] (tm-f/unparse (tm-f/formatter "mm")   d))
(defn get-sec   [d] (tm-f/unparse (tm-f/formatter "ss")   d))
(defn get-day   [d] (tm-f/unparse (tm-f/formatter "dd")   d))
(defn get-month [d] (tm-f/unparse (tm-f/formatter "MM")   d))
(defn get-year  [d] (tm-f/unparse (tm-f/formatter "YYYY") d))

(defn get-date 
  ([] (get-date (get-date-object)))
  ([d] (tm-f/unparse (tm-f/formatters :date) d)))

(defn get-time
  ([] (str (tm-c/to-long (get-date-object))))
  ([d] (str (tm-c/to-long d))))

(defn date-map
  "Returns a map with date/time replacements.

  Example
  ```clojure
  (date-map {:at-replace \"%\"})
  ;; {\"%hour\"  \"14\",
  ;; \"%minute\" \"07\",
  ;; \"%second\" \"54\",
  ;; \"%year\"   \"2020\",
  ;; \"%month\"  \"02\",
  ;; \"%day\"    \"02\",
  ;; \"%time\"   \"1580652474824\"}
  ```"
  ([{a :at-replace}]
   (let [d (get-date-object)]
     {(str a "hour") (get-hour d)
      (str a "minute") (get-min d)
      (str a "second") (get-sec d)
      (str a "year") (get-year d)
      (str a "month") (get-month d)
      (str a "day") (get-day d)
      (str a "time") (get-time d)})))

(defn apply-to-map-values
  "Applies function `f` to the values of the map `m`."
  [f m]
  (into {} (mapv (fn [[k v]]
                  (if (map? v)
                    [k (apply-to-map-values f v)]
                    [k (f v)]))
                m)))

(defn apply-to-map-keys
  "Applies function `f` to the keys of the map `m`."
  [f m]
  (into {} (mapv (fn [[k v]]
                  (if (map? v)
                    [(f k) (apply-to-map-keys f v)]
                    [(f k) v]))
                m)))

(defn outer-replace-map
  "Replaces tokens (given in `m`) in `task`.
  
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

  Example:
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

  Example:
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

(defn task-conf [task-name {d :db-task-design  v :db-task-view o :db-opt :as conf}]
  (assoc conf
        :db-design d 
        :db-view v
        :db-opt (assoc o :query-params {"key" (che/encode task-name)})))

