(ns hecatoncheires.server
  (:require [clojure.walk :as walk]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [response file-response resource-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.resource :refer [wrap-resource]]
            [org.httpkit.server :refer [run-server]]
            [om.next.server :as om]
            [hecatoncheires.parser :as parser]
            [hecatoncheires.middleware
             :refer [wrap-transit-params wrap-transit-response
                     wrap-db]]
            [com.stuartsierra.component :as component]))

(defn api [req]
  (let [{:keys [transit-params db-conn]} req
        data ((om/parser {:read parser/readf :mutate parser/mutatef})
              {:conn db-conn} (first transit-params))
        data' (walk/postwalk (fn [x]
                               (if (and (sequential? x) (= :result (first x)))
                                 (update-in x [1] dissoc :db-before :db-after :tx-data)
                                 x))
                             data)]
    {:status 200
     :headers {"Content-Type" "application/transit+json"}
     :body data'}))

(defroutes routes
  (GET "/" _
       (assoc (resource-response (str "index.html") {:root "public"})
              :headers {"Content-Type" "text/html"}))
  (POST "/api" req
        (api req) ))

(defn http-handler [conn]
  (-> routes
      (wrap-defaults api-defaults)
      (wrap-db conn)
      wrap-transit-params
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
