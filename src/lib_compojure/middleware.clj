(ns lib-compojure.middleware
  (:require [clojure.string :as str]
            [clojure.tools.logging :refer [info]]
            [noir.response :refer [set-headers]]
            [stencil.loader :refer [invalidate-cache]]))

(defn wrap-stencil-refresh [handler]
  (fn [request] 
    (invalidate-cache)
    (handler request)))

(defn wrap-request-log [handler]
  (fn [request]
    (let [^String uri (:uri request)]
      (if-not (.contains uri ".") ; 非.js, .css, .jpg等web资源
        (let [method (-> (:request-method request)
                       name
                       str/upper-case)
              req-info (str method " " uri)]
          (info req-info)
          (let [reps (handler request)
                status (:status reps)]
            (info req-info "->" status)
            reps)))
        (handler request))))

(defn wrap-dev-helper [handler]
  (-> handler 
    wrap-stencil-refresh
    wrap-request-log))

(defn wrap-profile [handler]
  (fn [request]
    (let [start (System/currentTimeMillis)]
      (when-let [ret (handler request)]
        (set-headers {"Profile-Time"
                      (str (- (System/currentTimeMillis) start) "ms")}
                     ret)))))
