(ns cljwtang.utils.env
  (:require [environ.core :as environ]
            [coercer.core :refer [coerce]]))

(defn- ^Boolean coerce-bool [^String s]
  (if (or (empty? s) (= "false" (.toLowerCase s)))
    false
    true))

(defn env-config
  ([key] (env-config key nil))
  ([key default-value] (or (environ/env key) default-value)))

(def env-config-int (comp #(coerce % Integer) env-config))

(def env-config-bool (comp coerce-bool env-config))
