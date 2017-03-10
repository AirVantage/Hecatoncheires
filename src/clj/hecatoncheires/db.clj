(ns hecatoncheires.db
  (:require [datomic.api :as d]
            [com.stuartsierra.component :as component]))


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
