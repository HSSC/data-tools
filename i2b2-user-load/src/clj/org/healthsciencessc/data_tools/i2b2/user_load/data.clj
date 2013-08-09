(ns org.healthsciencessc.data-tools.i2b2.user-load.data
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :refer [info error]]
            [org.healthsciencessc.data-tools.i2b2.user-load.config :refer [config]]))

(def pm-db
  {:classname (config :db.classname "oracle.jdbc.OracleDriver")
   :subprotocol (config :db.subprotocol "oracle:thin")
   :subname (config :db.classname "@server:1521:db")
   :user (config :db.user "pmuser")
   :password (config :db.password "demouser")})


(def sql-select-users
  "SELECT USER_ID as \"user-id\",
          FULL_NAME as \"full-name\",
          PASSWORD as \"password\",
          EMAIL as \"email\",
          STATUS_CD as \"status\",
          CHANGE_DATE as \"change-date\",
          ENTRY_DATE as \"entry-date\",
          CHANGEBY_CHAR as \"change-by\"
     FROM PM_USER_DATA ")


(defn get-user
  "Gets a user by it's ID."
  [id]
  (jdbc/with-connection pm-db
    (jdbc/with-query-results rs [(str sql-select-users " WHERE USER_ID=?") id]
      (first rs))))


(defn create-user
  "Adds a user to I2B2"
  [user]
  (jdbc/with-connection pm-db
    (jdbc/transaction
      (jdbc/insert-records :PM_USER_DATA
                          {:USER_ID (:user-id user)
                           :FULL_NAME (:full-name user)
                           :PASSWORD (:password user)
                           :EMAIL (:email user)
                           :STATUS_CD (:status user)
                           :CHANGE_DATE (:change-date user)
                           :ENTRY_DATE (:entry-date user)
                           :CHANGEBY_CHAR (:change-by user)}))))

(defn create-user-roles
  "Adds a user to I2B2"
  [roles]
  (jdbc/with-connection pm-db
    (jdbc/transaction
      (doseq [role roles]
        (jdbc/insert-records :PM_PROJECT_USER_ROLES
                          {:USER_ID (name (:user-id role))
                           :PROJECT_ID (name (:project-id role))
                           :USER_ROLE_CD (name (:role role))
                           :STATUS_CD (name (:status role))
                           :CHANGE_DATE (:change-date role)
                           :ENTRY_DATE (:entry-date role)
                           :CHANGEBY_CHAR (:change-by role)})))))


(def counter (atom 0))

(defn track
  [user-id]
  (swap! counter inc)
  (if (= 0 (mod @counter 25))
    (info "Added " @counter "th User:" user-id)))

(defn add-user
  [user auths]
  (if user
    (if (not (get-user (:user-id user)))
      (do
        (create-user user)
        (create-user-roles auths)
        (track (:user-id user)))
      (info "USER EXISTS: " (:user-id user) ", " (:full-name user)))))

(defn delete-users
  []
  (jdbc/with-connection pm-db
    (jdbc/transaction
     (jdbc/delete-rows :PM_USER_DATA ["USER_ID like 'test%'"])
     (jdbc/delete-rows :PM_PROJECT_USER_ROLES ["USER_ID like 'test%'"]))))

#_(delete-users)
