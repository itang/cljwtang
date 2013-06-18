(ns cljwtang.inject
  (:require [cljtang.core :refer :all]
            [korma.db :refer [defdb h2]]
            [cljwtang.core :refer [env-config]]
            [cljwtang.templates :as templates]))

(defonce ^:dynamic fn-app-config env-config)

(defonce ^:dynamic fn-user-logined? (constantly true))

(defonce ^:dynamic fn-current-user (constantly nil))

(defonce ^:dynamic app-routes [])

(defonce ^:dynamic bootstrap-tasks [])

(defonce ^:dynamic db-config
  (h2 {:subname "~/cljwtang_dev;AUTO_SERVER=TRUE"
       :user "sa"
       :password ""}))

(defonce ^:dynamic not-found-content "Not Found")

(defdb default-db db-config)

(defn- load-snippets
  "在指定名字空间加载所有的snippets"
  [& nss]
  (doseq [n nss] (require n))
  (let [helpers (->> nss
    (map ns-publics)
    (map (partial map 
     (fn [[k v]]
       [(keyword (str "snippet-" k)) (var-get v)])))
    (flatten)
    (apply hash-map))]
  (doseq [[k v] helpers]
    (templates/regist-tag k v))))

(defn- inject-var [v new-value]
  (alter-var-root v (constantly new-value)))

(defn inject-snippets-ns [nss]
  (apply load-snippets nss))

(defn inject-fn-user-logined? [f]
  (inject-var #'fn-user-logined? f))

(defn inject-fn-app-config [f]
  (inject-var #'fn-app-config f))

(defn inject-fn-current-user [f]
  (inject-var #'fn-current-user f))

(defn inject-routes [routes]
  (inject-var #'app-routes routes))

(defn inject-bootstrap-tasks [tasks]
  (inject-var #'bootstrap-tasks tasks))

(defn inject-db-config [db-config]
  (inject-var #'db-config db-config)
  (defdb latest-db db-config))

(defn inject-not-found-content [content]
  (inject-var #'not-found-content content))
