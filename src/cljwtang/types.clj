(ns cljwtang.types
  (:refer-clojure :exclude [name sort])
  (:require [clojure.tools.logging :as log]
            [cljtang.core :refer :all]
            [cljwtang.utils.util :refer [nil->empty]]))

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

(defprotocol RegistModule
  (regist-module [this module] "注册模块"))

(defprotocol Modules
  (modules [this] "获取所有模块"))

(defprotocol BootstrapTask
  (run? [this] "是否执行")
  (run-fn [this] "执行体"))

(defprotocol ElementAttrs
  (classname [this] "css class名称")
  (target [this] "打开方式"))

(defprotocol FuncPoint
  (url [this] "URL")
  (perm [this] "权限")
  (module [this] "所属模块"))

;; 菜单项
(defprotocol Menu
  (id [this] "ID")
  (funcpoint [this] "对应功能点")
  (attrs [this] "属性")
  (children [this] "子级")
  (parent [this] "父级"))

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

(defrecord FuncPointRecord
    [name url perm module description]
  Base
  (name [_] name)
  (description [_] description)
  FuncPoint
  (url [_] url)
  (perm [_] perm)
  (module [_] module))

(defrecord ElementAttrsRecord
    [classname target]
  ElementAttrs
  (classname [_] classname)
  (target [_] target))

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

(defn- flatten-m [modules f]
  (->> modules (map f) flatten))

(defrecord AppModule [name description before-init after-init modules]
  Base
  (name [_] name)
  (description [_] description)
  Module
  (init [this]
    (log/info (cljwtang.types/name this) "init...")
    (when before-init (before-init this))
    (doseq [m @modules]
      (log/info "module" (cljwtang.types/name m) "init...")
      (cljwtang.types/init m))
    ;; bootstrap tasks
    (doseq [task (sort-by sort (bootstrap-tasks this))]
      (try
        (let [name (cljwtang.types/name task)
              f (run-fn task)
              run? ((run? task))]
          (log/info "run" name "task?" run?)
          (when run?
            (log/info name " run")
            (f)))
        (catch Exception e
          (.printStackTrace e))))
    (when after-init (after-init this))
    (log/info (cljwtang.types/name this) "init finished."))
  (destroy [this]
    (doseq [m @modules] (destroy m))
    (println (cljwtang.types/name this) " destory"))
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

(extend-protocol UiModule
  nil
  (routes [_] [])
  (fps [_] [])
  (menus [_] [])
  (snippets-ns [_] [])
  (bootstrap-tasks [_] [])
  (contollers [_] []))
