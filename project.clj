(defproject cljwtang "0.1.0-SNAPSHOT"
  :description "clojure web app libs"
  :url "http://cljwtang.itang.me"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]

                 ;;[org.clojure/clojurescript "0.0-1586"]
                 [org.clojure/core.incubator "0.1.3"]
                 [org.clojure/core.memoize "0.5.5"]
                 [org.clojure/core.match "0.2.0-rc2"]

                 [cljtang "0.1.1"]
                 [clj-pretty-format "0.1.1"]
                 ;;[prismatic/plumbing "0.1.0"]

                 [commons-codec/commons-codec "1.8"]
                 [com.lambdaworks/scrypt "1.4.0"]       ; scrypt

                 [coercer "0.2.0"]                    ; conv types
                 [environ "0.4.0"]                    ; managing environment setting
                 [me.raynes/conch "0.5.1"]            ; shell
                 [me.raynes/fs "1.4.5"]               ; file system
                 ;;[cc.qbits/tardis "0.3.1"]            ; UUID
                 [crypto-random "1.1.0"]              ; crypto
                 ;;[clj-time "0.5.0"]                   ; date time
                 ;;[markdown-clj "0.9.19"]              ; markdown
                 ;;[crouton "0.1.1"]                    ; jsoup html

                 ;;[sonian/carica "1.0.2"]              ; config
                 [cheshire "5.2.0"]                   ; JSON
                 [com.draines/postal "1.10.3"]         ; email
                 [com.taoensso/tower "1.7.1"]         ; i18n

                 [org.clojure/tools.logging "0.2.6"]  ; logging
                 [org.slf4j/slf4j-log4j12 "1.7.5"]
                 [log4j "1.2.17" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]

                 [metis "0.3.3"]                      ; validate data
                 ;;[clj-rss "0.1.2"]                    ; rss
                 ;;[clojurewerkz/quartzite "1.1.0"]     ; Scheduling
                 ;;[clj-http "0.6.4"]                   ; http client
                 ;;[com.novemberain/pantomime "1.7.0"]  ;MIME types

                 [ring "1.2.0-RC1"]
                 ;;[com.cemerick/friend "0.1.5"
                   ;;:exclusions [ring/ring-core]]      ; authentication
                 [compojure "1.1.5"]                  ; web framework
                 [stencil "0.3.2"]                    ; mustache template
                 ;;[hbs "0.4.1"]                        ; handlebars template
                 [lib-noir "0.6.4"]                   ; middleware

                 [com.h2database/h2 "1.3.172"]        ; H2 Driver
                 [postgresql "9.1-901-1.jdbc4"]       ; PostgreSQL Driver
                 [org.clojure/java.jdbc "0.2.3"]      ; java jdbc
                 [korma "0.3.0-RC5"]                  ; DB
                 ;;[lobos "1.0.0-beta1"]                ; migrations
                 [me.itang/lobos "1.0.0-beta1"]
                 ;;[clojurewerkz/elastisch "1.1.0-rc1"] ; ElasticSearch
                 ;;[com.taoensso/carmine "1.7.0-beta2"] ; redis
                 ;;[akka/akka-clojure "0.1.0"]          ; akka
                 ;;[co.paralleluniverse/pulsar "0.1.1"] ; Pulsar

                 [http-kit "2.1.4"]

                 [jayq "2.4.0"]                       ; jQuery wrapper
                 ;;[prismatic/dommy "0.0.2"]            ; CS Template
                 [cljstang "0.1"]
                 ]
  :profiles {:dev {:plugins [[codox "0.6.4"]
                             [lein-pprint "1.1.1"]
                             [lein-outdated "1.0.1"]
                             [lein-checkall "0.1.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}}
  :aliases {"run-tests" ["with-profile" "1.4:1.5" "test"]}
  :global-vars {*warn-on-reflection* true}
  :injections [(require 'clojure.pprint)]
  :min-lein-version "2.0.0"
  :pom-addition [:developers [:developer
                              [:id "itang"]
                              [:name "唐古拉山"]
                              [:url "http://www.itang.me"]
                              [:email "live.tang@gmail.com"]]])
