(ns hecatoncheires.core
  (:require [hecatoncheires.system :as system]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]])
  (:gen-class))

(defonce servlet-system (atom nil))

(defn prod-start []
  (let [sys (system/prod-system env)
        sys' (component/start sys)]
    (reset! servlet-system sys')
    sys'))

(defn prod-stop []
  (swap! servlet-system component/stop))

(defn -main [& args]
  (println "Starting on port " (:web-port env))
  (prod-start)
  (println (str "Started server on port " (:web-port env)))
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(do (prod-stop)
                                  (println "Server stopped")))))
