(ns dx.sheduler-test
  (:require [clojure.test :refer :all]
            [dx.scheduler :refer :all]))


(deftest all-pre-exec?-test-i
  (testing "basics"
    (is (true?
         (all-pre-exec? {:idx 1} [{:idx 0 :state :executed} {:idx 1 :state :executed} {:idx 1 :state :working}])))
    (is (false?
         (all-pre-exec? {:idx 1} [{:idx 0 :state :working} {:idx 1 :state :executed} {:idx 1 :state :working}])))))

