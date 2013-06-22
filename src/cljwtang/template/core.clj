(ns cljwtang.template.core
  (:refer-clojure :exclude [name]))

(defprotocol TemplateEngine
  (name [this] "name")
  (render-string [this template data] "render string")
  (render-file [this template-name data] "render file")
  (regist-tag [this k v] "add helper")
  (clear-cache! [this] "clear cache"))
