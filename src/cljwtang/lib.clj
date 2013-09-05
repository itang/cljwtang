(ns cljwtang.lib
  (:require potemkin)
  (:require [clojure.tools.logging]
            [ring.util.response]
            [compojure core]
            [clojurewerkz.route-one.core]
            [noir response session]
            [noir.util.middleware]

            [cljwtang.core]
            [cljwtang.config.app]
            [cljwtang.web core request response view middleware]
            [cljwtang.utils env mail scrypt upload]))

(potemkin/import-vars
 [potemkin
  import-vars]

 [clojure.tools.logging
  debug info warn error]

 [compojure.core
  defroutes
  context
  GET
  POST
  routes]

 [clojurewerkz.route-one.core
  path-for
  url-for
  defroute
  with-base-url
  #_(route)
  ]

 [ring.util.response
  not-found
  status]

 [noir.response
  json
  content-type
  redirect]

 [noir.util.middleware
  app-handler]

 [cljwtang.config.app
  version
  run-mode prod-mode? dev-mode?
  server-port
  start-nrepl-server? nrepl-server-port
  i18n-config-file
  hostname hostaddr
  mail-server mail-vendors-out-rule system-monitoring-mail-accounts
  appdata-dir]

 [cljwtang.core
  #_(name sort)
  #_(init)
  description
  new-ui-module
  new-bootstrap-task
  new-funcpoint
  new-element-attrs
  new-menu
  maps->menus
  menu-tree
  #_(new-app-module)
  app-module
  app-sub-modules
  regist-modules!
  init-app-module!
  app-routes
  app-bootstrap-tasks
  app-menus
  app-snippet-ns
  *app-config-fn*
  *user-logined?-fn*
  *current-user-fn*
  *db-config*
  *not-found-content*
  set-user-logined?-fn!
  set-app-config-fn!
  set-current-user-fn!
  set-db-config!
  set-not-found-content!]

 [cljwtang.web.core
  message json-message success-message json-success-message failture-message
  json-failture-message error-message json-error-message info-message json-info-message
  flash-msg
  flash-post-params
  postback-params
  defhandler
  with-validates
  defhandler-with-validates
  render-string render-file regist-helper regist-tag regist-tag template-engine-name
  clear-template-cache!]

 [cljwtang.web.request
  ajax?]

 [cljwtang.web.response
  html content-length]

 [cljwtang.web.view
  template
  view
  defsnippet]

 [cljwtang.web.middleware
  wrap-templates-refresh
  wrap-request-log
  wrap-dev-helper
  wrap-profile
  wrap-delay
  wrap-exception-handling ]

 [cljwtang.utils.env
  env-config
  env-config-int
  env-config-bool]

 [cljwtang.utils.mail
  send-mail
  send-mail-by-template
  mail-vendor-by-email-account]

 [cljwtang.utils.scrypt
  encrypt
  check
  verify]

 [cljwtang.utils.upload
  upload-file])

(defn session-remove!
  "remove session data"
  [k]
  (noir.session/remove! k))
