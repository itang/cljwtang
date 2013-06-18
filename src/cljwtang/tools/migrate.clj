(ns cljwtang.tools.migrate
  (:require [clojure.tools.logging :as log]
            [lobos.core :refer [migrate]]
            [lobos.analyzer :refer [analyze-schema]]))

(defn -main [& args]
  (log/info "try migrate ...")
  (migrate)
  (log/info "after migrate ...")
  (prn (-> (analyze-schema) :tables keys)))
