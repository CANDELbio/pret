(ns org.candelbio.pret.db.gc-storage-call
  (:require [datomic.api :as d]
            [org.candelbio.pret.db.config :as config]))

;;; Note: won't work in Datomic Cloud environment


(def db-list
  (d/get-database-names (str (config/base-uri) '*)))


(defn -main [& _args]
  (doseq [db db-list]
    (let [uri (str root-db-uri db)
          conn (d/connect uri)]
      (d/gc-storage conn (java.util.Date.)))))

(comment
  (-main))

