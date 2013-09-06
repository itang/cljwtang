(ns cljwtang.web.core
  (:require [clojure.tools.macro :refer [name-with-attributes]]
            [compojure.core :refer :all]
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

;;;; validate fn 规范
;;;;  (validate-fn (:params *request*)) => {:user ["error1"]}
(defn- with-validates [validates-fn failture success]
  [`(let [~'find (atom  false)
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
        (do ~@success)))])

(defn- m-handler [name args validates-fn failture body]
 `(defn ~name [~'req]
    (let [{:keys ~args :or {~'req ~'req}} (:params ~'req)]
      ~@(if validates-fn
         (with-validates validates-fn failture body)
         body))))

(defn- m-method [method path handler]
  (case method
    :get `(GET ~path ~'req ~handler)
    :post `(POST ~path ~'req ~handler)
    :delete `(DELETE ~path ~'req ~handler)
    :put `(PUT ~path  ~'req ~handler)
    :head `(HEAD ~path  ~'req ~handler)
    :options `(OPTIONS ~path  ~'req ~handler)
    :patch `(PATCH ~path  ~'req ~handler)
    `(ANY ~path ~'req ~handler)))

(defn- m-fc [name url perm]
  `(def ~(symbol (str name "-fp"))
     (new-funcpoint {:name ~name
                     :url ~url
                     :perm ~perm})))

(defn- m-routes [name path meta]
  `(do
     (route-one/defroute ~name ~path)
     ~(when (:fp-name meta)
        (m-fc name path (:perm meta)))
     (swap! ~'_routes conj ~(m-method (:method meta) path name))))

(defmacro defhandler
  "define handler"
  {:arglists '([name doc-string? attr-map? [params*] body])}
  [name & args]
  (let [[name attrs] (name-with-attributes name args)
        meta         (meta name)
        path         (:path meta)
        validates-fn (:validates-fn meta)
        failture     (or (:failture meta) #(throw (Exception. "validate error")))
        args         (first attrs)
        body         (next attrs)]
    `(do
       ~(m-handler name args validates-fn failture body)
       ~(if path (m-routes name path meta)))))

;;@see http://blog.fnil.net/index.php/archives/27
#_(defmacro defhandler
  [name args & body]
  `(defn ~name [~'req]
     (let [{:keys ~args :or {~'req ~'req}} (:params ~'req)]
       ~@body)))

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
