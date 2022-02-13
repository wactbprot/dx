(ns dx.model)

;; ................................................................................
;; container (cont)
;; ................................................................................
(defn conts [mem {id :mp-id}] (get-in mem [id :Container]))

(defn cont-defs   [mem m] (mapv :Definition (conts mem m)))
(defn cont-titles [mem m] (mapv :Title (conts mem m)))

;; ................................................................................
;; definitions (defi)
;; ................................................................................
(defn defis     [mem {id :mp-id}] (get-in mem [id :Definitions]))
(defn defi-defs [mem m] (mapv :Definition (defis mem m)))

;; ................................................................................
;; exchange (exch)
;; ................................................................................
(defn exch [mem {id :mp-id}] (get-in mem [id :Exchange]))

;; ................................................................................
;; pre tasks
;; ................................................................................
(defn pre-task [mem {:keys [mp-id struct ndx idx jdx]}]
    (get-in mem [mp-id struct ndx :Definition idx jdx]))

;; ................................................................................
;; states
;; ................................................................................
(defn flattenv [coll] (into [] (flatten coll))) 

(defn state-vec 
  "Turns the state vector `s` (a vector of vectors with keywords in
  it) into a vector of maps which can be analysed more easy.

  Example:
  ```clojure
  (state-vec {:mp-id :mpe-ref} [[:foo] [:bar :baz]])

  ;; =>
  ;; [{:mp-id :mpe-ref :idx 0 :jdx 0, :state :foo}
  ;;  {:mp-id :mpe-ref :idx 1 :jdx 0, :state :bar}
  ;;  {:mp-id :mpe-ref :idx 1 :jdx 1, :state :baz}]
  ```"
  [loc vv]
  (flattenv (mapv (fn [v i]
                    (mapv (fn [s j] (merge loc {:idx i :jdx j :state s}))
                          v (range)))
                  vv (range))))

(defn states [v]
  (mapv (fn [vv] (mapv (fn [vvv] (mapv (constantly :ready) vvv)) vv)) v))

(defn cont-states [mem loc]  (states (cont-defs mem loc)))
(defn defi-states [mem loc]  (states (defi-defs mem loc)))

(defn state 
  "Builds up the `ndx` `struct`ure state interface for the mutating parts.
  Agents are used (see [[add-agent]]. 

  Example:
  ```clojure
  ;; states looks like this:
  (def s [[[:ready :ready]]
   [[:ready]]])
  
  ;; m carries the position:
  (def loc {:mp-id :a :struct :b})

  ;; call up returns:
  ;; (state mem loc s)
  ;; {:a
  ;; {:b
  ;; {0 #<Agent@149953c4: 
  ;;    {:states
  ;;     [{:mp-id :a, :struct :b, :ndx 0, :idx 0, :jdx 0, :state :ready}
  ;;      {:mp-id :a,
  ;;       :struct :b,
  ;;       :ndx 0,
  ;;       :idx 0,
  ;;       :jdx 1,
  ;;       :state :ready}],
  ;;     :ctrl :ready}>,
  ;;  1 #<Agent@3c3eb708: 
  ;;    {:states
  ;;     [{:mp-id :a,
  ;;       :struct :b,
  ;;       :ndx 1,
  ;;       :idx 0,
  ;;       :jdx 0,
  ;;       :state :ready}],
  ;;     :ctrl :ready}>}}}
  ```"
  [mem {:keys [mp-id struct] :as loc} states]
  (reduce-kv (fn [res ndx state]
               (let [loc (assoc loc :ndx ndx)
                     a (agent {:states (state-vec loc state)
                               :ctrl :ready})]
                 (assoc-in res [mp-id struct ndx] a)))
             mem states))

(defn exch-agent [mem {id :mp-id}] (get-in mem [id :Exchange]))

;; ................................................................................
;; all in, all out
;; ................................................................................
(defn up [mem {mp-id :_id mp :Mp}]
  (let [id   (keyword mp-id)
        loc  {:mp-id id}
        mem  (assoc mem id mp)]
    (-> mem
        (assoc-in  [id :Exchange] (agent (:Exchange mp)))
        (state (assoc loc :struct :Container) (cont-states mem loc))
        (state (assoc loc :struct :Definitions) (defi-states mem loc)))))

(defn down [mem {id :mp-id}] (dissoc mem id))
