(ns cljwtang.inject
  (:require [cljtang.core :refer :all]
            [korma.db :refer [defdb h2]]
            [cljwtang.utils.env :refer [env-config]]
            [cljwtang.datatype :as datatype]
            [cljwtang.template.core :as template]
            [cljwtang.global :refer [template-engine]]
            [cljwtang.buildin :as buildin]))

(defonce ^:dynamic fn-app-config env-config)

(defonce ^:dynamic fn-user-logined? (constantly true))

(defonce ^:dynamic fn-current-user (constantly nil))

(defonce ^:dynamic db-config
  (h2 {:subname "~/cljwtang_dev;AUTO_SERVER=TRUE"
       :user "sa"
       :password ""}))

(defdb default-db db-config)

(defonce ^:dynamic not-found-content "Not Found")

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

(def app-module (datatype/new-app-module 
                  "cljwtang"
                  "cljwtang web app"
                  nil
                  (fn [m] (load-snippets (datatype/snippets-ns app-module)))))

(defn regist-modules! [& modules]
  (doseq [m modules] (datatype/regist-module app-module m)))

(regist-modules!
  (buildin/tower-module)
  (buildin/nrepl-module)
  (buildin/cljwtang-view-module))

(defn app-routes []
  (datatype/routes app-module))

(defn bootstrap-tasks []
  (datatype/bootstrap-tasks app-module))

(defn app-menus []
  (datatype/menu-tree (datatype/menus app-module)))

(defn app-snippet-ns []
  (datatype/snippets-ns app-module))

(defn- inject-var [v new-value]
  (alter-var-root v (constantly new-value)))

(defn inject-fn-user-logined? [f]
  (inject-var #'fn-user-logined? f))

(defn inject-fn-app-config [f]
  (inject-var #'fn-app-config f))

(defn inject-fn-current-user [f]
  (inject-var #'fn-current-user f))

(defn inject-db-config [db-config]
  (inject-var #'db-config db-config)
  (defdb latest-db db-config))

(defn inject-not-found-content [content]
  (inject-var #'not-found-content content))
