(ns dx.exch-test
  (:require [clojure.test :refer :all]
            [dx.exch :refer :all]
            [dx.core :as dx]
            [dx.mpd :as mpd]))

(def test-mem (atom {}))

(dx/up test-mem (-> (mpd/base)
                    mpd/exch->))

(deftest rd-i
  (testing "basics"
    (is (= true
           (rd test-mem {:mp-id :mpd-nn-generic :exch "Default.Bool"})))
    (is (nil?
         (rd test-mem {:mp-id :mpd-nn-generic :exch "Default.WrongPath"})))
    "replaced"))
