(ns dx.model)

(defonce mem (atom {}))

(comment
  (alter-var-root #'mem {}))
;; ....................................................................................................
;; all in, all out
;; ....................................................................................................
(defn up [mem {id :mp-id} mpd] (swap! mem assoc id mpd))
(defn down [mem {id :mp-id}] (swap! mem dissoc id))

;; ....................................................................................................
;; container (cont)
;; ....................................................................................................
(defn conts       [mem {id :mp-id}] (get-in @mem [id :Container]))
(defn cont-defs   [mem {id :mp-id}] (mapv :Definition (conts id)))
(defn cont-titles [mem {id :mp-id}] (mapv :Title (conts id)))
(defn cont-ctrls  [mem {id :mp-id}] (mapv (comp keyword :Ctrl) (conts id)))

;; ....................................................................................................
;; definitions (defi)
;; ....................................................................................................
(defn defis     [mem {id :mp-id}] (get-in @mem [id :Definitions]))
(defn defi-defs [mem {id :mp-id}] (mapv :Definition (defis id)))

;; ....................................................................................................
;; exchange (exch)
;; ....................................................................................................
(defn exch [mem {id :mp-id}] (get-in @mem [id :Exchange]))

;; ....................................................................................................
;; states
;; ....................................................................................................
(defn states [v] (mapv (fn [vv] (mapv (fn [vvv] (mapv (constantly :ready) vvv)) vv)) v))
(defn cont-states [mem m] (states (cont-defs mem m)))
(defn defi-states [mem m] (states (defi-defs mem m)))

