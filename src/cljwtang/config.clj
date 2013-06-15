(ns cljwtang.config
  (:require [me.raynes.fs :refer [file home exists? mkdirs]]
            [cljwtang.inject :refer [fn-app-config]]
            [cljwtang.core :refer [env-config]]))

(def ^{:doc "web服务端口"}
  server-port (or (env-config :wapp-server-port) 3000))

(defn hostname []
  (fn-app-config :platform.host "localhost"))

(defn host []
  (str (hostname) (when (= 80 server-port) (str ":" server-port))))

(def hostaddr host)

(def ^{:doc "i18n 配置文件"}
  i18n-config-file "i18n-config.clj")

(defn mail-server
  "stmp 服务器配置"
  []
  (fn-app-config :platform.mail-server
                 {:host "smtp.exmail.qq.com"
                  :port 465 
                  :user "support@traup.com"
                  :pass "traup_2012"
                  :ssl :yes}))

(defn mail-vendors-out-rule
  "邮件服务商(规则之外的)"
  []
  (fn-app-config :platform.mail-vendors-out-rule
                 {"gmail.com" "mail.google.com"
                  "139.com" "mail.10086.cn"}))

(defn system-monitoring-mail-accounts
  "系统监控对应的邮件帐号"
  []
  (fn-app-config :platform.system-monitoring-mail-account
                 ["livetang@qq.com"]))

(defn appdata-dir
  "应用存放数据的目录"
  []
  (let [root-dir (home) ;;TODO 可配置
        base-dir (str "cljwtang-data")
        data-dir (.getAbsolutePath ^java.io.File (file root-dir base-dir))
        data-dir (fn-app-config :platform.appdata-dir data-dir)]
    (when-not (exists? data-dir)
      (mkdirs data-dir))
    data-dir))
