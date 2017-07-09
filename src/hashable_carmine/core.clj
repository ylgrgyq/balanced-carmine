(ns hashable-carmine.core
  (:require [taoensso.carmine :as car]
            [taoensso.carmine.commands :as cmds]
            [hashable-carmine.balancer :as balancer])
  (:import (com.google.common.hash Hashing HashCode)))

(defn- get-spec [hash-key balancer candidate-specs]
  (if (string? hash-key)
    (balancer/choose-spec balancer hash-key candidate-specs)
    (first candidate-specs)))

(def ^:private default-load-balancer (balancer/->ConsistentHashBalancer))

(defn- parse-hash-key [args]
  (when-not (or (= :as-pipeline (first args))
           (list? (first args)))
    (first args)))

(defmacro wcar
  "It's the same as taoensso.carmine/wcar, but supports
      :specs-group [{:host \"127.0.0.1\" :port 6379}
                    {:host \"127.0.0.1\" :port 6380}
                    {:host \"127.0.0.1\" :port 6381}]
   in conn for redis load balance.
  "
  {:arglists '([conn-opts hash-key :as-pipeline & body]
               [conn-opts hash-key & body]
               [conn-opts :as-pipeline & body]
               [conn-opts & body])}
  [conn-opts & args] ; [conn-opts & [a1 & an :as args]]
  (let [hash-key (gensym 'hash-key)]
    `(let [{specs-group# :specs-group balancer# :load-balancer
            :or          {balancer# default-load-balancer}} ~conn-opts
           ~hash-key  ~(parse-hash-key args)
           spec#      (get-spec ~hash-key
                                balancer#
                                specs-group#)
           conn-opts# (assoc ~conn-opts :spec spec#)]
       (car/wcar conn-opts#
                 ~(if hash-key
                    `(do ~@(rest args))
                    `(do ~@args))))))

(comment
  (let [pool         {:max-total-per-key (* 3 (.availableProcessors (Runtime/getRuntime)))
                      :min-idle-per-key  (* 3 (.availableProcessors (Runtime/getRuntime)))
                      :max-wait-ms       5000}
        specs        [{:host "127.0.0.1" :port 6379}
                      {:host "127.0.0.1" :port 6380}
                      {:host "127.0.0.1" :port 6381}]
        server1-conn {:pool pool :spec {} :specs-group specs}]
    (println
      (wcar server1-conn "sss"
            (car/set "a" 100)
            (car/get "a")
            (car/del "a")))))

(comment
  (def pool {:max-total-per-key (* 3 (.availableProcessors (Runtime/getRuntime)))
             :min-idle-per-key  (* 3 (.availableProcessors (Runtime/getRuntime)))
             :max-wait-ms       5000})
  (def specs [{:host "127.0.0.1" :port 6379}
              {:host "127.0.0.1" :port 6380}
              {:host "127.0.0.1" :port 6381}])
  (defmacro wcar*
    [& body]
    `(wcar {:pool pool
            :specs-group specs}
           ~@body))
  (println
    (wcar* "hello"
          (car/set "a" 100)
          (car/get "a"))))




