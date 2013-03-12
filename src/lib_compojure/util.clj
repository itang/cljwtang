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

;;;; validate fn 规范 
;;;;  (validate-fn (:params *request*)) => {:user ["error1"]}
(defmacro with-validates [validates-fn success failture]
  `(let [find# (atom  false)
         fs# (atom ~validates-fn)
         ret# (atom nil)
         p# (:params *request*)]
     (while (and (not @find#)
                 (not (empty? @fs#)))
       (let [f# (first @fs#)
             r# (f# p#)]
         (if-not (empty? r#)
           (do (reset! find# true) (reset! ret# r#))
           (do (reset! fs# (next @fs#))))))
     (if-not (empty? @ret#)
       (do
         (session/flash-put! :msg (failture-message "验证错误" @ret#))
         ~failture)
       ~success)))

(defmacro defhandler-with-validates
  [handler args validates-fn &
   {:keys [success failture] 
    :or {failture
         #(throw (Exception. "validate error"))}}]
  `(defhandler ~handler [~@args]
     (with-validates ~validates-fn ~success ~failture)))
