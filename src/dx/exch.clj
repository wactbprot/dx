(ns dx.exch)

(defonce mem (atom {}))

(defn up [mp-id exch] (swap! mem assoc mp-id exch))
