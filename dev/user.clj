(ns user
  (:require [hecatoncheires.system :as system]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [ring.middleware.reload :refer [wrap-reload]]
            [figwheel-sidecar.repl-api :as figwheel]))

;; Let Clojure warn you when it needs to reflect on types, or when it does math
;; on unboxed numbers. In both cases you should add type annotations to prevent
;; degraded performance.
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defonce servlet-system (atom nil))

(defn dev-start []
  (let [sys (system/prod-system env)
        sys' (component/start sys)]
    (reset! servlet-system sys')
    sys'))

(defn dev-stop []
  (swap! servlet-system component/stop))

(defn dev-restart []
  (dev-stop)
  (dev-start))

(defn run []
  (figwheel/start-figwheel!)
  (dev-start))

(def browser-repl figwheel/cljs-repl)

(.addShutdownHook (Runtime/getRuntime)
                  (Thread. #(dev-stop)))
