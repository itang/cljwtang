(ns cljwtang.web.core
  (:require [compojure.core :refer :all]
            [noir.request :refer [*request*]]
            [noir.session :as session]
            [noir.response :refer [json]]
            [clojurewerkz.route-one.core :as route-one]
            [cljwtang.core :as core :refer [template-engine new-funcpoint]]
            [cljwtang.template.core :as template]
            [cljwtang.utils.env :as env]))

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

(defmacro with-routes [name & body]
  `(do (def ~name (atom []))
       (let [~'_routes ~name]
         ~@body)))

(defmacro defhandler-meta [meta name args & body]
  `(do
     (route-one/defroute ~name ~(:path meta))
     (if ~(:fp-name meta)
       (def ~(symbol (str name "-fp"))
         (new-funcpoint {:name ~(:fp-name meta)
                         :url ~(:path meta)
                         :perm ~(:perm meta)})))
     (defn ~name [~'req]
       (let [{:keys ~args :or {~'req ~'req}} (:params ~'req)]
         ~@body))
     (swap! ~'_routes conj
            (case ~(:method meta)
              :get (GET ~(:path meta) ~'req ~name)
              :post (POST ~(:path meta) ~'req ~name)
              :delete (DELETE ~(:path meta) ~'req ~name)
              :put (PUT ~(:path meta) ~'req ~name)
              (ANY ~(:path meta) ~'req ~name)))
     ))

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
  (template/render-string template-engine template data))

(defn render-file
  "render template form file"
  [template-name data]
  (template/render-file template-engine template-name data))

(defn regist-helper
  "regist helper"
  [k v]
  (template/regist-helper template-engine k v))

(defn regist-tag
  "regist tags"
  [k v]
  (template/regist-tag template-engine k v (->> k name (str "end-") keyword)))

(defn template-engine-name
  "template-engine name"
  []
  (template/name template-engine))

(defn clear-template-cache!
  "clear template's cache"
  []
  (template/clear-cache! template-engine))
