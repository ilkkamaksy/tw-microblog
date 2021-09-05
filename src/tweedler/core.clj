(ns tweedler.core
  (:require [ring.adapter.jetty :as jetty]))

(defn tweedler [request] "Hello world")

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

(defn -main []
  (jetty/run-jetty (response-middleware tweedler) {:port 3000}))