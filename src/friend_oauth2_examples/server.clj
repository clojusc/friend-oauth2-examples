(ns friend-oauth2-examples.server
  (:require [ring.server.standalone :as ring]
            [taoensso.timbre :as log]
            [friend-oauth2-examples.google-handler :as h]))

(defonce srv (atom nil))

(log/merge-config! {:appenders {:spit {:enabled? true
                                       :fmt-output-opts {:nofonts? true}}
                                :standard-out {:enabled? true
                                               :fmt-output-opts {:nofonts? true}}}
                    :shared-appender-config {:spit-filename "/tmp/web.log"}})


(defn -main []
  (log/info "starting server")
  (swap! srv (fn [s] (when s (.stop s))
               (ring/serve #'friend-oauth2-examples.google-handler/app {:port 4567
                                                                        :open-browser? false}))))

(comment
  (-main)

  (use 'utilza.repl)
  
  (hjall @srv)
  )
