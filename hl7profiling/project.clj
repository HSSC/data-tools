(defproject hl7v2 "1.0.0-SNAPSHOT"
  :url "http://github.com/ghadishayban/doublehockeysticks"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :description "FIXME: write description"
  :main com.shayban.hl7v2.structure-analysis
  :jvm-opts ["-Xmx4096m"]
  :dependencies [[org.clojure/clojure "1.5.0-RC17"]
                 [clojure-csv "2.0.0-alpha2"]
                 [org.clojure/tools.cli "0.2.2"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/core.cache "0.6.3-SNAPSHOT"]
                 [org.codehaus.jsr166-mirror/jsr166y "1.7.0"]]
  :repositories [["sonatype" {:snapshots true
                              :url "https://oss.sonatype.org/content/groups/public/"}]]
  :profiles {:dev {:dependencies [[criterium "0.3.0-SNAPSHOT"]]}})
