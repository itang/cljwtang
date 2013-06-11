(ns cljwtang.templates
  (:require [cljwtang.datatype :refer [-render-file -regist-tag -clear-cache!]]
            [cljwtang.impls.stencil :refer [new-stencil-template-engine]]))

(defonce ^:dynamic *template-engine*
  (new-stencil-template-engine))

(defn set-template-engine! [template-engine]
  (alter-var-root (var *template-engine*) (constantly template-engine)))

(defn clear-template-cache! []
  (-clear-cache! *template-engine*))

(defn render-file [template-name data]
  (-render-file *template-engine* template-name data))

(defn regist-tag [k v]
  (-regist-tag *template-engine* k v))
