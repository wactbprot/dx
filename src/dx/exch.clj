(ns dx.exch)

(defn up [mem {id :mp-id} exch] (swap! mem assoc-in [id :Exchange] (agent exch)))

(defn down [mem {id :mp-id}] (swap! mem dissoc id :Exchange))

(defn exchange-agent [mem {id :mp-id}] (get-in @mem [id :Exchange]))


(comment
  (def complete-task-view  (:rows (che/decode (slurp "test/dx/tasks.json") true)))
  
  (defn vfv [m] (->> m :value :FromExchange vals))

  (def all-exchange-vals (->> complete-task-view
                              (mapv vfv)
                              flatten
                              (filterv some?)
                              (mapv name)    
                              (distinct))))
