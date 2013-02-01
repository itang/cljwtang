(ns lib-compojure.middleware
  (:require [clojure.string :as str]
            [clojure.tools.logging :refer [info]]
            [noir.response :refer [set-headers]]
            [stencil.loader :refer [invalidate-cache]]))

(defn- static-resource-request? [request]
  (let [^String uri (:uri request)]
    ;; OPTIMIZE
    (.contains uri ".")))

(defn wrap-stencil-refresh [handler]
  (fn [request]
    (if-not (static-resource-request? request)
      (invalidate-cache))
    (handler request)))

(defn- handle-with-log [handler request]
  (let [uri (:uri request)
        method (-> (:request-method request)
                 name
                 str/upper-case)
        req-info (str method " " uri)]
    (info req-info)
    (let [reps (handler request)
          status (:status reps)]
      (info req-info "->" status)
      reps)))

(defn wrap-request-log [handler]
  (fn [request]
    (if-not (static-resource-request? request)
      (handle-with-log handler request)
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

(defn wrap-exception-handling [handler & [f]]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (if f
          (f request e)
          {:status 500
           :body "Application Error!"
           :headers {"Content-Type" "text/html; charset=utf-8"}
           :exception e})))))
