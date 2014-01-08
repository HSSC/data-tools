(ns org.healthsciencessc.data-tools.i2b2.user-load.values
  (:require [clojure.string :refer [trim]]))


(defn get-userid
  [r]
  (trim (first r)))

(defn get-name
  [r]
  (str (trim (nth r 1)) " " (trim (nth r 2))))

(defn get-email
  [r]
  (trim (first r)))


;; A vector of role codes that are assigned to each user/project combination
(def roles [:USER :DATA_DEID :DATA_OBFSC :DATA_AGG :DATA_LDS])

;; A map of project/roles that every user is assigned
(def base-project-roles {:HSSC roles})

;; A set of valid domains that user ids can be from. ie - @musc.edu
(def domains #{"@clemson.edu" "@ghs.org" "@musc.edu" "@palmettohealth.org" "@sc.edu" "@email.sc.edu" "@srhs.com"})

;; A mapping of domains to mappings of project/roles that are given to users whose ID falls within that domain.
(def domain-roles {"@ghs.org" {:GHS roles}
                   "@musc.edu" {:MUSC roles}
                   "@palmettohealth.org" {:PH roles}
                   "@srhs.com" {:SRHS roles}})


(defn get-project-roles
  "Gets the project and role mappings for a user in form of a map where the keys are
  the project names and the values are vectors of roles they have for that project."
  [r]
  (let [domain (re-find #"@.*" (get-userid r))]
    (if (contains? domains domain)
      (merge base-project-roles (domain-roles domain)))))


(defn get-status
  "Gets the status for  the record.  Default is A"
  [r]
  "A")


(defn get-password
  "Gets the password to use for the user.  Default is demouser"
  [r]
  "9117d59a69dc49807671a51f10ab7f")

(defn get-authority
  [r]
  (let [uid (get-userid r)]
    (reduce
     (fn [s [k v]]
       (concat s (map #(hash-map :user-id uid :project-id k :role % :status (get-status r)) v)))
     '() (get-project-roles r))))

(defn get-user
  [r]
  (let [domain (re-find #"@.*" (get-userid r))]
    (if (contains? domains domain)
      {:user-id (get-userid r)
       :full-name (get-name r)
       :password (get-password r)
       :email (get-email r)
       :status (get-status r)
       :roles (get-authority r)})))




