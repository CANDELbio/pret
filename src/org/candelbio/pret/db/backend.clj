(ns org.candelbio.pret.db.backend
  (:require [datomic.api :as d]
            [org.candelbio.pret.db.config :as db.config]))


(defn ddb-base-uri []
  (str "datomic:ddb://"
       (db.config/aws-region) "/"
       (db.config/ddb-table) "/"))

(defn db-base-uri []
  (or (db.config/base-uri)
      (ddb-base-uri)))

(defn request-db
  [database]
  (let [uri (str (db-base-uri) database)
        result (d/create-database uri)]
    (if result
      {:db-name database
       :database database
       :uri uri}
      {:error "Database already exists!"})))

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

(defn database-info
  "Retrieves the branch database's datomic uri.
  Returns the uri, {:error ...} or throws an exception if the user doesn't have
  access permissions to the branch database."
  [database]
  {:uri (str (db-base-uri) database)})

(defn list-dbs []
  (try
    (d/get-database-names (str (db-base-uri) "*"))
    (catch Exception e
      {:error (.getMessage e)})))



