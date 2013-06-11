(ns cljwtang.tools.core
  (:require [clojure.java.io :refer :all]
            [me.raynes.conch.low-level :as sh]))

(defn- wrap-cmd [cmd]
  (let [^String os-name (get (System/getProperties) "os.name")]
    (if (.contains os-name "Windows")
      (str cmd ".bat")
      cmd)))

(defn- output [p]
  (doseq [line (line-seq (-> p :out input-stream reader))]
    (println line)))

(defn sh [cmd & more]
  (output (apply sh/proc cmd more)))

(defn lein [& more]
  (apply sh (wrap-cmd "lein") more))
