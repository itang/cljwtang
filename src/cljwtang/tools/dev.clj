(ns cljwtang.tools.dev
  (:require [cljwtang.tools.core :refer [lein]]))

(defn- lein-ring-server []
  (println ">> lein ring server ...")
  (lein "ring" "server"))

(defn- lein-cljsbuld []
  (println ">> lein cljsbuild auto app-debug app ...")
  (lein "cljsbuild" "auto" "app-debug" "app"))

(defn -main [& args]
  (try
    (future (lein-ring-server))
    (lein-cljsbuld)
    (catch Exception e (println e)
      (System/exit -1)))
  (System/exit 0))