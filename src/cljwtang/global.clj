(ns cljwtang.global
  (:require [cljwtang.template.selmer :as selmer]))

(defonce template-engine
  (selmer/new-selmer-template-engine))
