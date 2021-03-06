(ns tweedler.core
(:require [net.cgrand.enlive-html :as enlive]
          [ring.adapter.jetty :as jetty]
          [ring.middleware.params :refer [wrap-params]]
          [markdown.core :refer [md-to-html-string]]
          [clojure.string :refer [escape]]
          [compojure.route :refer [resources]]
          [compojure.core :refer [defroutes GET POST]]))

(defrecord Tweed [title content])

(defprotocol TweedStore
  (get-tweeds [store])
  (put-tweed! [store tweed]))

(defrecord AtomStore [data])

(extend-protocol TweedStore 
  AtomStore
  (get-tweeds [store]
    (get @(:data store) :tweeds))
  (put-tweed! [store tweed]
    (swap! (:data store)
           update-in [:tweeds] conj tweed)))

(def store (->AtomStore (atom {:tweeds '()})))
(put-tweed! store (->Tweed "title 1" "content 1"))
(put-tweed! store (->Tweed "title 2" "content 2"))

(enlive/defsnippet tweed-tpl "tweedler/index.html" [[:article.tweed enlive/first-of-type]]
  [tweed]
  [:.title] (enlive/html-content (md-to-html-string (:title tweed)))
  [:.content] (enlive/html-content (md-to-html-string (:content tweed))))

(enlive/deftemplate index-tpl "tweedler/index.html"
                    [tweeds]
                    [:section.tweeds] (enlive/content (map tweed-tpl tweeds))
                    [:form] (enlive/set-attr :method "post" :action "/"))

(defn escape-html [s]
  (escape s {\> "&gt;" \< "&lt;"}))

(defn handle-create [{{title "title" content "content"} :params}]
  (put-tweed! store (->Tweed (escape-html title) (escape-html content)))
  {:body "" :status 302 :headers {"Location" "/"}})

(defroutes app-routes 
  (GET "/" [] (index-tpl (get-tweeds store)))
  (POST "/" req (handle-create req))
  (resources "/css" {:root "tweedler/css"})
  (resources "/img" {:root "tweedler/img"}))

(def app 
  (-> app-routes
      (wrap-params)))

(defn -main []
  (jetty/run-jetty #'app {:port 4000 :join? false}))
