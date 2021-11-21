(ns dx.exch)

(defonce mem (atom {}))

(defn up [mp-id exch] (swap! mem assoc mp-id (agent exch)))

(defn down [mp-id] (swap! mem dissoc mp-id))
