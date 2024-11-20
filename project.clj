(defproject bet "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :uberjar-name "bet.jar"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [cheshire "5.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [clj-http "3.9.1"]]
  :plugins [[lein-ring "0.12.5"]]
  :main ^:skip-aot bet.core
  :ring {:handler bet.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.4.0"]
                        [midje "1.9.6"]
                        [ring/ring-core "1.9.4"]
                        [ring/ring-jetty-adapter "1.9.4"]]
         :plugins [[lein-midje "3.2.1"]
                   [lein-cloverage "1.0.13"]]}}
  :test-paths ["test/unitarios" "test/aceitacao"])