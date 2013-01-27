(defproject lib-compojure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [lib-noir "0.3.5"]
                 [stencil "0.3.1"]
                 [org.clojure/tools.logging "0.2.4"]]
  :profiles {:dev {:plugins [[codox "0.6.4"]
                             [jonase/eastwood "0.0.2"]
                             [lein-localrepo "0.4.1"]
                             [lein-deps-tree "0.1.2"]
                             [lein-pprint "1.1.1"]
                             [lein-kibit "0.0.7"]
                             [lein-bikeshed "0.1.0"]]}
             :1.4.0 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5.0 {:dependencies [[org.clojure/clojure "1.5.0-RC2"]]}}
   :aliases {"run-tests" ["with-profile" "1.4.0:1.5.0" "test"]}
   :warn-on-reflection true
   :injections [(require 'clojure.pprint)]
   :pom-addition [:developers [:developer [:name "itang"]]])