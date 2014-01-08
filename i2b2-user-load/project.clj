(defproject org.healthsciencessc.data-tools/i2b2-user-load "0.1.0-SNAPSHOT"

  :description "A utility for uploading a list of users into the I2B2 PM database with a given set of permissions."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :main org.healthsciencessc.data-tools.i2b2.user-load.core
  :aot [org.healthsciencessc.data-tools.i2b2.user-load.core]

  :resource-paths ["local"]
  
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [pliant/configure "0.1.2"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [org.clojure/tools.logging "0.2.3"]
                 [ojdbc "6"]])
