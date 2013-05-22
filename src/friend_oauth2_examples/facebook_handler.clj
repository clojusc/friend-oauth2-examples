(ns friend-oauth2-examples.facebook-handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cemerick.friend :as friend]
            [friend-oauth2.workflow :as oauth2]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

;; OAuth2 config
(defn access-token-parsefn
  [response]
  (-> response
      :body
      ring.util.codec/form-decode
      clojure.walk/keywordize-keys
      :access_token))

(def config-auth {:roles #{::user}})

(def client-config
  {:client-id ""
   :client-secret ""
   :callback {:domain "http://localhost:3000"
              :path "/facebook.callback"}})

(def uri-config
  {:authentication-uri {:url "https://www.facebook.com/dialog/oauth"
                        :query {:client_id (:client-id client-config)
                                :redirect_uri (oauth2/format-config-uri client-config)}}

   :access-token-uri {:url "https://graph.facebook.com/oauth/access_token"
                      :query {:client_id (:client-id client-config)
                              :client_secret (:client-secret client-config)
                              :redirect_uri (oauth2/format-config-uri client-config)
                              :code ""}}})


(def friend-config {:allow-anon? true
                    :workflows [(oauth2/workflow
                                 {:client-config client-config
                                  :uri-config uri-config
                                  :access-token-parsefn access-token-parsefn
                                  :config-auth config-auth})]})

(defroutes ring-app
  (GET "/" request "open.")
  (GET "/status" request
       (let [count (:count (:session request) 0)
             session (assoc (:session request) :count (inc count))]
         (-> (ring.util.response/response
              (str "<p>We've hit the session page " (:count session)
                   " times.</p><p>The current session: " session "</p>"))
             (assoc :session session))))
  (GET "/authlink" request
       (friend/authorize #{::user} "Authorized page."))
  (GET "/authlink2" request
       (friend/authorize #{::user} "Authorized page 2."))
  (GET "/admin" request
       (friend/authorize #{::admin} "Only admins can see this page."))
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/"))))



(def app
  (->   ring-app
        (friend/authenticate friend-config)
        handler/site))

