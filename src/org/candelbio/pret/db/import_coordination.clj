(ns org.candelbio.pret.db.import-coordination
  (:require [datomic.client.api :as d]
            [org.candelbio.pret.db.query :as dq]))

(defn import-entity-txn-eid
  "Return the txn-id for the import entity defined by the job
  named import-name"
  [db import-name]
  (let [result (ffirst (dq/q+retry '[:find ?import
                                     :in $ ?import-name
                                     :where
                                     [?import :import/name ?import-name]]
                                   db import-name))]
     result))


(comment
  ;; for reference purposes at the moment, these two queries are slower than is practical
  ;; for large imports on database containing multiple imports already.
  (def tx-uuids-for-import
    '[:find ?uuid
      :in $ ?import-name
      :where
      [?import :import/name ?import-name]
      [?txn :import/import ?import]
      [?txn :import/txn-id ?uuid]])

  (def all-tx-uuids
    '[:find ?uuid
      :in $
      :where
      [?txn :import/txn-id ?uuid]]))

(def first-import-tx-q
  '[:find ?tx
    :in $ ?name
    :where
    [_ :import/name ?name ?tx]])

(def after-tx-q
  '[:find ?uuid
    :in $ ?start-tx
    :where
    [(> ?tx ?start-tx)]
    [?tx :import/txn-id ?uuid]])

(defn imported-uuids-q
  "Return all txn-ids for transactions put into the database after the start of
  the job named import-name"
  [db import-name]
  (let [first-import-tx (ffirst (dq/q+retry first-import-tx-q db import-name))]
    (dq/q+retry after-tx-q db first-import-tx)))

;; cache uuid set results so multiple calls return same results
(def successful-uuids
  (atom nil))

(defn successful-uuid-set
  "Return a set of all transaction UUIDs already in the database for the import
  defined by import-name"
  [db import-name {:keys [invalidate] :as opts}]
  (if-let [uuid-set (and (not invalidate)
                         @successful-uuids)]
    uuid-set
    (let [uuid-q-results (if-let [import-ent-uid
                                  (import-entity-txn-eid db import-name)]
                           (->> (imported-uuids-q db import-name)
                                (map first)
                                (into #{}))
                           #{})]
      (reset! successful-uuids uuid-q-results))))


(comment
  (def db-uri "datomic:ddb://us-east-1/cdel-test-tcga/my-new-db")
  (def conn (d/connect db-uri))
  (def db (d/db conn))
  (successful-uuid-set db "pici0025-import" {:invalidate false})
  (import-entity-txn-eid db "pici0025-import")
  (flatten (imported-uuids-q db "pici0025-import")))
