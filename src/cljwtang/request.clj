(ns cljwtang.request)

(defn ajax? [req]
  (= "XMLHttpRequest"
     (get-in req [:headers "x-requested-with"])))
