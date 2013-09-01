(ns cljwtang.web.request-test
  (:use clojure.test
        cljwtang.web.request))

(deftest ajax?-test
  (is (ajax? {:headers {"x-requested-with" "XMLHttpRequest"}}))
  (is (not (ajax? {:headers {"x-requested-with" "XXXClient"}})))
  (is (not (ajax? {:headers {}})))
  (is (not (ajax? {:headers nil}))))
