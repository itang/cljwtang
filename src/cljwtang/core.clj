(ns cljwtang.core
  (:refer-clojure :exclude [name sort])
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cljtang.core :refer :all]
            [taoensso.tower :as tower]
            [korma.db :refer [defdb h2]]
            [clojure.tools.nrepl.server :as nrepl-server]
            [cljwtang.utils.env :as env]
            [cljwtang.template.core :as template]
            [cljwtang.template.selmer :as selmer]
            [cljwtang.config.core :as config]))

(defdynamic ^{:doc "应用配置函数"} app-config-fn env/env-config)

(defdynamic user-logined?-fn (constantly false))

(defdynamic current-user-fn (constantly nil))

(defdynamic db-config
  (h2 {:subname "~/cljwtang_dev;AUTO_SERVER=TRUE"
       :user "sa"
       :password ""})
  :after-set
  (defdb latest-db (get-db-config)))

(defdb default-db (get-db-config))

(defdynamic not-found-content "Not Found")

(defonce template-engine
  (selmer/new-selmer-template-engine))

(defprotocol Base
  (name [this] "名称")
  (description [this] "描述"))

(defprotocol Sort
  (sort [this] "排序号"))

(defprotocol Module
  (init [this] "初始化")
  (destroy [this] "销毁"))

(defprotocol UiModule
  (routes [this] "路由表")
  (fps [this] "功能点")
  (menus [this] "菜单")
  (snippets-ns [this] "Snippets 名字空间")
  (bootstrap-tasks [this] "系统启动时任务")
  (contollers [this] "客户端Controllers"))

(defn- nil->empty [coll]
  (nil-> coll []))

(defrecord UiModuleRecord
    [name description routes fps menus snippets-ns bootstrap-tasks contollers init destroy]
  Base
  (name [_] name)
  (description [_] description)
  Module
  (init [this]
    (when init
      (init this)))
  (destroy [this]
    (when destroy
      (destroy this)))
  UiModule
  (routes [_] (nil->empty routes))
  (fps [_] (nil->empty fps))
  (menus [_] (nil->empty menus))
  (snippets-ns [_] (nil->empty snippets-ns))
  (bootstrap-tasks [_] (nil->empty bootstrap-tasks))
  (contollers [_] (nil->empty contollers)))

(defprotocol RegistModule
  (regist-module [this module] "注册模块"))

(defn new-ui-module
  ([]
     (new-ui-module {}))
  ([m]
     (let [es-keys
           [:routes :fps :menus :snippets-ns :bootstrap-tasks :contollers]
           m
           (loop [r m keys es-keys]
             (if-not keys
               r
               (recur (update-in r [(first keys)] nil->empty) (next keys))))]
       (map->UiModuleRecord m))))

(defprotocol BootstrapTask
  (run? [this] "是否执行")
  (run-fn [this] "执行体"))

(defrecord BootstrapTaskRecord
    [name description sort run? run-fn]
  Base
  (name [_] name)
  (description [_] description)
  Sort
  (sort [_] sort)
  BootstrapTask
  (run? [_] run?)
  (run-fn [_] run-fn))

(defn new-bootstrap-task [m]
  (map->BootstrapTaskRecord m))

(defprotocol FuncPoint
  (url [this] "URL")
  (perm [this] "权限")
  (module [this] "所属模块"))

(defrecord FuncPointRecord
    [name url perm module description]
  Base
  (name [_] name)
  (description [_] description)
  FuncPoint
  (url [_] url)
  (perm [_] perm)
  (module [_] module))

(defn new-funcpoint [m]
  (map->FuncPointRecord m))

(defprotocol ElementAttrs
  (classname [this] "css class名称")
  (target [this] "打开方式"))

(defrecord ElementAttrsRecord
    [classname target]
  ElementAttrs
  (classname [_] classname)
  (target [_] target))

(defn new-element-attrs [m]
  (map->ElementAttrsRecord m))

;; 菜单项
(defprotocol Menu
  (id [this] "ID")
  (funcpoint [this] "对应功能点")
  (attrs [this] "属性")
  (children [this] "子级")
  (parent [this] "父级"))

(defrecord MenuRecord
    [id name funcpoint attrs children parent sort description]
  Base
  (name [_] name)
  (description [_] description)
  Sort
  (sort [_] sort)
  Menu
  (id [_] id)
  (funcpoint [_] funcpoint)
  (attrs [_] attrs)
  (children [_] children)
  (parent [_] parent))

(defn new-menu [m]
  (let [m (if (:sort m) m (assoc m :sort 100))
        m (if (:id m) m (assoc m :id (:name m)))
        attrs (:attrs m)
        m (if attrs
            (if (instance? ElementAttrsRecord attrs)
              m
              (assoc m :attrs (new-element-attrs attrs)))
            (assoc m :attrs (new-element-attrs {:classname "icol-user"})))
        fp (:funcpoint m)
        m (if-not (instance? FuncPointRecord fp)
            (if fp
              (assoc m :funcpoint (new-funcpoint fp))
              m)
            m)]
    (map->MenuRecord m)))

(defn maps->menus [maps]
  (map new-menu maps))

(defn- menus-by-parent [menus parent]
  (let [parent-id (:id parent)]
    (filter #(= parent-id (:parent %)) menus)))

(defn- menu1 [parent menus]
  (let [children (sort-by :sort (menus-by-parent menus parent))]
    (assoc parent :children children )))

(defn menu-tree [& menus]
  (let [menus (flatten menus)
        parents (sort-by :sort (filter #(nil? (:parent %)) menus))]
    (for [parent parents]
      (menu1 parent menus))))

(defn- flatten-m [modules f]
  (->> modules (map f) flatten))

(defprotocol Modules
  (modules [this] "获取所有模块"))

(defrecord AppModule [name description before-init after-init modules]
  Base
  (name [_] name)
  (description [_] description)
  Module
  (init [this]
    (log/info (cljwtang.core/name this) "init...")
    (when before-init (before-init this))
    (doseq [m @modules]
      (log/info "module" (cljwtang.core/name m) "init...")
      (cljwtang.core/init m))
    ;; bootstrap tasks
    (doseq [task (sort-by sort (bootstrap-tasks this))]
      (try
        (let [name (cljwtang.core/name task)
              f (run-fn task)
              run? ((run? task))]
          (log/info "run" name "task?" run?)
          (when run?
            (log/info name " run")
            (f)))
        (catch Exception e
          (.printStackTrace e))))
    (when after-init (after-init this))
    (log/info (cljwtang.core/name this) "init finished."))
  (destroy [this]
    (doseq [m @modules] (destroy m))
    (println (cljwtang.core/name this) " destory"))
  Modules
  (modules [_] @modules)
  RegistModule
  (regist-module [this module]
    (swap! modules conj module))
  UiModule
  (routes [_]
    (flatten-m @modules routes))
  (fps [_]
    (flatten-m @modules fps))
  (menus [_]
    (flatten-m @modules menus))
  (snippets-ns [_]
    (flatten-m @modules snippets-ns))
  (bootstrap-tasks [_]
    (flatten-m @modules bootstrap-tasks))
  (contollers [_]
    (flatten-m @modules contollers)))

(defn new-app-module
  ([name description before-init after-init]
     (new-app-module name description before-init after-init (atom [])))
  ([name description before-init after-init modules]
     (->AppModule name description before-init after-init modules)))


(defn- load-snippets
  "在指定名字空间加载所有的snippets"
  [nss]
  (doseq [n nss] (require n))
  (let [helpers (->> nss
                     (map ns-publics)
                     (map (partial map
                                   (fn [[k v]]
                                     [(keyword (str "snippet-" k)) (var-get v)])))
                     (flatten)
                     (apply hash-map))]
    (doseq [[k v] helpers]
      (template/regist-helper template-engine k v))))

(def ^{:doc "应用主模块"} app-module
  (new-app-module
   "cljwtang"
   "cljwtang web app"
   nil
   (fn [m] (load-snippets (snippets-ns app-module)))))

(defn app-sub-modules
  "获取应用的所有子模块"
  []
  (modules app-module))

(defn regist-modules! [& modules]
  (doseq [m modules] (regist-module app-module m)))

(defn app-routes []
  (routes app-module))

(defn app-bootstrap-tasks []
  (bootstrap-tasks app-module))

(defn app-menus []
  (menu-tree (menus app-module)))

(defn app-snippet-ns []
  (snippets-ns app-module))

(defn- load-i18n-dictionary []
  (if (io/resource config/i18n-config-file)
    (do
      (tower/load-dictionary-from-map-resource! config/i18n-config-file)
      (tower/set-config! [:dev-mode?] config/dev-mode?))
    (log/warn "\tNot found" config/i18n-config-file ",使用默认配置!")))

(defn- tower-module []
  (new-ui-module
   {:name "tower"
    :init (fn [m]
            (load-i18n-dictionary))}))

(defn- start-nrepl-server []
  (defonce nrepl-server
    (nrepl-server/start-server :port config/nrepl-server-port))
  (log/info (str "use lein to connect nrepl server: lein repl :connect "
                 config/nrepl-server-port)))

(defn- nrepl-module []
  (new-ui-module
   {:name "nrepl"
    :init (fn [m] (start-nrepl-server))}))

(defn- cljwtang-view-init []
  (log/info "regist-helper" "i18n")
  (template/regist-helper template-engine
                          :i18n
                          (fn [args context]
                            (tower/t (-> args first keyword))))
  (log/info "regist-helper" "chan-active")
  (template/regist-helper template-engine
                          :chan-active
                          (fn [args context]
                            (when (= (first args) (:channel context))
                              "active"))))

(defn- cljwtang-view-module []
  (new-ui-module
   {:name "cljwtang-view"
    :init (fn [m] (cljwtang-view-init))}))

(defn init-app-module! []
  (init app-module))

(regist-modules!
 (tower-module)
 (nrepl-module)
 (cljwtang-view-module))
