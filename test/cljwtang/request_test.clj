(ns cljwtang.request-test
  (:use clojure.test
        cljwtang.request))

(deftest ajax?-test
  (is (ajax? {:headers {"x-requested-with" "XMLHttpRequest"}}))
  (is (not (ajax? {:headers {"x-requested-with" "XXXClient"}})))
  (is (not (ajax? {:headers {}})))
  (is (not (ajax? {:headers nil}))))
