(ns cljwtang
  (:require potemkin)
  (:require [ring.util.response]
            [compojure.core]
            [noir.response]
            [noir.session]
            [cljwtang config core request response view]
            [cljwtang.utils env mail scrypt upload]))

(potemkin/import-vars
 [potemkin
  import-vars]

 [compojure.core
  defroutes
  context
  GET
  POST]

 [ring.util.response
  not-found]

 [noir.response
  json
  content-type
  redirect]

 [cljwtang.config
  version
  run-mode prod-mode? dev-mode?
  server-port
  hostname hostaddr
  start-nrepl-server? nrepl-server-port
  i18n-config-file
  mail-server mail-vendors-out-rule system-monitoring-mail-accounts
  appdata-dir]

 [cljwtang.core
  app-config
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

 [cljwtang.request
  ajax?]

 [cljwtang.response
  html content-length]

 [cljwtang.view
  template
  view
  defsnippet]

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
