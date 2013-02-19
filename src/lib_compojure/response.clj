(ns lib-compojure.response
  (:require [noir.response :refer [content-type]]))

(defn html
  "Wraps the response with the content type for html and sets the body to the content."
  [content]
  (content-type "text/html; charset=utf-8" content))