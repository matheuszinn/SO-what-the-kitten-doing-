(defproject what-the-kitty-doing-lein "0.1.1-SNAPSHOT"
  :description "Concurrency and threads are cute as kittens"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.json "2.4.0"]
                 [clj-http/clj-http "3.12.3"]]

  :profiles {:uberjar {:aot :all}}
  :main what-the-kitty-doing-lein.core
  :repl-options {:init-ns what-the-kitty-doing-lein.core})
