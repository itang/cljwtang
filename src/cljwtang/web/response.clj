(ns cljwtang.web.response
  (:require [noir.response :refer [content-type set-headers json]]
            [cljwtang.web.core :refer :all]))

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

(def ^{:doc "消息map -> JSON"}
  json-message (comp json message))

(def ^{:doc "success消息map -> JSON"}
  json-success-message (comp json success-message))

(def ^{:doc "failture消息map -> JSON"}
  json-failture-message (comp json failture-message))

(def ^{:doc "error消息map -> JSON"}
  json-error-message (comp json error-message))

(def ^{:doc "info消息map -> JSON"}
  json-info-message (comp json info-message))
