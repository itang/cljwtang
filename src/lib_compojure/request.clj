(ns lib-compojure.request)

(defn ajax? [req]
  (= (get-in req [:headers "x-requested-with"])
     "XMLHttpRequest"))