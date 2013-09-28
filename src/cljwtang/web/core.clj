(ns cljwtang.web.core
  (:require [clojure.tools.macro :refer [name-with-attributes]]
            [cljtang.lib :refer :all]
            [compojure.core :refer :all]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [noir.request :refer [*request*]]
            [noir.session :as session]
            [noir.validation]
            [cemerick.friend :as friend]
            [clojurewerkz.route-one.core :as route-one]
            [cljwtang.core :as core :refer [template-engine new-funcpoint]]
            [cljwtang.template.core :as template]
            [cljwtang.utils.env :as env]))

(defn message
  "消息map"
  [success message & [data detailMessage type]]
  (let [message (or message "")
        data (or data {})
        detailMessage (or detailMessage "")
        type (or type (if success :success :error))]
    {:success success
     :message message
     :data data
     :detailMessage detailMessage
     :type type}))

(defn success-message
  "success消息map"
  [pmessage & [data detailMessage]]
  (message true pmessage data detailMessage))

(defn failture-message
  "failture消息map"
  [pmessage & [data detailMessage]]
  (message false pmessage data detailMessage))

(def ^{:doc "error消息map"}
  error-message failture-message)

(defn info-message
  "info消息map"
  [pmessage & [data detailMessage]]
  (message true pmessage data detailMessage :info))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defdynamic routes nil)

(defdynamic routes-config nil)

(defmacro with-routes [name opts & body]
  (let [routes-config (if (string? opts) {:path opts} opts)]
    (set-routes-config! routes-config) ;; 会存在并发问题吗? 
    `(binding [*routes* (atom [])]
       ~@body
       (def ~name (apply routes @*routes*))
       (set-routes-config! nil))))

(defn -un-quote [x]
  (if (and (seq? x) (= 'quote (first x)))
    (fnext x)
    x))

(defn- with-validates [success meta]
  (let [validate (-un-quote (:validate meta))
        on-validate-error (-un-quote (or (:on-validate-error meta)
                                         #(throw (Exception. "validate error"))))]
    [`(do
        ~validate
        (if (noir.validation/errors?)
          (do
            (flash-msg (failture-message "验证错误" @noir.validation/*errors*))
            (flash-post-params (:params *request*))
            ~on-validate-error)
          (do ~@success)))]))

(defn- with-authenticated [body]
  [`(friend/authenticated (do ~@body))])

(defn- get-prop [meta f]
  (f (merge (get-routes-config) meta)))

(defn- with-authorized [body perm]
  (let [authorize (if (coll? perm) perm #{perm})]
    [`(friend/authorize ~authorize (do ~@body))]))

;;@see http://blog.fnil.net/index.php/archives/27
(defn- make-handler [name args body meta]
  (let [handler-fn
        `(fn [~'req]
           (let [{:keys ~args :or {~'req ~'req}} (:params ~'req)]
             ~@(let [validated (boolean (:validate meta))
                     authenticated (get-prop meta :auth)
                     perm (get-prop meta :perm)]
                 (-> body
                   (?> validated with-validates meta)
                   (?> authenticated with-authenticated)
                   (?> perm with-authorized perm)))))]
    `(def ~name ~(if (boolean (:anti-forgery meta))
                   `(wrap-anti-forgery ~handler-fn)
                   handler-fn))))

(defn- make-compojure-route [method path handler]
  (case method
    :get `(GET ~path ~'req ~handler)
    :post `(POST ~path ~'req ~handler)
    :delete `(DELETE ~path ~'req ~handler)
    :put `(PUT ~path  ~'req ~handler)
    :head `(HEAD ~path  ~'req ~handler)
    :options `(OPTIONS ~path  ~'req ~handler)
    :patch `(PATCH ~path  ~'req ~handler)
    `(ANY ~path ~'req ~handler)))

(defn- make-funcpoint [name fp]
  `(def ~(symbol (str name "-fp"))
     (new-funcpoint ~fp)))

(defn- get-route-info [name meta]
  (let [method (or (:method meta)
                   (->> meta
                     keys
                     (filter #{:get :post :delete :put :head :options :patch :any})
                     first))
        path (or (:path meta) (get meta method) (str "/" name))]
    (when method
      {:method method :path path})))

(defn- get-fp-info [name path meta]
  (when-let [fp-name (or (:fp meta) (:fp-name meta))]
    {:name (when-> fp-name true? name)
     :url path
     :perm (get-prop meta :perm)
     :description (or (:description meta) (:doc meta))
     :module (:module meta)}))

(defn- make-routes [name meta]
  (when-let [route-info (get-route-info name meta)]
    (let [p (gensym)
          method (:method route-info)
          path (:path route-info)
          path (str (:path (get-routes-config)) path)
          fp-info (get-fp-info name path meta)]
    `(let [~p ~path]
        (route-one/defroute ~name ~p)
        ~(when fp-info (make-funcpoint name fp-info))
        (some-> *routes* (swap! conj ~(make-compojure-route method p name)))))))

(defmacro defhandler
  "define handler"
  {:arglists '([name doc-string? attr-map? [params*] body])}
  [name & args]
  (let [[name attrs] (name-with-attributes name args)
        meta         (meta name)
        args         (first attrs)
        body         (next attrs)]
    `(do
       ~(make-handler name args body meta)
       ~(make-routes name meta))))
