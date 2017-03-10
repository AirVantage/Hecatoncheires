(ns hecatoncheires.parser
  (:require [datomic.api :as d]
            [om.next.server :as om]))

;; -----------------------------------------------------------------------------
;; Reads
;; -----------------------------------------------------------------------------

(defmulti readf (fn [_ k _] k))

(defmethod readf :users
  [{:keys [conn query]} _ params]
  (let [db (d/db conn)
        q '[:find [(pull ?eid selector) ...]
            :in $ selector
            :where [?eid :hec.user/name]]]
    {:value (d/q q db (or query '[*]))}))

(defmethod readf :repos
  [{:keys [conn query]} _ params]
  (let [db (d/db conn)
        q '[:find [(pull ?eid selector) ...]
            :in $ selector
            :where [?eid :gh.repo/id]]]
    {:value (d/q q db (or query '[*]))}))

(defmethod readf :stacks
  [{:keys [conn query]} _ params]
  (let [db (d/db conn)
        q '[:find [(pull ?eid selector) ...]
            :in $ selector
            :where [?eid :aws.cf.stack/id]]]
    {:value (d/q q db (or query '[*]))}))

(defmethod readf :components
  [{:keys [conn query]} _ params]
  (let [db (d/db conn)
        q '[:find [(pull ?eid selector) ...]
            :in $ selector
            :where [?eid :hec.component/name]]]
    {:value (d/q q db (or query '[*]))}))


(defmethod readf :default
  [{:keys [conn]} k  _]
  {:value {:error (str "No handler for read key " k)}})
;; -----------------------------------------------------------------------------
;; Mutations
;; -----------------------------------------------------------------------------

(defmulti mutatef om/dispatch)

(defmethod mutatef :default
  [_ k _]
  {:value {:error (str "No handler for mutation key " k)}})

(defmethod mutatef 'component/create
  [{:keys [conn]} _ {:keys [db/id hec.component/name hec.component/leader
                            hec.component/repo hec.component/stack]}]
  {:value {:keys [:components]}
   :action (fn []
             @(d/transact conn [{:db/id id
                                 :hec.component/name name
                                 :hec.component/leader leader
                                 :hec.component/repo repo
                                 :hec.component/stack stack}]))})
