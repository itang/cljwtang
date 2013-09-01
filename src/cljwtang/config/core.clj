(ns cljwtang.config.core
  (:require [cljwtang.utils.env :as env]))

(def ^{:doc "库版本信息"}
  version "cljwtang-0.1.0-SNAPSHOT")

(def ^{:doc "应用运行模式(开发或生产)"}
  run-mode
  (if (or (env/env-config :wapp-no-dev) (env/env-config :lein-no-dev))
    "prod"
    "dev"))

(def ^{:doc "应用是在生产模式下运行?"}
  prod-mode? (= run-mode "prod"))

(def ^{:doc "应用是在开发模式下运行?"}
  dev-mode? (= run-mode "dev"))

(def ^{:doc "web服务端口"}
  server-port
  (env/env-config-int :cljwtang-server-port "3000"))

(def ^{:doc "start nrepl server"}
  start-nrepl-server?
  (env/env-config-bool :cljwtang-start-nrepl-server "false"))

(def ^{:doc "nrepl server port"}
  nrepl-server-port
  (env/env-config-int :cljwtang-nrepl-server-port "7888"))

(def ^{:doc "i18n 配置文件"}
  i18n-config-file "i18n-config.clj")
