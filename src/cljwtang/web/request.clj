(ns cljwtang.web.request
  (:require [noir.request]))

(defn req []
  noir.request/*request*)

(defn reuqest-params []
  (:params (req)))

(defn ajax?
  ([]
    (ajax? (req)))
  ([req]
    (= "XMLHttpRequest"
       (get-in req [:headers "x-requested-with"]))))
