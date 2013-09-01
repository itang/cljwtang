(ns cljwtang.server
  (:require [clojure.java.io :as io]
            [cljtang.core :refer :all]
            [compojure.route :as route]
            [taoensso.tower.ring :refer [wrap-i18n-middleware]]
            [org.httpkit.server :as httpkit])
  (:require [cljwtang.lib :refer :all]))

(defn- exception-handle
  "统一异常处理"
  [req exception]
  (let [msg "系统出错了!"
        exception-info (stacktrace->string exception)
        at (moment-format)]
    (error "Exception:" exception-info)
    (when prod-mode?
      (doseq [email (system-monitoring-mail-accounts)]
        (info "send email to " email)
        (send-mail-by-template
         {:to email
          :subject (str "[wapp]-" at ":" msg)}
         "common/mail-error-notice"
         {:current-user (or (:name (*current-user-fn*)) "*未登录*")
          :at at
          :request-info (prn-str req)
          :exception-info exception-info})))
    (if (ajax? req)
      (status 500 (json (error-message msg nil exception-info)))
      (status 500 (html (view
                          "common/500"
                          {:detail-message exception-info}
                          {:title msg}))))))

(defroutes full-app-routes
  (apply routes (app-routes))
  (GET "/_lib_version" [] version)
  (route/resources "/public")
  (route/not-found *not-found-content*))

(def ^:private intern-app
  (-> full-app-routes
    (wrap-exception-handling exception-handle)
    (wrap-i18n-middleware)))

(def ^{:doc "app handler"} app
  (let [app (app-handler [intern-app])
        app (when-not-> app prod-mode? wrap-dev-helper)]
    (wrap-profile app)))

(defn init
  "服务器初始化入口"
  []
  (init-app-module!)
  (info ">>Server start! Run mode: " run-mode))

(defn start-server
  "启动服务器"
  []
  (init)
  (httpkit/run-server app {:port server-port}))
