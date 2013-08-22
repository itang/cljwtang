(ns cljwtang.template.core
  (:refer-clojure :exclude [name]))

(defprotocol TemplateEngine
  (name [this] "name")
  (render-string [this template data] "render string")
  (render-file [this template-name data] "render file")
  (regist-tag [this k v] "add helper")
  (regist-filter [this k v] "add filter")
  (clear-cache! [this] "clear cache"))
