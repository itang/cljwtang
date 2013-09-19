(ns cljwtang.utils.upload-test
  (:use clojure.test
        cljwtang.utils.upload)
  (:require [noir.request :refer [*request*]]))

(deftest multipart-files-test
  (binding [*request* {:multipart-params nil}]
    (is (nil? (multipart-files)))
    (is (nil? (multipart-files "file")))
    (is (nil? (multipart-file "file"))))
  ;; non valid files
  (binding [*request* {:multipart-params {"file" {:size 0 :filename ""}}}]
    (is (nil? (multipart-files)))
    (is (nil? (multipart-files "file"))))
  (binding [*request* {:multipart-params {"file" {:size 10 :filename "t.txt"}}}]
    (is (= 1 (count (multipart-files))))
    (is (= 1 (count (multipart-files "file"))))
    (is (= 10 (-> (multipart-files "file") first :size)))
    (is (= "t.txt" (-> (multipart-file "file") :filename))))
  (binding [*request* {:multipart-params {"file" [{:size 10 :filename "t.txt"}]}}]
    (is (= 1 (count (multipart-files))))
    (is (= 1 (count (multipart-files "file"))))
    (is (= 10 (-> (multipart-files "file") first :size)))
    (is (= "t.txt" (-> (multipart-file "file") :filename))))
  (binding [*request* {:multipart-params {"file" [{:size 10 :filename "t.txt"}
                                                  {:size 20 :filename "t2.txt"}]
                                          "file2" [{:size 10 :filename "t.txt"}
                                                   {:size 20 :filename "t2.txt"}]}}]
    (is (= 2 (count (multipart-files))))
    (is (= 2 (count (multipart-files "file"))))
    (is (= 20 (-> (multipart-files "file2") second :size)))
    (is (= "t.txt" (-> (multipart-file "file2") :filename)))))
