(ns tweedler.core
  (:require [ring.adapter.jetty :as jetty]))

(defn tweedler [request]
  {:body "Hello world" 
   :status 200
   :headers {"Content-Type" "text/html"}})

(defn -main []
  (jetty/run-jetty tweedler {:port 3000}))