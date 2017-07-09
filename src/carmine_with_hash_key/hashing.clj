(ns carmine-with-hash-key.hashing)

(defn- linear-congruential-next-double
  "Translate from https://github.com/google/guava/blob/v20.0/guava/src/com/google/common/hash/Hashing.java#L691"
  [^long state]
  (println state (type state) (unchecked-multiply state (long 2862933555777941757)))
  (let [state (+ (unchecked-multiply state (long 2862933555777941757)) 1)]
    (* )
    [state
     (/ (double (+ (int (unsigned-bit-shift-right state 33)) 1))
        2.147483648E9)]))

(defn- consistent-hash
  "Translate from https://github.com/google/guava/blob/v20.0/guava/src/com/google/common/hash/Hashing.java#L522"
  [input buckets-count]
  (loop [state input candidate 0]
    (let [[state next-double] (linear-congruential-next-double state)
          next (int (/ (double (+ candidate 1)) next-double))]
      (if (or (neg? next) (>= next buckets-count))
        candidate
        (recur state next)))))

(defn- hash-code
  "Translate from https://github.com/google/guava/blob/v20.0/guava/src/com/google/common/hash/HashCode.java#L259"
  [^String key]
  (let [bs (.getBytes key)
        ret (long (bit-and (first bs) 0xFF))
        index-limit (min (count bs) 8)]
    (loop [ret ret index 1]
      (if (< index index-limit)
        (let [b (bit-shift-left (bit-and (long (get bs index)) 0xFF) (* index 8))]
          (recur (bit-or ret b) (inc index)))
        ret))))

(defprotocol Hasher
  (choose-spec [this hash-key candidate-specs]))

(defrecord ConsistentHasher []
  Hasher
  (choose-spec [this hash-key candidate-specs]
    (nth candidate-specs
         (consistent-hash (hash-code hash-key)
                          (count candidate-specs)))))