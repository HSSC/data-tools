(defproject org.healthsciencessc.i2b2/sync-sequences "0.1.0-SNAPSHOT"
  :description "A utility project that will sync the internal sequences to the actual values 
                used after cloning data from one environment to another."
  
  :url "http://github.com/HSSC/data-tools"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.3.2"]
                 [pliant/configure "0.1.2"]
                 [ojdbc "6"]]
  
  :main org.healthsciencessc.i2b2.sync-sequences.core
  :aot [org.healthsciencessc.i2b2.sync-sequences.core]
  
  :profiles {:local {:resource-paths ["local"]}})
