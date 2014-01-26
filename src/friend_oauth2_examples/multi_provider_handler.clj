(ns friend-oauth2-examples.multi-provider-handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cemerick.friend :as friend]
            [friend-oauth2.workflow :as oauth2]
            [friend-oauth2.util :refer [format-config-uri get-access-token-from-params]]
            [cheshire.core :as j]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(def config-auth {:roles #{::user}})

(def google-client-config
  {:client-id ""
   :client-secret ""
   :callback {:domain "http://www.mysite.com" :path "/auth/google"}})

(def google-uri-config
  {:authentication-uri {:url "https://accounts.google.com/o/oauth2/auth"
                       :query {:client_id (:client-id google-client-config)
                               :response_type "code"
                               :redirect_uri (format-config-uri google-client-config)
                               :scope "email"}}

   :access-token-uri {:url "https://accounts.google.com/o/oauth2/token"
                      :query {:client_id (:client-id google-client-config)
                              :client_secret (:client-secret google-client-config)
                              :grant_type "authorization_code"
                              :redirect_uri (format-config-uri google-client-config)}}})


(def github-client-config
  {:client-id ""
   :client-secret ""
   :callback {:domain "http://www.mysite.com" :path "/auth/github"}})

(def github-uri-config
  {:authentication-uri {:url "https://github.com/login/oauth/authorize"
                        :query {:client_id (:client-id github-client-config)
                                :response_type "code"
                                :redirect_uri (format-config-uri github-client-config)
                                :scope "user"}}

   :access-token-uri {:url "https://github.com/login/oauth/access_token"
                      :query {:client_id (:client-id github-client-config)
                              :client_secret (:client-secret github-client-config)
                              :grant_type "authorization_code"
                              :redirect_uri (format-config-uri github-client-config)}}})

(def providers
  {:google {:client-config google-client-config
            :uri-config google-uri-config}
   :github {:client-config github-client-config
            :uri-config github-uri-config
            :access-token-parsefn get-access-token-from-params}})

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
  (handler/site
   (friend/authenticate
    ring-app
    {:allow-anon? true
     :workflows [(oauth2/workflow-multi
                  {:providers providers
                   :auth-error-fn (fn [error]
                                    (ring.util.response/response
                                     (str
                                      "Error from authentication provider :"
                                      error)))
                   :pre-login-fn (fn [multi-config]
                                   (ring.util.response/response
                                    (str
                                     "<a href='/auth/google'> Google </a><br/>"
                                     "<a href='/auth/github'> Github </a><br/>")))
                   :credential-fn (fn [token]
                                    {:identity token
                                     :roles #{::user}})})]})))
