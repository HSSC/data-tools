(ns org.healthsciencessc.data-tools.i2b2.user-load.core
  (:require [org.healthsciencessc.data-tools.i2b2.user-load.config :refer [config]]
            [org.healthsciencessc.data-tools.i2b2.user-load.values :refer [get-user get-authority]]
            [org.healthsciencessc.data-tools.i2b2.user-load.data :refer [add-users]]
            [clojure.tools.logging :refer [info error]]
            [clojure.java.io :refer [as-file]]
            [clojure.string :refer [split]]))


(defn obtain-users
  [file-name]
  (let [raw (slurp file-name)
        records (split raw #"\n")]
    (info "There are " (count records) " user records in the file " file-name ".")
    (map #(clojure.string/split % #"\t") records)))


(defn -main
  [& args]
  (let [file-name (or (config :source.file) (first args) "/temp/i2b2-user-load.csv")]
    (if (.exists (as-file file-name))
      (->> (obtain-users file-name) 
        (map get-user) 
        (filter identity) 
        add-users)
      (error "The file " file-name " can not be found."))))
