(ns dx.exch)

(defn up [mem {id :mp-id} exch] (swap! mem assoc-in  [id :Exchange] (agent exch)))

(defn down [mem {id :mp-id}] (swap! mem dissoc id :Exchange))

(defn exchange-agent [mem {id :mp-id}] (get-in @mem [id :Exchange]))
