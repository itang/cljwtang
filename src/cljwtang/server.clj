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
            [taoensso.tower :refer [t set-config!] :as tower]
            [taoensso.tower.ring :refer [wrap-i18n-middleware]]
            [org.httpkit.server :as httpkit])
  (:require [cljwtang.core :refer :all :as cljwtang]
            [cljwtang.inject :as inject]
            [cljwtang.view :refer :all]
            [cljwtang.datatype :refer [name sort run-fn run?]]
            [cljwtang.config :as config]
            [cljwtang.request :refer [ajax?]]
            [cljwtang.response :refer [html]]
            [cljwtang.utils.mail :refer [send-mail]]
            [cljwtang.middleware :refer [wrap-dev-helper 
                                         wrap-profile
                                         wrap-view
                                         wrap-exception-handling]]))

(defn- exception-handle
  "统一异常处理"
  [req exception]
  (let [msg "系统出错了!"
        exception-info (stacktrace->string exception)
        at (moment-format)]
    (log/error "Exception:" exception-info)
    (if prod-mode?
      (doseq [email (config/system-monitoring-mail-accounts)]
        (log/info "send email to " email)
        (send-mail
          {:to email
           :subject (str "[wapp]-" at ":" msg)
           :body [{:type
                   "text/html; charset=utf-8"
                   :content 
                   (template "common/mail-error-notice"
                             {:current-user (or (:name (inject/fn-current-user)) "*未登录*")
                              :at at
                              :request-info (prn-str req)
                              :exception-info exception-info})}]})))
    (if (ajax? req)
      (status 500 (json (error-message msg nil exception-info)))
      (status 500 (html (layout-view
                          "common/500"
                          {:detail-message exception-info}
                          {:title msg}))))))

(defroutes app-routes
  (apply routes inject/app-routes)
  (GET "/_lib_version" [] cljwtang/version)
  (route/resources "/public")
  (route/not-found inject/not-found-content))

(def ^:private intern-app
  (-> app-routes
    (wrap-view)
    (wrap-exception-handling exception-handle)
    (wrap-i18n-middleware)))

(def ^{:doc "app handler"} app
  (let [app (app-handler [intern-app])
        app (when-not-> app prod-mode? wrap-dev-helper)]
    (wrap-profile app)))

(defn init
  "服务器初始化入口"
  []
  (letfn [(load-i18n-dictionary []
            (if (io/resource config/i18n-config-file)
              (do
                (tower/load-dictionary-from-map-resource! config/i18n-config-file)
                (set-config! [:dev-mode?] dev-mode?))
              (log/warn "\tNot found" config/i18n-config-file ",使用默认配置!")))
          (run-bootstrap-tasks []
            (doseq [task (sort-by sort inject/bootstrap-tasks)]
              (try
                (let [name (name task)
                      f (run-fn task)
                      run? ((run? task))]
                  (log/info "Try run " name)
                  (log/info "run?" run?)
                  (when run?
                    (f)
                    (log/info "finish" name)))
                (catch Exception e
                  (.printStackTrace e)))))]
    (log/info "Load i18n dictionary...")
    (load-i18n-dictionary)
    (log/info "Run bootstrap tasks...")
    (run-bootstrap-tasks)
    (log/info ">>Server start! Run mode: " run-mode)))

(defn start-server
  "启动服务器"
  []
  (init)
  (httpkit/run-server app {:port config/server-port}))
