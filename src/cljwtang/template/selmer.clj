(ns cljwtang.template.selmer
  (:require [selmer.parser :as parser]
            [selmer.filters :as filters]
            [cljwtang.template.core :refer [TemplateEngine]]))

(deftype SelmerTemplateEngine
    [the-name template-path-prefix template-path-suffix]
  TemplateEngine
  (name [_]
    the-name)
  (render-string [_ template data]
    (parser/render template data))
  (render-file [_ template-name data]
    (parser/render-file
     (str template-path-prefix template-name template-path-suffix) data))
  (regist-helper [_ k v]
    (parser/add-tag! k v))
  (regist-tag [_ k v m]
    (parser/add-tag! k v m))
  (regist-filter [_ k v]
    (filters/add-filter! k v))
  (clear-cache! [_]))

(defn new-selmer-template-engine []
  (filters/add-filter! :empty? empty?)
  (SelmerTemplateEngine. :selmer "templates/" ".html"))
