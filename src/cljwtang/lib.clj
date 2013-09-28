(ns cljwtang.lib
  (:require [cljtang.lib :refer [import-vars import-macro import-fn import-def]])
  (:require [clojure.tools.logging :as log]
            [pandect.core]
            [ring.util response anti-forgery]
            [ring.middleware.anti-forgery]
            [compojure core]
            [clojurewerkz.route-one.core]
            [noir request response session validation]
            [noir.util.middleware]
            [cemerick.friend]
            [cljwtang.core]
            [cljwtang.config.app]
            [cljwtang.web core request response view middleware]
            [cljwtang.utils env mail scrypt upload]))

(import-vars
 [pandect.core
  sha1
  sha1-file
  sha1-hmac]

 [ring.util.response
  not-found]

 [ring.util.anti-forgery
  anti-forgery-field]

 [ring.middleware.anti-forgery
  wrap-anti-forgery]

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

 [noir.request
  *request*]

 [noir.response
  json
  content-type
  redirect
  status]
 
 [noir.validation
  wrap-noir-validation
  has-value?
  has-values?
  #_(not-nil?)
  min-length?
  max-length?
  matches-regex?
  is-email?
  valid-file?
  valid-number?
  greater-than?
  less-than?
  equal-to?]

 [noir.util.middleware
  app-handler]
 
 [cemerick.friend
  authenticated
  authorize
  wrap-authorize]

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
  new-ui-module
  new-bootstrap-task
  new-funcpoint
  new-element-attrs
  new-menu
  maps->menus
  menu-tree
  #_(new-app-module)
  get-app-module
  create-app
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
  *exception-handle-fn*
  *load-credentials-fn*
  *unauthorized-handler*
  get-not-found-content
  set-user-logined?-fn!
  set-app-config-fn!
  set-current-user-fn!
  set-db-config!
  set-not-found-content!
  set-exception-handle-fn!
  set-load-credentials-fn!
  set-unauthorized-handler!]

 [cljwtang.web.core
  message success-message failture-message error-message info-message
  flash-msg
  flash-post-params
  postback-params
  with-routes
  defhandler
  render-string render-file regist-helper regist-tag regist-tag template-engine-name
  clear-template-cache!]

 [cljwtang.web.request
  ajax?
  reuqest-params]

 [cljwtang.web.response
  html content-length
  json-message json-success-message json-failture-message json-error-message json-info-message]

 [cljwtang.web.view
  template
  view
  defsnippet]

 [cljwtang.web.middleware
  wrap-templates-refresh
  wrap-request-log
  wrap-dev-helper
  wrap-profile
  wrap-exception-handling]

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
  verify
  scrypt-credential-fn]

 [cljwtang.utils.upload
  upload-file!
  multipart-files
  multipart-file])

(import-macro log/debug log-debug)
(import-macro log/info log-info)
(import-macro log/warn log-warn)
(import-macro log/error log-error)

(import-fn noir.session/remove! session-remove!)

(import-fn noir.validation/get-errors validate-get-errors)
(import-fn noir.validation/set-error validate-set-error)
(import-fn noir.validation/rule validate-rule)
(import-fn noir.validation/errors? validate-errors?)
(import-fn noir.validation/on-error validate-on-error)
(import-def noir.validation/*errors* *validate-errors*)
