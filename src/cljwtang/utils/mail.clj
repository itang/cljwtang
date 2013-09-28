(ns cljwtang.utils.mail
  (:require [clojure.core.async :refer [go]]
            [clojure.string :as string]
            [cljtang.lib :refer :all]
            [postal.core :as postal]
            [cljwtang.config.app :as config]
            [cljwtang.web.view :as view]))

(defn send-mail
  "发送邮件(使用默认服务器配置)."
  [msg & {:keys [async?] :or {async? true}}]
  (let [f (partial postal/send-message (config/mail-server))
        msg (if-not (:from msg)
              (assoc msg :from (:user (config/mail-server)))
              msg)]
    (if async?
      (go (f msg))
      (f msg))))

(defn send-mail-by-template
  "发送邮件(使用默认服务器配置)."
  [msg template-name ctx & more]
  (let [msg (merge {:body [{:type  "text/html; charset=utf-8"
                            :content (view/template template-name ctx)}]}
                   msg)]
    (apply send-mail msg more)))

(defn mail-vendor-by-email-account
  "通过邮件帐号获取邮件服务入口"
  [email]
  (let [host (-> email (string/split #"@") second)]
    (when-not (empty? host)
      (or (get (config/mail-vendors-out-rule) host)
          (str "mail." host)))))
