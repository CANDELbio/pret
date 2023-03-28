(ns org.candelbio.pret.db.backend
  (:require [datomic.client.api :as d]
            [org.candelbio.pret.db.config :as db.config]))


;;; MT note: I don't understand why this was carved out of db

;;; Datomic Cloud config
;;; TODO shouldn't be in code
(def cfg {:server-type :ion
          :region "us-east-1" 
          :system "PublicCANDEL5"
          ;; This is from the Datomic Cloud ClientApiGatewayEnpoint output
          :endpoint "https://nazpex6ueb.execute-api.us-east-1.amazonaws.com"
          ;; :creds-profile "<your_aws_profile_if_not_using_the_default>"
          })

(def client (d/client cfg))

(defn connect
  [db-name]
  #_ (d/connect datomic-uri)
  (d/connect client {:db-name db-name})
  )

(defn db
  [name]
  (d/db (connect name)))


#_
(defn ddb-base-uri []
  (str "datomic:ddb://"
       (db.config/aws-region) "/"
       (db.config/ddb-table) "/"))

#_
(defn db-base-uri []
  (or (db.config/base-uri)
      (ddb-base-uri)))

(defn request-db
  [database]
  (let [;; uri (str (db-base-uri) database)
        result #_ (d/create-database uri)
        (d/create-database client {:db-name database})
        ]
    (if result
      {:db-name database
       :database database
       :uri database}                   ;Not really
      {:error "Database already exists!"})))

#_
(defn delete-db
  [database]
  (let [uri (str (db-base-uri) database)
        result (d/delete-database uri)]
    (if result
      {:success true
       :db-name database
       :database database
       :uri uri}
      {:error "Database not deleted!"})))

(defn delete-db
  [db]
  (d/delete-database client {:db-name db}))

#_
(defn database-info
  "Retrieves the branch database's datomic uri.
  Returns the uri, {:error ...} or throws an exception if the user doesn't have
  access permissions to the branch database."
  [database]
  {:uri (str (db-base-uri) database)})

(defn db-stats
  [db]
  (d/db-stats (d/db (d/connect client {:db-name db}))))

;;; This is not terribly useful, but cli could pprint it.
(defn database-info
  [db]
  (db-stats db))

;;; Curation functions.

(defn all-dbs
  []
  (d/list-databases client {}))

(defn list-dbs []
  (all-dbs))

(defn all-db-stats
  []
  (let [dbs (all-dbs)]
    (zipmap dbs (map db-stats dbs))))

(defn print-db-stats
  []
  (doseq [db (all-dbs)]
    (prn :db db)
    (clojure.pprint/pprint (db-stats db))))


  

(defn delete-all-dbs
  []
  (doseq [db (all-dbs)]
    (delete-db db)))

(defn create-database
  [name]
  (d/create-database client {:db-name name}))
