(ns hecatoncheires.db
  (:require [datomic.api :as d]
            [com.stuartsierra.component :as component]))


;; -----------------------------------------------------------------------------
;; Connection
;; -----------------------------------------------------------------------------

(def db-uri-base (str "datomic:mem://" (d/squuid)))

(d/create-database db-uri-base)

(defonce conn (d/connect db-uri-base))

;; -----------------------------------------------------------------------------
;; Schema
;; -----------------------------------------------------------------------------

(def schema
  [;; GitHub User --------------------------------------------------------------

   {:db/ident :gh.user/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   {:db/ident :gh.user/name
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   ;; GitHub Org ---------------------------------------------------------------

   {:db/ident :gh.org/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   {:db/ident :gh.org/name
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   ;; GitHub Repo --------------------------------------------------------------

   {:db/ident :gh.repo/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   {:db/ident :gh.repo/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident :gh.repo/owner
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}

   ;; AWS Account --------------------------------------------------------------

   {:db/ident :aws.account/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   {:db/ident :aws.account/name
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   ;; AWS Region ---------------------------------------------------------------

   {:db/ident :aws.region/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   {:db/ident :aws.region/name
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   ;; AWS Stack ----------------------------------------------------------------

   {:db/ident :aws.cf.stack/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   {:db/ident :aws.cf.stack/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident :aws.cf.stack/account
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}

   {:db/ident :aws.cf.stack/region
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}

   ;; Hec User -----------------------------------------------------------------

   {:db/ident :hec.user/name
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   {:db/ident :hec.user/avatar
    :db/valueType :db.type/uri
    :db/cardinality :db.cardinality/one}

   {:db/ident :hec.user/gh
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}

   ;; Hec Component ------------------------------------------------------------

   {:db/ident :hec.component/name
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   {:db/ident :hec.component/leader
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}

   {:db/ident :hec.component/repo
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}

   {:db/ident :hec.component/stack
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}])

;; -----------------------------------------------------------------------------
;; Initial data
;; -----------------------------------------------------------------------------

(def initial-data [])

;; -----------------------------------------------------------------------------
;; Init
;; -----------------------------------------------------------------------------

(d/transact conn schema)

;; -----------------------------------------------------------------------------
;; Queries
;; -----------------------------------------------------------------------------

(defn query [conn q]
  (d/q q (d/db conn)))

(defn transact [conn t]
  (d/transact conn t))

(defn get-users []
  (d/q '[:find [(pull ?e [:db/id :hec.user/name :hec.user/avatar]) ...]
         :where [?e :hec.user/name]]
       (d/db conn)))

(defn get-repos []
  (d/q '[:find [(pull ?e [:db/id :gh.repo/name]) ...]
         :where [?e :gh.repo/id]]
       (d/db conn)))

(defn get-stacks []
  (d/q '[:find [(pull ?e [:db/id :aws.cf.stack/name]) ...]
         :where [?e :aws.cf.stack/id]]
       (d/db conn)))

(defn get-components []
  (d/q '[:find [(pull ?e [*]) ...]
         :where [?e :hec.component/name]]
       (d/db conn)))

(defn create-component [component]
                                        ;(d/transact conn [component])
  (println component))

;; -----------------------------------------------------------------------------
;; Component
;; -----------------------------------------------------------------------------

(defrecord DatomicDatabase [uri schema initial-data connection]
  component/Lifecycle
  (start [component]
    (d/create-database uri)
    (let [c (d/connect uri)]
      @(d/transact c schema)
      @(d/transact c initial-data)
      (assoc component :connection c)))
  (stop [component]))

(defn new-database [db-uri]
  (DatomicDatabase. db-uri schema initial-data nil))
