(ns cljwtang.server
  (:refer-clojure :exclude [name sort])
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cljtang.core :refer :all]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.util.middleware :refer [app-handler]]
            [noir.response :refer [json status]]
            [taoensso.tower :as tower]
            [taoensso.tower.ring :refer [wrap-i18n-middleware]]
            [org.httpkit.server :as httpkit])
  (:require [cljwtang.core :refer :all :as cljwtang]
            [cljwtang.inject :as inject]
            [cljwtang.datatype :as datatype]
            [cljwtang.middleware :as middlewares]
            [cljwtang.utils.mail :as mailer]
            [cljwtang.env-config :as env-config]
            [cljwtang.config :as config]
            [cljwtang.request :refer [ajax?]]
            [cljwtang.response :refer [html]]
            [cljwtang.view :as view]))

(defn- exception-handle
  "统一异常处理"
  [req exception]
  (let [msg "系统出错了!"
        exception-info (stacktrace->string exception)
        at (moment-format)]
    (log/error "Exception:" exception-info)
    (when env-config/prod-mode?
      (doseq [email (config/system-monitoring-mail-accounts)]
        (log/info "send email to " email)
        (mailer/send-mail-by-template
          {:to email
           :subject (str "[wapp]-" at ":" msg)}
          "common/mail-error-notice"
          {:current-user (or (:name (inject/fn-current-user)) "*未登录*")
           :at at
           :request-info (prn-str req)
           :exception-info exception-info})))
    (if (ajax? req)
      (status 500 (json (error-message msg nil exception-info)))
      (status 500 (html (view/view
                          "common/500"
                          {:detail-message exception-info}
                          {:title msg}))))))

(defroutes app-routes
  (apply routes (inject/app-routes))
  (GET "/_lib_version" [] env-config/version)
  (route/resources "/public")
  (route/not-found inject/not-found-content))

(def ^:private intern-app
  (-> app-routes
    (middlewares/wrap-exception-handling exception-handle)
    (wrap-i18n-middleware)))

(def ^{:doc "app handler"} app
  (let [app (app-handler [intern-app])
        app (when-not-> app env-config/prod-mode? middlewares/wrap-dev-helper)]
    (middlewares/wrap-profile app)))

(defn init
  "服务器初始化入口"
  []
  (datatype/init inject/app-module)
  (log/info ">>Server start! Run mode: " env-config/run-mode))

(defn start-server
  "启动服务器"
  []
  (init)
  (httpkit/run-server app {:port env-config/server-port}))
