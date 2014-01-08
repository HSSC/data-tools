(ns org.healthsciencessc.data-tools.i2b2.user-load.data
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :refer [info error]]
            [org.healthsciencessc.data-tools.i2b2.user-load.config :refer [config]]))

(def connection 
  {:classname "oracle.jdbc.OracleDriver"
   :subprotocol "oracle:thin"
   :subname (str "@" (config :database.host "localhost") ":" 
                 (config :database.port "1521") ":" 
                 (config :database.instance "db"))
   :user (:database.username config)
   :password (:database.password config)})

(def sql-select-users
  "SELECT USER_ID as \"user-id\",
          FULL_NAME as \"full-name\",
          PASSWORD as \"password\",
          EMAIL as \"email\",
          STATUS_CD as \"status\",
          CHANGE_DATE as \"change-date\",
          ENTRY_DATE as \"entry-date\",
          CHANGEBY_CHAR as \"change-by\"
     FROM PM_USER_DATA 
    WHERE USER_ID=?")


(def counter (atom 0))

(defn track
  "Just a way to progress provide feedback for every 25th user added to the database."
  [user]
  (swap! counter inc)
  (if (= 0 (mod @counter 25))
    (info "Added " @counter "th User:" (:user-id user) ", " (:full-name user))))

(defn add-users
  "Adds a collections of users to the database."
  [users]
  (jdbc/with-connection connection
    (jdbc/transaction
      (doseq [user users]
        (if (not (jdbc/with-query-results rs [sql-select-users (:user-id user)]
                   (first rs)))
          (do
            (jdbc/insert-records :PM_USER_DATA
                              {:USER_ID (:user-id user)
                               :FULL_NAME (:full-name user)
                               :PASSWORD (:password user)
                               :EMAIL (:email user)
                               :STATUS_CD (:status user)
                               :CHANGE_DATE (:change-date user)
                               :ENTRY_DATE (:entry-date user)
                               :CHANGEBY_CHAR (:change-by user)})
            (doseq [role (:roles user)]
              (jdbc/insert-records :PM_PROJECT_USER_ROLES
                              {:USER_ID (name (:user-id role))
                               :PROJECT_ID (name (:project-id role))
                               :USER_ROLE_CD (name (:role role))
                               :STATUS_CD (name (:status role))
                               :CHANGE_DATE (:change-date role)
                               :ENTRY_DATE (:entry-date role)
                               :CHANGEBY_CHAR (:change-by role)}))
            (track user))
          (info "USER EXISTS: " (:user-id user) ", " (:full-name user)))))))
