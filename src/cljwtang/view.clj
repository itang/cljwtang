(ns cljwtang.view
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as str]
            [cljtang.core :refer :all]
            [taoensso.tower :as tower]
            [cljwtang.inject :as inject]
            [cljwtang.core :refer :all]
            [cljwtang.config :as config]
            [cljwtang.template.core :refer [name]]))

(def ^:private te-name (name inject/*template-engine*))
(defn- selmer? []
  (= :selmer te-name))

(def ^:private static-context
  {:mode config/run-mode
   :host (config/hostaddr)
   :is-prod-mode config/prod-mode?
   :is-dev-mode config/dev-mode?})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; init

(defn- init []
  (println "view init ...")
  (regist-helper :i18n 
                 (fn [args context]
                   (tower/t (-> args first keyword))))
  (regist-helper :chan-active
              (fn [args context]
                (when (= (first args) (:channel context))
                  "active"))))

(init)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; public api
(defn template [tpl-name & [ctx]]
  (render-file tpl-name (merge static-context ctx)))

;;; view

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
