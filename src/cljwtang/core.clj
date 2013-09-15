(ns cljwtang.core
  (:refer-clojure :exclude [name sort])
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [bultitude.core :as bultitude]
            [cljtang.core :refer :all]
            [taoensso.tower :as tower]
            [korma.db :refer [defdb h2]]
            [clojure.tools.nrepl.server :as nrepl-server]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [cljwtang.utils.env :as env]
            [cljwtang.template.core :as template]
            [cljwtang.template.selmer :as selmer]
            [cljwtang.config.core :as config]
            [cljwtang.types :as types]
            [cljwtang.utils.util :refer [nil->empty]])
  (:import [cljwtang.types ElementAttrsRecord FuncPointRecord]))

(defdynamic ^{:doc "应用主模块"} app-module nil)

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

(def ^{:constant true} default-module-sort 100)

(defn new-ui-module
  ([]
     (new-ui-module {}))
  ([m]
     (let [m (merge {:sort default-module-sort} m)
           es-keys
           [:routes :fps :menus :snippets-ns :bootstrap-tasks :contollers]
           m
           (loop [r m keys es-keys]
             (if-not keys
               r
               (recur (update-in r [(first keys)] nil->empty) (next keys))))]
       (types/map->UiModuleRecord m))))

(defn new-funcpoint [m]
  (types/map->FuncPointRecord m))

(defn new-bootstrap-task [m]
  (types/map->BootstrapTaskRecord m))

(defn new-element-attrs [m]
  (types/map->ElementAttrsRecord m))

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
    (types/map->MenuRecord m)))

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

(defn new-app-module
  ([name description before-init after-init]
     (new-app-module name description before-init after-init (atom [])))
  ([name description before-init after-init modules]
     (types/->AppModule name description before-init after-init modules)))

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

(defn app-sub-modules
  "获取应用的所有子模块"
  []
  (types/modules *app-module*))

(defn init-app-module! []
  (types/init *app-module*))

(defn regist-modules! [& modules]
  (doseq [m modules] (types/regist-module *app-module* m)))

(defn app-routes []
  (types/routes (get-app-module)))

(defn app-bootstrap-tasks []
  (types/bootstrap-tasks *app-module*))

(defn app-menus []
  (menu-tree (types/menus *app-module*)))

(defn app-snippet-ns []
  (types/snippets-ns *app-module*))

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
                              "active")))
  (log/info "regist-helper anti-forgery-field")
  (template/regist-helper template-engine
                          :anti-forgery-field
                          (fn [args context]
                            (anti-forgery-field))))

(defn- cljwtang-view-module []
  (new-ui-module
   {:name "cljwtang-view"
    :init (fn [m] (cljwtang-view-init))}))

(defn- auto-scan-modules
  "扫描获取所有应用模块
   规则: (var-get 'prefixns.module/module)"
  []
  (let [ms (->> (bultitude/namespaces-on-classpath)
            (filter #(.endsWith (str %) ".module")))]
    (log/info "auto scan modules:" ms)
    (doseq [m ms]
      (require m)) ;; load
    (let [modules (map #(-> (ns-publics %)
                          (get 'module)
                          (var-get)) ms)
          sorted-modules (sort-by types/sort modules)]
      (log/info "sorted app modules:" (map types/name sorted-modules))
      sorted-modules)))

(defn create-app [init]
  (set-app-module!
    (new-app-module
      "cljwtang"
      "cljwtang web app"
      (fn [m]
        (log/info "regist builtin modules...")
        (regist-modules!
          (tower-module)
          (nrepl-module)
          (cljwtang-view-module))
        (log/info "regist app modules...")
        (apply regist-modules! (auto-scan-modules))
        (init))
      (fn [m] (load-snippets (types/snippets-ns *app-module*)))))
  (init-app-module!))
