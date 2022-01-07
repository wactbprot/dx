(ns dx.db-test
  (:require [clojure.test :refer :all]
            [dx.db :refer :all]
            [cheshire.core :as che]
            [clojure.string :as string]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))


(gen/sample gen/string-alphanumeric 10)
(def prop
  (prop/for-all [v gen/string-alphanumeric]
                (let [s (str "@" v)
                      a (str "%" v)
                      x (safe {:at-replace "%"} s)]
                  (= a x))))

(tc/quick-check 100 prop)
                
(deftest safe-i
  (testing "replace strings"
    (is (= "%a"
           (safe {:at-replace "%"} "@a")))
    "replaced")
  (testing "don't replace strings"
    (is (= "@1"
           (safe {:at-replace "%"} "@1")))
    "replaced"))
