(ns hecatoncheires.collect
  (:require [datomic.api :as d]
            [hecatoncheires.aws :refer [get-stacks]]
            [hecatoncheires.github :refer [get-users get-repos]]
            [hecatoncheires.db :refer [conn]]))

(defn gh-to-hec-user [{:keys [:db/id
                              :gh.user/name]}]
  {:hec.user/name name
   :hec.user/gh id
   :hec.user/avatar (java.net.URI. (str "https://github.com/" name ".png?size=42"))})

(defn generate-users! [conn]
  (let [db (d/db conn)
        gh-users (d/q '[:find [(pull ?u [:db/id
                                         :gh.user/id
                                         :gh.user/name]) ...]
                        :where [?u :gh.user/id]]
                      db)
        hec-users (map gh-to-hec-user gh-users)]
    (d/transact conn hec-users)))

(defn init! []
  (d/transact conn (get-stacks))
  (d/transact conn (get-repos))
  (d/transact conn (get-users))
  (generate-users! conn)
  nil)
