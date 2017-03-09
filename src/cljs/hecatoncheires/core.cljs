(ns hecatoncheires.core
  (:require cljsjs.jquery
            cljsjs.bootstrap
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :as html :refer-macros [html]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!] :as async]
            [clojure.string :as string]
            [hecatoncheires.ui :as ui]
            [clojure.walk :refer [postwalk]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

;; -----------------------------------------------------------------------------
;; Init
;; -----------------------------------------------------------------------------

(def init-data
  {::users []
   ::stacks []
   ::repos []
   ::components []})

;; -----------------------------------------------------------------------------
;; Parsing
;; -----------------------------------------------------------------------------

(defn q [st k query]
  (om/db->tree query (get st k) st))

(derive ::users ::basics)
(derive ::stacks ::basics)
(derive ::repos ::basics)
(derive ::components ::basics)

(defmulti read om/dispatch)

(defmethod read ::basics
  [{:keys [query state ast]} k _]
  (let [st @state]
    {:value (q st k query)
     ::query ast
     }))

(defmethod read :default
  [{:keys [query state]} k _]
  (let [st @state]
    {:value (q st k query)}))

(defmulti mutate om/dispatch)

(defn create-component [component]
  (-> component
      (update-in [:hec.component/leader] #(map second %))
      (update-in [:hec.component/repo] #(map second %))
      (update-in [:hec.component/stack] #(map second %))))

(defn merge-component [state {:keys [db/id] :as component}]
  (let [component (-> component
                      (update-in [:hec.component/leader] (fn [x] (reduce #(conj %1 [:db/id %2]) [] x)))
                      (update-in [:hec.component/repo] (fn [x] (reduce #(conj %1 [:db/id %2]) [] x)))
                      (update-in [:hec.component/stack] (fn [x] (reduce #(conj %1 [:db/id %2]) [] x))))]
    (swap! state
           #(-> %
                (assoc-in [:db/id id] component)
                (update-in [::components] conj [:db/id id])))))

(defmethod mutate 'component/create
  [{:keys [state] :as env} key component]
  {:keys [::components]
   ::transact true
   :action #(merge-component state component)})

;; -----------------------------------------------------------------------------
;; Remote
;; -----------------------------------------------------------------------------

(def query-selectors {::users '[?e :hec.user/name]
                      ::repos '[?e :gh.repo/id]
                      ::stacks '[?e :aws.cf.stack/id]
                      ::components '[?e :hec.component/name]})

(defn query-builder [k q]
  `[:find [(~'pull ~'?e ~q) ...]
    :where ~(query-selectors k)])

(def mutation-key
  {'component/create ::components})

(defn send [req cb]
  (let [{:keys [::query ::transact]} req]
    (doseq [a query]
      (let [[k q] (first a)]
        (go (cb (->> (<! (http/post "/api/query" {:transit-params (query-builder k q)}))
                     :body
                     (assoc {} k))
                k))))
    (doseq [[m t] transact]
      (go (let [res (<! (http/post "/api/transact" {:transit-params [t]}))
                res (:body res)]
            (cb res :tempids))))))

;; -----------------------------------------------------------------------------
;; Root
;; -----------------------------------------------------------------------------

(defn my-merge [reconciler state res key]
  (if (= key :tempids)
    (let [tempids (:tempids res)
          next (postwalk #(or (tempids %) %) state)]
      {:next next})
    (let [component (om/app-root reconciler)
          novelty (om/tree->db component res :db/id)
                                        ;_ (cljs.pprint/pprint novelty)
          next (-> state
                   (assoc key (novelty key))
                   (update-in [:db/id] (partial merge-with merge) (:db/id novelty)))]
      {:next next :keys [key]})))

(def reconciler
  (om/reconciler
   {:state init-data
    :normalize true
    :parser (om/parser {:read read :mutate mutate})
    :send send
    :remotes [::query ::transact]
    :merge my-merge}))

(om/add-root! reconciler
              ui/App (gdom/getElement "app"))
