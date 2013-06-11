(ns cljwtang.tools.migrate
  (:require  [lobos.core :refer [migrate]]
             [lobos.analyzer :refer [analyze-schema]]))
  
(defn -main [& args]
  (println "try migrate ...")
  (migrate)
  (prn (-> (analyze-schema) :tables keys)))
