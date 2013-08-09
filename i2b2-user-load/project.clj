(defproject org.healthsciencessc.data-tools/i2b2-user-load "0.1.0-SNAPSHOT"

  :description "A utility for uploading a list of users into the I2B2 PM database with a given set of permissions."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :main org.healthsciencessc.data-tools.i2b2.user-load.core

  :source-paths ["src/clj"]
  :resource-paths ["src/resources"]
  :test-paths ["test/clj"]

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [pliant/configure "0.1.2-SNAPSHOT"]
                 [pliant/process "0.1.1-SNAPSHOT"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [org.clojure/tools.logging "0.2.3"]
                 [ojdbc "6"]])
