(ns cljwtang.server
  (:refer-clojure :exclude [name sort])
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl-server]
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
            [cljwtang.datatype :refer [name sort run-fn run?]]
            [cljwtang.middleware :as middlewares]
            [cljwtang.utils.mail :as mailer]
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
    (when config/prod-mode?
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
  (apply routes inject/app-routes)
  (GET "/_lib_version" [] config/version)
  (route/resources "/public")
  (route/not-found inject/not-found-content))

(def ^:private intern-app
  (-> app-routes
    (middlewares/wrap-exception-handling exception-handle)
    (wrap-i18n-middleware)))

(def ^{:doc "app handler"} app
  (let [app (app-handler [intern-app])
        app (when-not-> app config/prod-mode? middlewares/wrap-dev-helper)]
    (middlewares/wrap-profile app)))

(defn- load-i18n-dictionary []
  (if (io/resource config/i18n-config-file)
    (do
      (tower/load-dictionary-from-map-resource! config/i18n-config-file)
      (tower/set-config! [:dev-mode?] config/dev-mode?))
    (log/warn "\tNot found" config/i18n-config-file ",使用默认配置!")))

(defn- run-bootstrap-tasks []
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
        (.printStackTrace e)))))

(defn- start-nrepl-server []
  (defonce nrepl-server
    (nrepl-server/start-server :port config/nrepl-server-port))
  (log/info (str "use lein to connect nrepl server: lein repl :connect "
                 config/nrepl-server-port)))

(defn init
  "服务器初始化入口"
  []
  (log/info "Load i18n dictionary...")
  (load-i18n-dictionary)

  (log/info "Run bootstrap tasks...")
  (run-bootstrap-tasks)

  (log/info "start-nrepl-server? " config/start-nrepl-server?)
  (when config/start-nrepl-server?
    (start-nrepl-server))

  (log/info ">>Server start! Run mode: " config/run-mode))

(defn start-server
  "启动服务器"
  []
  (init)
  (httpkit/run-server app {:port config/server-port}))
