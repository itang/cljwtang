(ns cljwtang.web.response
  (:require [noir.response :refer [content-type set-headers]]))

(defn html
  "Wraps the response with the content type
   for html and sets the body to the content."
  [content]
  (content-type "text/html; charset=utf-8" content))

(defn content-length
  ([content]
     (content-length (count content) content)) ;;TODO improve count
  ([length content]
     (set-headers {"Content-Length" length} content)))
