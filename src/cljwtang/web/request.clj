(ns cljwtang.web.request
  (:require [noir.request]))

(defn reuqest-params []
  (:params noir.request/*request*))

(defn ajax? [req]
  (= "XMLHttpRequest"
     (get-in req [:headers "x-requested-with"])))
