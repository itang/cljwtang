(ns lib-compojure.util
  (:require [noir.request :refer [*request*]]
            [noir.session :as session]
            [lib-compojure.core :refer :all]))

;;@see http://blog.fnil.net/index.php/archives/27
(defmacro defhandler
  [name args & body]
  `(defn ~name [req#]
     (let [{:keys ~args :or {~'req req#}} (:params req#)]
       ~@body)))

(defmacro with-validate [validates-fn success failture]
  `(let [ret# (~validates-fn (:params *request*))]
     (if (empty? ret#)
       ~success
       (do
         (session/flash-put! :msg (failture-message "验证错误" ret#))
         ~failture))))
