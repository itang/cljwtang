(ns cljwtang.web.response-test
  (:use clojure.test
        cljwtang.web.response))

(deftest html-test
  (is (= {:headers
          {"Content-Type" "text/html; charset=utf-8"}, :body "ss"}
         (html "ss"))))

(deftest content-length-test
  (is (= {:headers {"Content-Length" 2}, :body "ss"}
         (content-length (count "ss") "ss")))
  (is (= {:headers {"Content-Length" 2}, :body "ss"}
         (content-length "ss"))))
