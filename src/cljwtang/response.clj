(ns cljwtang.response
  (:require [noir.response :refer [content-type set-headers]]))

(defn html
  "Wraps the response with the content type 
   for html and sets the body to the content."
  [content]
  (content-type "text/html; charset=utf-8" content))

(defn content-length
  [length content]
  (set-headers {"Content-Length" length} content))
