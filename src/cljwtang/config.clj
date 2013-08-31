(ns cljwtang.config
  (:require [me.raynes.fs :refer [file home exists? mkdirs]]
            [cljwtang.inject :refer [fn-app-config]]
            [cljwtang.utils.env :as env]
            [cljwtang.env-config :refer :all]))

(defn hostname
  "主机名"
  []
  (fn-app-config :platform.host "localhost"))

(defn hostaddr
  "主机地址: 主机名:端口"
  []
  (str (hostname) (when (not= 80 server-port) (str ":" server-port))))

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
  (let [file-path #(.getAbsolutePath ^java.io.File %)
        data-dir (fn-app-config :platform.appdata-dir
                             (file-path (file (home) ".cljwtang/data")))]
    (when-not (exists? data-dir)
      (mkdirs data-dir))
    data-dir))
