(ns lib-compojure.middleware-test
  (use clojure.test
       lib-compojure.middleware))

(defn- h1 [req]
  "a")

(defn- h2 [req]
  (throw (Exception. "exception")))

(defn- f [req e]
  (str "an " (.getMessage e)))

(deftest wrap-exception-handling-test
  (is (= "a" 
         ((wrap-exception-handling h1) {})))
  (is (= "Application Error!" 
         (:body ((wrap-exception-handling h2) {}))))
  (is (= "exception" 
         (.getMessage (:exception ((wrap-exception-handling h2) {})))))
  (is (= "an exception" 
         ((wrap-exception-handling h2 f) {}))))
