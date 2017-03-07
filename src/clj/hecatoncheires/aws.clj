(ns hecatoncheires.aws
  (:require [amazonica.aws.cloudformation :as cf]
            [environ.core :refer [env]]
            [clojure.string :as string]))


;; -----------------------------------------------------------------------------
;; Utils
;; -----------------------------------------------------------------------------

(defn- record-to-map [[k v] r]
  (reduce (fn [m {k k v v}] (assoc m k v)) {} r))

(defn- collection-env [e]
  (map string/trim (string/split e #",")))

(defn- with-regions [f]
  (fn [& args]
    (reduce #(merge-with
              into
              %1
              (apply f (conj args
                             {:endpoint %2})))
            {}
            (collection-env (env :aws-regions)))))

(defn- with-next-token [f]
  "If there is a next-token key in the answer to the AWS request
   a new request is automaticcaly issued."
  (fn [& args]
    (loop [{:keys [next-token] :as result} (apply f args)
           buffer {}]
      (if next-token
        (recur (apply f (-> args vec (conj :next-token next-token)))
               (merge-with into buffer (dissoc result :next-token)))
        (merge-with into buffer (dissoc result :next-token))))))

;; -----------------------------------------------------------------------------
;; CloudFormation
;; -----------------------------------------------------------------------------

(def cf-describe-stacks
  (-> cf/describe-stacks
      with-next-token
      with-regions))

(defn cf-process-stack [{:keys [creation-time
                                last-update-time
                                parameters
                                stack-id
                                stack-name
                                stack-status
                                tags]}]
  (let [stack {:aws.cf.stack/id stack-id
               :aws.cf.stack/name stack-name
;               :aws.stack/status stack-status
;               :parameters (record-to-map [:parameter-key :parameter-value]
;                                          parameters)
;               :tags (record-to-map [:key :value]
;                                    tags)
               }
;        last-change (or last-update-time creation-time)
;        stack (if last-change
;                (assoc stack :aws.stack/last-change (.getMillis last-change))
;                stack)
        ]
    stack))

(defn get-stacks []
  (->> (cf-describe-stacks)
       :stacks
       (map cf-process-stack)))
