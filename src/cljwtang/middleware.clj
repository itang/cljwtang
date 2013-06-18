(ns cljwtang.middleware
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [noir.response :refer [set-headers]]
            [cljwtang.core :refer :all]
            [cljwtang.view :refer :all]
            [cljwtang.templates :as templates]
            [cljwtang.request :refer [ajax?]]))

(defn- static-resource-request? [request]
  (let [^String uri (:uri request)]
    ;; OPTIMIZE
    (.contains uri ".")))

(defn wrap-templates-refresh [handler]
  (fn [request]
    (when-not (static-resource-request? request)
      (templates/clear-template-cache!))
    (handler request)))

(defn wrap-view [handler]
  (fn [req]
    (if (ajax? req)
      (handler req)
      (binding [*more-js* nil *more-css* nil]
        (handler req)))))

(defn- handle-with-log [handler request]
  (let [uri (:uri request)
        method (-> (:request-method request)
                 name
                 str/upper-case)
        req-info (str method " " uri)]
    (log/info req-info)
    (let [reps (handler request)
          status (:status reps)]
      (log/info req-info "->" status)
      reps)))

(defn wrap-request-log [handler]
  (fn [request]
    (if-not (static-resource-request? request)
      (handle-with-log handler request)
      (handler request))))

(defn wrap-dev-helper [handler]
  (-> handler
    wrap-templates-refresh
    wrap-request-log))

(defn wrap-profile [handler]
  (fn [request]
    (let [start (System/nanoTime)]
      (when-let [ret (handler request)]
        (set-headers {"X-Runtime"
                      (->> (- (System/nanoTime) start)
                        (* 0.000000001)
                        (format "%.5f"))}
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
