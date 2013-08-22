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
  (regist-tag [_ k v]
    (println (keyword (str "end" (name k))))
    (parser/add-tag! k v (keyword (str "end-" (name k)))))
  (regist-filter [_ k v]
    (filters/add-filter! k v))
  (clear-cache! [_]))

(defn new-selmer-template-engine []
  #_(parser/set-resource-path! "/var/html/templates/")
  (parser/add-tag! :withcss (fn [a1 a2 a3] "<h1>css</h2>") :endwithcss)
  (SelmerTemplateEngine. "Selmer" "templates/" ".sm.html"))
