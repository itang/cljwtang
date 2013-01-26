(ns lib-compojure.util)

;;@see http://blog.fnil.net/index.php/archives/27
(defmacro defhandler
  [name args & body]
  `(defn ~name [req#]
     (let [{:keys ~args :or {~'req req#}} (:params req#)]
       ~@body)))
