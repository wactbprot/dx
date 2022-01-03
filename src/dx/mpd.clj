(ns dx.mpd
  ^{:author "Wact.B.Prot <wactbprot@gmail.com>"
    :doc "Generates mpds. "}
  (:require [clojure.string :as string]))

(def d {:standard "NN"
        :mp-name "generic"
        :task-name "Common-wait"
        :mp-descr "Default description"
        :exch {:Default {:Bool true}}
        :cont-title "Default title"
        :cont-descr "Default container description"
        :cont-elem [:Default]
        :defi-class "default"
        :defi-descr "Default description"
        :defi-cond [{:ExchangePath "Default.Bool", :Methode "eq", :Value true}]})

(defn mpd-id [s n]
  (str "mpd-" (string/lower-case s) "-" (string/replace (string/lower-case n) #"\s" "_")))

(defn pre-task
  ([] {:TaskName (:task-name d)})
  ([n] {:TaskName n})
  ([n r] {:TaskName n :Replace r})
  ([n r u] {:TaskName n :Replace r :Use u}))

(defn standard->
  ([m] (standard-> m (:standard d)))
  ([m p] (assoc-in m [:Mp :Standard] p)))

(defn name->
  ([m] (name-> m (:mp-name d)))
  ([{{s :Standard} :Mp :as m} n]
   (-> m
       (assoc :_id (mpd-id s n)) 
       (assoc-in  [:Mp :Name] n))))

(defn descr->
  ([m] (descr-> m (:mp-descr d)))
  ([m s]
   (assoc-in m [:Mp :Description] s)))

(defn exch->
  ([m] (exch-> m (:exch d)))
  ([m e]
   (assoc-in m [:Mp :Exchange] e)))

(defn cont->
  ([m] (cont-> m (:cont-title d) (:cont-descr d) (:cont-elem d) [[(pre-task)]]))
  ([m title] (cont-> m title (:cont-descr d) (:cont-elem d) [[(pre-task)]]))
  ([m title descr] (cont-> m title descr (:cont-elem d) [[(pre-task)]]))
  ([m title descr elem] (cont-> m title descr elem [[(pre-task)]])) 
  ([m title descr elem defin]
   (let [cont {:Title title
               :Description descr
               :Element elem
               :Definition defin}
         v [:Mp :Container]]
     (assoc-in m v (if-let [conts (get-in m v)]
                     (conj conts cont)
                     [cont])))))

(defn defi->
  ([m] (defi-> m (:defi-class d) (:defi-descr d) (:defi-cond d) [[(pre-task)]]))
  ([m defi-class] (defi-> m defi-class (:defi-descr d) (:defi-cond d) [[(pre-task)]]))
  ([m defi-class descr] (defi-> m defi-class descr (:defi-cond d) [[(pre-task)]]))
  ([m defi-class descr defi-cond] (defi-> m defi-class descr defi-cond [[(pre-task)]])) 
  ([m defi-class descr defi-cond defin]
   (let [defi {:DefinitionClass defi-class
               :ShortDescr descr
               :Condition defi-cond
               :Definition defin}
         v [:Mp :Definitions]]
     (assoc-in m v (if-let [defis (get-in m v)]
                     (conj defis defi)
                     [defi])))))

(comment
(-> {}
      standard->
      name->
      descr->
      exch->
      cont->
      defi->))
