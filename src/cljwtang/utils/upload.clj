(ns cljwtang.utils.upload
  (:require [me.raynes.fs :refer [copy file]]
            [cljwtang.config.app :as config]))

(defn ^java.io.File upload-file
  "上传文件."
  [from to-path]
  (let [target (file (config/appdata-dir) to-path)]
    (copy from target)
    target))
