(ns cljwtang.template.core
  (:refer-clojure :exclude [name]))

(defprotocol TemplateEngine
  (name [this] "name")
  (render-string [this template data] "render string")
  (render-file [this template-name data] "render file")
  (regist-helper [this k v] "add helper")
  (regist-tag [this k v m] "add tag")
  (regist-filter [this k v] "add filter")
  (clear-cache! [this] "clear cache"))
