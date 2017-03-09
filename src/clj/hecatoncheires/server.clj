(ns hecatoncheires.server
  (:require [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [response file-response resource-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.resource :refer [wrap-resource]]
            [org.httpkit.server :refer [run-server]]
            [hecatoncheires.db :refer [get-users get-repos get-stacks get-components create-component] :as db]
            [hecatoncheires.middleware
             :refer [wrap-transit-body wrap-transit-response
                     wrap-db]]
            [com.stuartsierra.component :as component]))

(defn query-handler [{:keys [body db-conn]}]
  (let [ret (db/query db-conn body)]
    {:status 200
     :headers {"Content-Type" "application/transit+json"}
     :body ret}))

(defn transact-handler [{:keys [body db-conn]}]
  (let [ret @(db/transact db-conn body)]
    {:status 200
     :headers {"Content-Type" "application/transit+json"}
     :body (select-keys ret [:tempids])
     }))

(defroutes routes
  (GET "/" _
       (assoc (resource-response (str "index.html") {:root "public"})
              :headers {"Content-Type" "text/html"}))
  (POST "/api/query" req
        (query-handler req))
  (POST "/api/transact" req
        (transact-handler req)))

(defn http-handler [conn]
  (-> routes
      (wrap-defaults api-defaults)
      (wrap-db conn)
      wrap-transit-body
      wrap-transit-response
      wrap-with-logger
      wrap-gzip
      (wrap-resource "public")))

;; -----------------------------------------------------------------------------
;; Component
;; -----------------------------------------------------------------------------

(defrecord WebServer [port server db]
  component/Lifecycle
  (start [component]
    (let [conn (:connection db)
          handler (http-handler conn)
          server (run-server handler {:port port :join? false})]
      (assoc component :server server)))
  (stop [component]
    (server :timeout 100)))

(defn new-webserver [port]
  (WebServer. port nil nil))
