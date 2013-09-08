(ns cljwtang.web.view
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cljtang.core :refer :all]
            [taoensso.tower :as tower]
            [compojure.response :as response]
            [noir.request :refer [*request*]]
            [cljwtang.core :as core]
            [cljwtang.config.app :as config]
            [cljwtang.template.core :refer [name]]
            [cljwtang.web.core :refer :all]))

(def ^:private static-context
  {:mode config/run-mode
   :is-prod-mode config/prod-mode?
   :is-dev-mode config/dev-mode?})

(defn- view-context []
  {:logined (core/*user-logined?-fn*)
   :host (config/hostaddr)
   :title (core/*app-config-fn* :platform.name "Clojure Web Platform")})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; public api
(defn template [tpl-name & [ctx]]
  (render-file tpl-name (merge static-context ctx)))

(defn view [tpl-name & [ctx]]
  (response/render (template tpl-name (merge (view-context) ctx))
                   *request*))

;; pathname 可以是sym或string
;; pathname "main/test", 对应snippetname是 main-test, 全名是snippet-main-test
;; 隐含变量 ctx
(defmacro defsnippet
  [pathname & more]
    (let [path (str pathname)
          method (str/replace path #"/" "-")
          more-num (count more)
          condition (condp = more-num
                      2 (first more)
                      true)
          context (condp = more-num
                    1 (first more)
                    2 (second more))]
      `(defn ~(symbol method) [args# context#]
         (when-let [~'ctx ~condition]
           (template (str "snippets/" ~path) ~context)))))
