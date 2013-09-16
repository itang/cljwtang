(ns cljwtang.utils.upload
  (:require [me.raynes.fs :refer [copy file]]
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
  ([param]
   (let [mf (get-in *request* [:multipart-params (name param)])
         files (if (sequential? mf) mf [mf])
         files (filter valid-file? files)]
     (if (seq files) files nil)))
  ([]
    (->> (:multipart-params *request*)
      (map #(let [[name mf] %]
              [name (multipart-files name)]))
      (into {}))))

(defn has-multipart-files
  ([param]
   (boolean (seq (multipart-files param))))
  ([]
    (every? has-multipart-files (keys (:multipart-params *request*)))))

(defn multipart-file
  [param]
  (first (multipart-files param)))
