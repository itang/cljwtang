(ns lib-compojure.util)

;;@see http://blog.fnil.net/index.php/archives/27
(defmacro defhandler
  [name args & body]
  `(defn ~name [req#]
     (let [{:keys ~args :or {~'req req#}} (:params req#)]
       ~@body)))

(defn ajax-request? [req]
  (= (get-in req [:headers "x-requested-with"])
     "XMLHttpRequest"))