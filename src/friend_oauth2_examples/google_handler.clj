(ns friend-oauth2-examples.google-handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [utilza.jwt :as jwt]
            [cemerick.friend :as friend]
            [friend-oauth2.workflow :as oauth2]
            [friend-oauth2.util :refer [format-config-uri]]
            [cheshire.core :as j]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(def config-auth  {:roles #{::user}})

(def client-config
  {:client-id ""
   :client-secret ""
   :openid.realm  ""
   :hd  ""
   :prompt "select_account" ;;  might be problematic, might not.
   ;; NOTE callback domain is not dynamic.
   :callback {:domain "http://localhost:4567"
              :path "/oauth2callback"}})


;; TODO; discover this, atom  cache it? or doesn't clj-http have caching?
(def uri-config
  {:authentication-uri {:url "https://accounts.google.com/o/oauth2/auth"
                        :query {:client_id (:client-id client-config)
                                :response_type "code"
                                :redirect_uri (format-config-uri client-config)
                                :scope "email"}}

   :access-token-uri {:url  "https://accounts.google.com/o/oauth2/token"
                      :query {:client_id (:client-id client-config)
                              :client_secret (:client-secret client-config)
                              :grant_type "authorization_code"
                              :redirect_uri (format-config-uri client-config)}}})

(defn show-session
  [{:keys [session]}]
  (str "<p>The current session: </p><pre>"
       (with-out-str (clojure.pprint/pprint session)) "</pre>"))

(defroutes ring-app
  (GET "/" request "open.")
  (GET "/status" request
       (let [count (:count (:session request) 0)
             session (assoc (:session request) :count (inc count))]
         (-> (ring.util.response/response
              (str "<p>We've hit the session page " (:count session)
                   " times.</p>" (show-session request)))
             (assoc :session session))))
  (GET "/authlink" request
       (friend/authorize #{::user} (str "<h3>Authorized page.</h3>" (show-session request))))
  (GET "/authtest" request
       (friend/authorize #{::user} (str "<h3>You are authorized as: "
                                        (-> request :session :cemerick.friend/identity :current) "</h3>")))
  (GET "/authlink2" request
       (friend/authorize #{::user} "Authorized page 2."))
  (GET "/admin" request
       (friend/authorize #{::admin} "Only admins can see this page."))
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/"))))

(defn parse-jwt
  "Returns the auth informationfrom a JSON response body"
  [{body :body}]
  (let [resp (json/parse-string body true)]
    (-> resp :id_token jwt/decode)))



(defn- google-credential-fn
  [db {{:keys [email] :as stripped} :access-token :as creds}]
  (log/debug "credential-fn " (with-out-str (clojure.pprint/pprint stripped)))
  ;;  (when-let [user (users email)]
  ;;  (log/debug "user" (-> user  clojure.pprint/pprint with-out-str))
  (merge  {:identity email}
          config-auth
          stripped))


(defn wrap-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (log/error e (with-out-str (->> request :body clojure.pprint/pprint))
                   (with-out-str (clojure.pprint/pprint request)))
        (throw e)))))


(def app
  (wrap-exception
   (handler/site
    (friend/authenticate
     ring-app
     {:allow-anon? true
      :workflows [(oauth2/workflow
                   {:client-config client-config
                    :uri-config uri-config
                    :credential-fn  (partial google-credential-fn nil)
                    :access-token-parsefn  parse-jwt})]}))))



(comment


  )