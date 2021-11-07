(ns dxrt.mem)

(defonce mem (atom {}))

;; ....................................................................................................
;; all in, all out
;; ....................................................................................................
(defn up [{mp-id :_id mpd :Mp}] (swap! mem assoc (keyword mp-id) mpd))
(defn down [mp-id] (swap! mem dissoc mp-id))

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
(defn defi-defs [mp-id] (mapv :Definition ))

;; ....................................................................................................
;; exchange (exch)
;; ....................................................................................................
(defn exch [mp-id] (get-in @mem [mp-id :Exchange]))

;; ....................................................................................................
;; states
;; ....................................................................................................
(defn template [v f] (mapv (fn [vv] (mapv (fn [vvv] (mapv f vvv)) vv)) v))

(defn cont-states [mp-id] (template (cont-defs mp-id) (constantly :ini)))
(defn defi-states [mp-id] (template (defi-defs mp-id) (constantly :ini)))

;; ....................................................................................................
;; ctrls
;; ....................................................................................................
(defn cont-ctrls [mp-id] (ctrls (cont-defs mp-id) (constantly :ini)))
(defn defi-ctrls [mp-id] (ctrls (defi-defs mp-id) (constantly :ini)))

