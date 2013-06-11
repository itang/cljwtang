(ns cljwtang.tools.dev
  (:require [clojure.string :as string])
  (:require [cljwtang.tools.core :refer [lein]]))

(defn- lein-ring-server []
  (println ">> lein ring server ...")
  (lein "ring" "server"))

(defn- lein-cljsbuld [& params]
  (println ">> lein cljsbuild auto"
           (string/join " " params)
           "...")
  (apply lein "cljsbuild" "auto" params))

(defn -main [& args]
  (try
    (future (lein-ring-server))
    (pr args)
    (if (empty? args)
      (lein-cljsbuld "app-debug" "app")
      (apply lein-cljsbuld args))
    (catch Exception e (println e)
      (System/exit -1)))
  (System/exit 0))
