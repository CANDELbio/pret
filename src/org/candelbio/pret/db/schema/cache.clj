(ns org.candelbio.pret.db.schema.cache
  (:require [org.candelbio.pret.db.schema :as schema]
            [org.candelbio.pret.db :as db]
            [org.candelbio.pret.util.uuid :as uuid]
            [datomic.api :as d]))

(defn temp-uri []
  (str "datomic:mem://" (uuid/random)))

(defn update-cached-schema
  []
  (let [db-uri (temp-uri)
        _ (db/init db-uri :skip-bootstrap true)
        conn (d/connect db-uri)
        db (d/db conn)
        updated-schema (schema/get-metamodel-and-schema db)]
    (schema/cache updated-schema)))

(defn -main [& args]
  (println "Re-caching schema.")
  (update-cached-schema)
  (println "Schema caching completed.")
  (System/exit 0))

(comment
  (-main))
