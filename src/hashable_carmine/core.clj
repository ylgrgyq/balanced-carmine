(ns hashable-carmine.core
  (:require [taoensso.carmine :as car]
            [taoensso.carmine.commands :as cmds])
  (:import (com.google.common.hash Hashing HashCode)))

(defn- get-spec [hash-key hash-fn candidate-specs]
  (println "get spec" hash-key candidate-specs)
  (if (string? hash-key)
    (hash-fn hash-key candidate-specs)
    (first candidate-specs)))

(defn default-hash-fn [hash-key candidate-specs]
  (nth candidate-specs
       (-> (.getBytes ^String hash-key)
           (HashCode/fromBytes)
           (Hashing/consistentHash (count candidate-specs)))))

(defn parse-hash-key [args]
  (when-not (list? (first args))
    (first args)))

(defmacro wcar
  {:arglists '([conn-opts :as-pipeline & body] [conn-opts & body])}
  [conn-opts & args]                                        ; [conn-opts & [a1 & an :as args]]
  `(let [{specs-group# :specs-group hash-fn# :hash-fn
          :or          {hash-fn# default-hash-fn}} ~conn-opts
         spec#      (get-spec ~(when-not (list? (first args))
                                 (first args))
                              default-hash-fn
                              specs-group#)
         conn-opts# (assoc ~conn-opts :spec spec#)]
     (println "conn opts" conn-opts#)
     (car/wcar conn-opts#
       ~@args)))

#_(defmacro wcar*
  [& body]
  `(let [spec# (get-spec ~(when-not (list? (first body))
                            (first body)))]
     (try
       (diehard/with-circuit-breaker (:breaker spec#)
                                     (car/wcar {:pool pool :spec spec#}
                                               ~@body))
       (catch CircuitBreakerOpenException _#
         (log/warn "Offline redis disabled with circuit breaker" (select-keys spec# [:host :port])))
       (catch Exception e#
         (log/warn e# "Failed to call offline redis" (select-keys spec# [:host :port]))))))

(comment
  (let [pool {:max-total-per-key (* 3 (.availableProcessors (Runtime/getRuntime)))
              :min-idle-per-key (* 3 (.availableProcessors (Runtime/getRuntime)))
              :max-wait-ms 5000}
        specs [{:host "127.0.0.1" :port 6379}
               {:host "127.0.0.1" :port 6380}
               {:host "127.0.0.1" :port 6381}]
        server1-conn {:pool pool :spec {} :specs-group specs}]
    (println
      (wcar server1-conn "sss"
            (car/set "a" 100)
            (car/get "a")
            #_(car/del "a")
            ))))


