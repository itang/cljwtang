(ns cljwtang.datatype
  (:refer-clojure :exclude [name sort])
  (:require [cljtang.core :refer :all]))

(defprotocol Base
  (name [this] "名称")
  (description [this] "描述"))

(defprotocol Sort
  (sort [this] "排序号"))

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
  [name description routes fps menus snippets-ns bootstrap-tasks contollers]
  Base 
  (name [_] name)
  (description [_] description)
  UiModule
  (routes [_] (nil->empty routes))
  (fps [_] (nil->empty fps))
  (menus [_] (nil->empty menus))
  (snippets-ns [_] (nil->empty snippets-ns))
  (bootstrap-tasks [_] (nil->empty bootstrap-tasks))
  (contollers [_] (nil->empty contollers)))

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
