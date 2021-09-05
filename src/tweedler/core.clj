(ns tweedler.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]))

(defn tweedler [request] 
  (str "Hello " (get (:params request) "name")))

(defn response-middleware [handler]
  (fn [request]
    (let [response (handler request)]
      (if (instance? String response)
      {:body response
       :status 200
       :headers {"Content-Type" "text/html"}}
      response
        )
      )
    )
  )

(def responseHandler
  (-> tweedler
      response-middleware
      wrap-params))

(defn -main []
  (jetty/run-jetty responseHandler {:port 3000}))