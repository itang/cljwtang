(ns cljwtang.config.config-test
  (:require [me.raynes.fs :refer [home file]])
  (:use clojure.test
        cljwtang.config.app))

(deftest appdata-dir-test
  (let [path
        (.getAbsolutePath ^java.io.File (file (home) ".cljwtang/data"))]
    (is (= path (appdata-dir)))))
