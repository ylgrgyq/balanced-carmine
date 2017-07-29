(defproject carmine-with-hash-key "0.1.0-RC1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.taoensso/carmine "2.16.0"]
                 [com.google.guava/guava "20.0"]]
  :main ^:skip-aot carmine-with-hash-key.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
