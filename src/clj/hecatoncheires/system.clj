(ns hecatoncheires.system
  (:require [com.stuartsierra.component :as component]
            [hecatoncheires.db :as db]
            [hecatoncheires.server :as server]
            [hecatoncheires.collect :as collect]))

(defn prod-system [config]
  (let [{:keys [db-uri web-port]} config]
    (component/system-map
     :db (db/new-database db-uri)
     :webserver (component/using
                 (server/new-webserver (Integer. web-port))
                 {:db :db})
     :collector (component/using
                 (collect/new-collector)
                 {:db :db}))))
