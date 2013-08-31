(ns cljwtang.view
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as str]
            [cljtang.core :refer :all]
            [taoensso.tower :as tower]
            [cljwtang.inject :as inject]
            [cljwtang.core :refer :all]
            [cljwtang.env-config :as env-config]
            [cljwtang.config :as config]
            [cljwtang.template.core :refer [name]]))

(def ^:private static-context
  {:mode env-config/run-mode
   :host (config/hostaddr)
   :is-prod-mode env-config/prod-mode?
   :is-dev-mode env-config/dev-mode?})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; public api
(defn template [tpl-name & [ctx]]
  (render-file tpl-name (merge static-context ctx)))

(defn- view-context []
  {:logined (inject/fn-user-logined?)
   :title (inject/fn-app-config :platform.name "Clojure Web Platform")})

(defn view [tpl-name & [ctx]]
  (template tpl-name (merge (view-context) ctx)))

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
