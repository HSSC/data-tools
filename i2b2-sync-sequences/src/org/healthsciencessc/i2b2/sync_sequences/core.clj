(ns org.healthsciencessc.i2b2.sync-sequences.core
  (:gen-class)
  (:require [pliant.configure.props :refer (slurp-config)]
            [pliant.configure.sniff :refer (sniff)]
            [clojure.java.jdbc :as jdbc]))

(def config
  (slurp-config "i2b2-sync-sequences.props"
    (sniff "SYNCSEQUENCES")))

(def seq-map {"SQ_UPLOADSTATUS_UPLOADID" {:table "upload_status" 
                                          :column "upload_id"}
              "QT_SQ_QXR_XRID"   {:table "QT_XML_RESULT" 
                                  :column "xml_result_id"}
							"QT_SQ_QRI_QRIID"  {:table "QT_QUERY_RESULT_INSTANCE" 
							                    :column "RESULT_INSTANCE_ID"}
							"QT_SQ_QPR_PCID"   {:table "qt_patient_set_collection" 
							                    :column "patient_set_coll_id"}
							"QT_SQ_QPER_PECID" {:table "qt_patient_enc_collection" 
							                    :column "patient_enc_coll_id"}
							"QT_SQ_QM_QMID"    {:table "QT_QUERY_MASTER" 
							                    :column "QUERY_MASTER_ID"}
							"QT_SQ_QI_QIID"    {:table "QT_QUERY_INSTANCE" 
							                    :column "QUERY_INSTANCE_ID"}
							"QT_SQ_PQM_QMID"   {:table "QT_PDO_QUERY_MASTER" 
							                    :column "QUERY_MASTER_ID"}
							#_"ONT_SQ_PS_PRID"   #_{:table "ONT_PROCESS_STATUS" 
							                    :column "process_id"}})

(defn valid-configuration?
  []
  (cond
    (clojure.string/blank? (:database.host config)) 
      (do 
        (println "Missing configuration value for database.host property.") 
        false)
    (clojure.string/blank? (:database.port config)) 
      (do 
        (println "Missing configuration value for database.port property.") 
        false)
    (clojure.string/blank? (:database.instance config)) 
      (do 
        (println "Missing configuration value for database.instance property.") 
        false)
    (clojure.string/blank? (:database.username config)) 
      (do 
        (println "Missing configuration value for database.username property.") 
        false)
    (clojure.string/blank? (:database.password config)) 
      (do 
        (println "Missing configuration value for database.password property.  If using an admin account with no password, you should be ashamed.") 
        false)
    :else true))

(def connection 
  {:classname "oracle.jdbc.OracleDriver"
   :subprotocol "oracle:thin"
   :subname (str "@" (:database.host config) ":" (:database.port config) ":" (:database.instance config))
   :user (:database.username config)
   :password (:database.password config)})

(def select-seqs (str "SELECT SEQUENCE_OWNER owner, SEQUENCE_NAME name, LAST_NUMBER last FROM SYS.ALL_SEQUENCES "
                      (if (clojure.string/blank? (:sequence.where config)) 
                        "WHERE SEQUENCE_OWNER LIKE 'I2B2%'"
                        (:sequence.where config))))

(defn clean-max
  [rs default-value]
  (if-let [max (-> rs first :m)]
    (if (> max 1)
      max
      default-value)
    default-value))

(defn alter-sql
  [owner sequence start-at]
  [(str "DROP SEQUENCE " owner "." sequence)
   (str "CREATE SEQUENCE " owner "." sequence " INCREMENT BY 1 START WITH " start-at)])

(defn update-sequences
  [sequences]
  (jdbc/with-db-transaction [db connection]
    (doseq [{name :name owner :owner last :last} sequences]
      (let [table (get-in seq-map [name :table])
            column (get-in seq-map [name :column])
            max (clean-max (jdbc/query db [(str "SELECT MAX (" column ") m FROM " owner "." table)]) 1)]
        (println (str "AUDIT: " owner "." name ", last=" last ", max=" max ", diff=" (- max last)))
        (if (> max last)
          (doseq [sql (alter-sql owner name (inc max))]
            (jdbc/execute! db [sql])))))))


(defn -main
  "Entry point to sync sequences.  If running from lein, use lein with-profile local run."
  [& args]
  (when (valid-configuration?)
    (->> 
      (jdbc/query connection [select-seqs])
      (filter #(seq-map (:name %)))
      update-sequences))
  (System/exit 0))

