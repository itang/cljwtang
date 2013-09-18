(ns cljwtang.utils.upload
  (:require [plumbing.core :refer :all]
            [me.raynes.fs :refer [copy file]]
            [cljwtang.config.app :as config]
            [noir.request :refer [*request*]]
            [noir.validation :refer [valid-file?]]))

(defn ^java.io.File upload-file!
  "上传文件."
  [from to-path]
  (let [target (file (config/appdata-dir) to-path)]
    (copy from target)
    target))

(defn multipart-files
  ([]
  (when-let [m (map-vals multipart-files (:multipart-params *request*))]
    (if (every? nil? (vals m)) nil m)))
  ([param]
   (let [mf (get-in *request* [:multipart-params (name param)])
         files (if (sequential? mf) mf [mf])
         files (filter valid-file? files)]
     (when (seq files) files))))

(defn multipart-file
  [param]
  (first (multipart-files param)))
