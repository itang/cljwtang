(ns cljwtang.utils.env
  (:require [environ.core :as environ]))

(defn env-config
  ([key] (env-config key nil))
  ([key default-value] (or (environ/env key) default-value)))
