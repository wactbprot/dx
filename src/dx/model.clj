(ns dx.model)

(defonce mem (atom {}))

(comment
  (alter-var-root #'mem {}))
;; ....................................................................................................
;; all in, all out
;; ....................................................................................................
(defn up [mp-id  mpd]  (swap! mem assoc mp-id mpd))
(defn down [mp-id]  (swap! mem dissoc mp-id))

;; ....................................................................................................
;; container (cont)
;; ....................................................................................................
(defn conts       [mp-id] (get-in @mem [mp-id :Container]))
(defn cont-defs   [mp-id] (mapv :Definition (conts mp-id)))
(defn cont-titles [mp-id] (mapv :Title (conts mp-id)))
(defn cont-ctrls  [mp-id] (mapv (comp keyword :Ctrl) (conts mp-id)))

;; ....................................................................................................
;; definitions (defi)
;; ....................................................................................................
(defn defis     [mp-id] (get-in @mem [mp-id :Definitions]))
(defn defi-defs [mp-id] (mapv :Definition (defis mp-id)))

;; ....................................................................................................
;; exchange (exch)
;; ....................................................................................................
(defn exch [mp-id] (get-in @mem [mp-id :Exchange]))

;; ....................................................................................................
;; states
;; ....................................................................................................
(defn states [v] (mapv (fn [vv] (mapv (fn [vvv] (mapv (constantly :ready) vvv)) vv)) v))
(defn cont-states [mp-id] (states (cont-defs mp-id)))
(defn defi-states [mp-id] (states (defi-defs mp-id)))

;; ....................................................................................................
;; ctrls
;; ....................................................................................................
(defn ctrls [v] (mapv (constantly :ready) v))
(defn cont-ctrls [mp-id] (ctrls (cont-defs mp-id)))
(defn defi-ctrls [mp-id] (ctrls (defi-defs mp-id)))

