(ns cljwtang.core
  (:require [noir.request :refer [*request*]]
            [noir.session :as session]
            [noir.response :refer [json]]
            [cljwtang.inject :as inject]
            [cljwtang.template.core :as template]
            [cljwtang.utils.env :as env]))

(def ^{:doc "库版本信息"}
  version "cljwtang-0.1.0-SNAPSHOT")

(def ^{:doc "获取应用配置"}
  app-config inject/fn-app-config)

(def ^{:doc "应用运行模式(开发或生产)"}
  run-mode
  (if (or (env/env-config :wapp-no-dev) (env/env-config :lein-no-dev))
    "prod"
    "dev"))

(def ^{:doc "应用是在生产模式下运行?"}
  prod-mode? (= run-mode "prod"))

(def ^{:doc "应用是在开发模式下运行?"}
  dev-mode? (= run-mode "dev"))

(defn message
  "消息map"
  [success pmessage & [data detailMessage ptype]]
  (let [pmessage (or pmessage "")
        data (or data {})
        detailMessage (or detailMessage "")
        ptype (or ptype (if success :success :error))]
    {:success success
     :message pmessage
     :data data
     :detailMessage detailMessage
     :type ptype}))

(def ^{:doc "消息map -> JSON"}
  json-message (comp json message))

(defn success-message
  "success消息map"
  [pmessage & [data detailMessage]]
  (message true pmessage data detailMessage))

(def ^{:doc "success消息map -> JSON"}
  json-success-message (comp json success-message))

(defn failture-message
  "failture消息map"
  [pmessage & [data detailMessage]]
  (message false pmessage data detailMessage))

(def ^{:doc "failture消息map -> JSON"}
  json-failture-message (comp json failture-message))

(def ^{:doc "error消息map"}
  error-message failture-message)

(def ^{:doc "error消息map -> JSON"}
  json-error-message (comp json error-message))

(defn info-message
  "info消息map"
  [pmessage & [data detailMessage]]
  (message true pmessage data detailMessage :info))

(def ^{:doc "info消息map -> JSON"}
  json-info-message (comp json info-message))

(defn flash-msg
  "获取或设置flash msg"
  ([]
    (session/flash-get :msg))
  ([msg]
    (session/flash-put! :msg msg)))

(defn flash-post-params
  "获取或设置flash post params"
  ([]
    (session/flash-get :post-params))
  ([msg]
    (session/flash-put! :post-params msg)))

(defn postback-params
  "收集参数(来自请求参数和本页面post提交的)"
  []
  (merge (:params *request*) (flash-post-params)))

;;@see http://blog.fnil.net/index.php/archives/27
(defmacro defhandler
  [name args & body]
  `(defn ~name [~'req]
     (let [{:keys ~args :or {~'req ~'req}} (:params ~'req)]
       ~@body)))

;;;; validate fn 规范
;;;;  (validate-fn (:params *request*)) => {:user ["error1"]}
(defmacro with-validates [validates-fn success failture]
  `(let [~'find (atom  false)
         ~'fs (atom ~validates-fn)
         ~'ret (atom nil)
         ~'p (:params *request*)]
     (while (and (not @~'find)
                 (not (empty? @~'fs)))
       (let [~'f (first @~'fs)
             ~'r (~'f ~'p)]
         (if-not (empty? ~'r)
           (do (reset! ~'find true) (reset! ~'ret ~'r))
           (do (reset! ~'fs (next @~'fs))))))
     (if-not (empty? @~'ret)
       (do
         (flash-msg (failture-message "验证错误" @~'ret))
         (flash-post-params ~'p)
         ~failture)
       ~success)))

(defmacro defhandler-with-validates
  [handler args validates-fn &
   {:keys [success failture]
    :or {failture
         #(throw (Exception. "validate error"))}}]
  `(defhandler ~handler [~@args]
     (with-validates ~validates-fn ~success ~failture)))

;;; for template
(defn render-string
  "render template from string"
  [template data]
  (template/render-string inject/*template-engine* template data))

(defn render-file
  "render template form file"
  [template-name data]
  (template/render-file inject/*template-engine* template-name data))

(defn regist-helper
  "regist helper"
  [k v]
  (template/regist-helper inject/*template-engine* k v))

(defn regist-tag
  "regist tags"
  [k v]
  (template/regist-tag inject/*template-engine* k v (->> k name (str "end-") keyword)))

(defn template-engine-name
  "template-engine name"
  []
  (template/name inject/*template-engine*))

(defn clear-template-cache!
  "clear template's cache"
  []
  (template/clear-cache! inject/*template-engine*))
