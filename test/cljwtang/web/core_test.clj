(ns cljwtang.web.core-test
  (:use clojure.test
        cljwtang.web.core))

(defhandler h1 [a b]
  (str a b))

(defhandler h2 [a b req]
  (str a b (get-in req [:params :a])))

(deftest defhandler-test
  (testing "defhandler"
    (is (= "ab" (h1 {:params {:a "a" :b "b"}})))
    (is (= "a" (h1 {:params {:a "a"}})))
    (is (= "aba" (h2 {:params {:a "a" :b "b"}})))))

(deftest message-test
  (is (= {:success true :message "msg" :data {} :detailMessage "" :type :success}
         (message true "msg"))))

(deftest render-string-test
  (is (= "hello, itang"
         (render-string "hello, {{name}}" {:name "itang"}))))
