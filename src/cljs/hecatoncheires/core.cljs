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
;; Parsing
;; -----------------------------------------------------------------------------

(defn- q [st k query]
  (om/db->tree query (get st k) st))

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [query state]} k _]
  (let [st @state]
    {:value (q st k query)
     :remote true}))

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [_ _ _]
  {:remote true})

;; -----------------------------------------------------------------------------
;; Remote
;; -----------------------------------------------------------------------------

(defn send [req cb]
  (let [{:keys [remote]} req]
    (go (let [{:keys [body]} (<! (http/post "/api" {:transit-params [remote]}))]
          (cb body)))))

;; -----------------------------------------------------------------------------
;; Root
;; -----------------------------------------------------------------------------


(def reconciler
  (om/reconciler
   {:state {}
    :normalize true
    :parser (om/parser {:read read :mutate mutate})
    :send send}))

(om/add-root! reconciler
              ui/App (gdom/getElement "app"))
