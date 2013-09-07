(ns cljwtang.utils.util
  (:require [cljtang.core :refer :all]
            [clojure.core.typed :refer :all :as typed]))

(ann nil-empty [(Seqable Any) -> (Seqable Any)])
(defn nil->empty [coll]
  (nil-> coll []))
