(ns cljwtang.buildin
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cljwtang.datatype :as datatype]
            [taoensso.tower :as tower]
            [clojure.tools.nrepl.server :as nrepl-server]
            [cljwtang.env-config :as env-config]
            [cljwtang.template.core :as template]
            [cljwtang.global :refer [template-engine]]))

(defn- load-i18n-dictionary []
  (if (io/resource env-config/i18n-config-file)
    (do
      (tower/load-dictionary-from-map-resource! env-config/i18n-config-file)
      (tower/set-config! [:dev-mode?] env-config/dev-mode?))
    (log/warn "\tNot found" env-config/i18n-config-file ",使用默认配置!")))

(defn tower-module [] 
  (datatype/new-ui-module
    {:name "tower"
     :init (fn [m]
             (load-i18n-dictionary))}))

(defn- start-nrepl-server []
  (defonce nrepl-server
    (nrepl-server/start-server :port env-config/nrepl-server-port))
  (log/info (str "use lein to connect nrepl server: lein repl :connect "
                 env-config/nrepl-server-port)))

(defn nrepl-module []
  (datatype/new-ui-module
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

(defn cljwtang-view-module []
  (datatype/new-ui-module
   {:name "cljwtang-view"
    :init (fn [m] (cljwtang-view-init))}))
