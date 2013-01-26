(ns lib-compojure.util-test
  (:use clojure.test
        lib-compojure.util))

(defhandler h1 [a b]
  (str a b))

(defhandler h2 [a b req]
  (str a b (get-in req [:params :a])))

(deftest defhandler-test
  (testing "defhandler"
           (is (= "ab" (h1 {:params {:a "a" :b "b"}})))
           (is (= "a" (h1 {:params {:a "a"}})))
           (is (= "aba" (h2 {:params {:a "a" :b "b"}})))))
