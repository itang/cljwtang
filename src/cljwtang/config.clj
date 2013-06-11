(ns cljwtang.config
  (:require [cljwtang.inject :refer [fn-app-config]]))

(defn host []
  (fn-app-config :platform.host "localhost:3000"))
