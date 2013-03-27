(ns cljwtang.tools.check-all
  (:require [cljwtang.tools.core :refer [lein]]))

(defn -main [& args]
  (println "[run lein check]")
  (lein "check")

  (println "[run lein kibit]")
  (lein "kibit")

  (println "[run lein eastwood]")
  (lein "eastwood")

  (println "[run lein bikeshed]")
  (lein "bikeshed")
  
  (System/exit 0))
