(ns dx.model)

(comment
  (alter-var-root #'mem {}))
;; ................................................................................
;; all in, all out
;; ................................................................................
(defn up [mem {id :mp-id} mpd] (assoc mem id mpd))
(defn down [mem {id :mp-id}] (dissoc mem id))

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
;; states
;; ................................................................................
(defn states [v]
  (mapv (fn [vv] (mapv (fn [vvv] (mapv (constantly :ready) vvv)) vv)) v))

(defn cont-states [mem m] (states (cont-defs mem m)))
(defn defi-states [mem m] (states (defi-defs mem m)))

(defn pre-task [mem {:keys [mp-id struct ndx idx jdx]}]
    (get-in mem [mp-id struct ndx :Definition idx jdx]))
