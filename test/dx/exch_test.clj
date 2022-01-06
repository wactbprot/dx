(ns dx.exch-test
  (:require [clojure.test :refer :all]
            [dx.exch :refer :all]
            [dx.mdp :as mdp]))

(def test-mem (atom {}))

(up test-mem (-> {}
                 mpd/standard->
                 mpd/name->
                 mpd/descr->
                 mpd/exch->))
