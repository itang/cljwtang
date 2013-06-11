(ns cljwtang.inject
  (:require
    [cljwtang.core :refer [env-config]]
    [cljwtang.templates :refer :all]))

(defonce ^:dynamic fn-user-logined? (constantly true))

(defonce ^:dynamic fn-app-config env-config)

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
    (regist-tag k v))))

(defn- inject-var [v new-value]
  (alter-var-root v (constantly new-value)))

(defn inject-snippets-ns [nss]
  (apply load-snippets nss))

(defn inject-fn-user-logined? [f]
  (inject-var #'fn-user-logined? f))

(defn inject-fn-app-config [f]
  (inject-var #'fn-app-config f))
