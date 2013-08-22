(ns cljwtang.view
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as str]
            [cljtang.core :refer :all]
            [taoensso.tower :as tower]
            [cljwtang.inject :as inject]
            [cljwtang.core :refer :all]
            [cljwtang.config :as config]
            [cljwtang.template.core :refer [name]]))

(def ^:private more-default "")

(def ^:dynamic *more-js* more-default)

(def ^:dynamic *more-css* more-default)

(def _s (name inject/*template-engine*))
(defn selmer? []
  (= "Selmer" _s))

(println "selmer?" (selmer?))

(def ^:private static-context
  {:mode run-mode
   :host config/hostaddr
   :is-prod-mode prod-mode?
   :is-dev-mode dev-mode?})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- append-line [target line]
  (if target 
    (str target \newline line)
    line))

(defmacro ^:private with [v]
    `(fn [src#] 
       (when (not= ~v more-default) ;; has binding
         (set! ~v (append-line ~v src#)) nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; init

(defn- init []
  (println "view init ...")
  (regist-tag :with-js (with *more-js*))
  (regist-tag :with-css (with *more-css*))
  (regist-tag :i18n #(tower/t (keyword %)))
  (regist-tag :chan-active
              ^{:stencil/pass-context true}
              (fn [x ctx]
                (when (= x (:channel ctx))
                  "active"))))
(init)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; public api

(defn render-template [tpl-name & [ctx]]
  (render-file tpl-name ctx))

(defn template [tpl-name & [ctx]]
  (render-template tpl-name (merge static-context ctx)))

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
      `(defn ~(symbol method) [& more#]
         (when-let [~'ctx ~condition]
           (template (str "snippets/" ~path) ~context)))))

;;; view

(defn- view-context []
  {:logined (inject/fn-user-logined?)
   :more-js *more-js*
   :morejs *more-js*
   :morecss *more-css*
   :more-css *more-css*})

(defn view-template [tpl-name & [ctx]]
  (template tpl-name (merge (view-context) ctx)))

(defn- with-layout [layout-name body & [head]]
  (if (selmer?)
    body
    (view-template
      (str "layouts/" layout-name)
      {:title (or (:title head)
                  (inject/fn-app-config :platform.name "Clojure Web Platform"))
       :content body})))

(defn layout [body & [head]]
  (with-layout "layout-main" body head))

(defn layout-base [body & [head]]
  (with-layout "layout-base" body head))

(defn layout-view [name & [vc lc]]
  (layout (view-template name vc) lc))

(defn layout-base-view [name & [vc lc]]
  (layout-base (view-template name vc) lc))
