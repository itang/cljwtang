(ns cljwtang.template.stencil
  (:require [stencil.core :as stencil]
            [stencil.loader :as stencil-loader]
            [cljwtang.template.core :refer [TemplateEngine]]))

(deftype StencilTemplateEngine
  [the-name tags-map template-path-prefix template-path-suffix]
  TemplateEngine
  (name [_]
    the-name)
  (render-string [_ template data]
    (stencil/render-string template (merge @tags-map data)))
  (render-file [_ template-name data]
    (stencil/render-file 
      (str template-path-prefix template-name template-path-suffix)
      (merge @tags-map data)))
  (regist-tag [_ k v]
    (swap! tags-map assoc k v))
  (regist-filter [_ k v] )
  (clear-cache! [_]
    (stencil-loader/invalidate-cache)))

(defn new-stencil-template-engine []
  (StencilTemplateEngine. "stencil" (atom {}) "templates/" ".html"))
