(ns hecatoncheires.server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server]]
            [cognitect.transit :as transit]
            [hecatoncheires.db :refer [get-users get-repos get-stacks get-components create-component] :as db])
  (:import (java.io ByteArrayOutputStream))
  (:gen-class))

(defn- t-write [x]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos :json)
        _    (transit/write w x)
        ret  (.toString baos)]
    (.reset baos)
    ret))

(defn t-read [bis]
  (let [r (transit/reader bis :json)
        ret (transit/read r)]
    ret))

(defn query-handler [{:keys [body]}]
  (let [q (t-read body)
        _ (clojure.pprint/pprint q)
        ret (db/query q)]
    {:status 200
     :headers {"Content-Type" "application/transit+json; charset=utf-8"}
     :body (t-write ret)}))

(defn transact-handler [{:keys [body]}]
  (let [t (t-read body)
        ret @(db/transact t)]
    {:status 200
     :headers {"Content-Type" "application/transit+json; charset=utf-8"}
     :body (t-write (select-keys ret [:tempids]))
     }))

(defroutes routes
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})
  (POST "/api/query" req
        (query-handler req))
  (POST "/api/transact" req
        (transact-handler req))
  (resources "/"))

(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (run-server http-handler {:port port :join? false})))
