(ns dx.scheduler-test
  (:require [clojure.test :refer :all]
            [dx.scheduler :refer :all]))


(deftest all-pre-exec?-test-i
  (testing "basics"
    (is (true?
         (all-pre-exec? {:idx 1} [{:idx 0 :state :executed}
                                  {:idx 1 :state :executed}
                                  {:idx 1 :state :working}])))
    (is (false?
         (all-pre-exec? {:idx 1} [{:idx 0 :state :working}
                                  {:idx 1 :state :executed}
                                  {:idx 1 :state :working}])))))

(deftest check-error-test-i
  (testing "basics"
    (is (= :error
           (:ctrl (->error {:states [{:idx 0 :state :error}
                                         {:idx 1 :state :executed}]}))))
    (is (nil?
         (:ctrl (->error {:states [{:idx 0 :state :working}
                                       {:idx 1 :state :executed}]}))))))

(deftest check-launch-test-i
  (testing "basics"
    (is (= 0
           (:idx (:launch (->launch {:ctrl :run
                                     :states [{:idx 0 :state :ready}
                                                  {:idx 1 :state :executed}]})))))
    (is (= 1
           (:idx (:launch (->launch {:ctrl :run
                                     :states [{:idx 0 :state :executed}
                                                  {:idx 1 :state :ready}]})))))
    (is (nil?
         (:idx (:launch (->launch {:ctrl :run
                                   :states [{:idx 0 :state :executed}
                                                {:idx 1 :state :executed}]})))))))


(deftest check-launch-test-ii
  (testing "find launch and set working"
    (is (= {:ctrl :run
            :states [{:idx 0 :state :working}
                     {:idx 1 :state :executed}]
            :launch {:idx 0 :state :ready}}
           (->launch {:ctrl :run
                      :states [{:idx 0 :state :ready}
                               {:idx 1 :state :executed}]})))))


(deftest check-end-test-i
  (testing "basics"
    (is (= {:ctrl :run
            :states [{:idx 0 :state :ready}
                     {:idx 1 :state :executed}]}
           (->end {:ctrl :run
                   :states [{:idx 0 :state :ready}
                            {:idx 1 :state :executed}]})))
    (is (= {:ctrl :ready
            :states [{:idx 0 :state :ready}
                     {:idx 1 :state :ready}]}
           (->end {:ctrl :run
                   :states [{:idx 0 :state :executed}
                            {:idx 1 :state :executed}]})))))
