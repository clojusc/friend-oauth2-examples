(defproject friend-oauth2-examples "0.0.3"
  :description "Friend Oauth2 Workflow examples, includes App.net, Facebook and Github handlers."

  :url "https://github.com/ddellacosta/friend-oauth2-examples"

  :license {:name "MIT License"
            :url "http://dd.mit-license.org"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5" :exclusions [ring/ring-core org.clojure/core.incubator]]
                 [com.cemerick/friend "0.1.5" :exclusions [ring/ring-core]]
                 [friend-oauth2 "0.0.4" :exclusions [org.apache.httpcomponents/httpcore]]
                 [cheshire "5.0.2"]
                 [ring-server "0.2.8" :exclusions [ring]]]
  :profiles {:dev {:dependencies [[ring-mock "0.1.3"]]
                   :plugins [[lein-ring "0.8.5" :exclusions [org.clojure/clojure]]
                             [ring-server "0.2.8" :exclusions [org.clojure/clojure]]
                             [ring-mock "0.1.3" :exclusions [org.clojure/clojure]]]}
             :appdotnet {:ring {:handler friend-oauth2-examples.appdotnet-handler/app}}
             :github {:ring {:handler friend-oauth2-examples.github-handler/app}}
             :facebook {:ring {:handler friend-oauth2-examples.facebook-handler/app}}}
  :aliases  {"facebook" ["with-profile" "dev,facebook"
                         "do" "ring" "server"]
             "appdotnet" ["with-profile" "dev,appdotnet"
                          "do" "ring" "server"]
             "github" ["with-profile" "dev,github"
                       "do" "ring" "server"]})

