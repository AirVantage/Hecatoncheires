(ns hecatoncheires.github
  (:require [tentacles.repos :as repos]
            [tentacles.orgs :as orgs]
            [environ.core :refer [env]]
            [clojure.string :as string]
            [clojure.set :refer [rename-keys]]
            [clojure.instant :refer [read-instant-date]]))

;; -----------------------------------------------------------------------------
;; Utils
;; -----------------------------------------------------------------------------

(def gh-opt
  {:per-page 100
   :all-pages true
   :auth (env :gh-auth)})

;; -----------------------------------------------------------------------------
;; Repositories
;; -----------------------------------------------------------------------------

(defn gh-process-repo [repo]
  (-> repo
      (select-keys [:id :name
                    ;:description :pushed_at :html_url
                    ])
      (rename-keys {:id :gh.repo/id
                    :name :gh.repo/name
                    ;:pushed_at :gh.repo/last-change
                    ;:description :gh.repo/description
                    ;:html_url :gh.repo/url
                    })
      (update-in [:gh.repo/id] str)
      ;(update-in [:gh.repo/last-change] #(when % (-> % read-instant-date (.getTime))))
      ;(update-in [:gh.repo/description] str)
      ))

(defn get-repos []
  (->> (repos/org-repos (env :gh-org) gh-opt)
       (map gh-process-repo)))

;; -----------------------------------------------------------------------------
;; Users
;; -----------------------------------------------------------------------------

(defn gh-process-user [user]
  (-> user
      (select-keys [:id :login
                    ;:avatar_url
                    ])
      (rename-keys {:id :gh.user/id
                    :login :gh.user/name
                    ;:avatar_url :gh.user/avatar
                    })
      (update-in [:gh.user/id] str)))

(defn get-users []
  (->> (orgs/members (env :gh-org) gh-opt)
       (map gh-process-user)))
