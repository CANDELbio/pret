(ns org.candelbio.pret.import.tuple-test
  (:require [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [datomic.api :as d]
            [org.candelbio.pret.bootstrap.data :as bootstrap.data]
            [org.candelbio.pret.util.io :as util.io]
            [clojure.java.io :as io]
            [org.candelbio.pret.db :as db]
            [org.candelbio.pret.import :as import]))

(def tmp-dir
  "tuple-tmp-output")

(def datomic-uri
  (str "datomic:mem://tuple-tests"))

(def import-cfg-file
  "test/resources/tuple-import/config.edn")

(defn bootstrap-genes []
  (first
    (filter #(= :genes (:name %)) (bootstrap.data/all-datasets))))

(defn setup []
  (log/info "Initializing in-memory tuple test db.")
  (db/init datomic-uri :skip-bootstrap true)
  (log/info "Bootstrap gene/HGNC data only.")
  (let [conn (d/connect datomic-uri)
        version (db/version datomic-uri)
        genes-data (bootstrap-genes)]
    (bootstrap.data/maybe-download version genes-data)
    (doseq [f (:files genes-data)]
      (db/transact-bootstrap-data
        conn
        (io/file bootstrap.data/seed-data-dir f)))))

(defn teardown []
  (log/info "Ending tuple test and deleting db.")
  (d/delete-database datomic-uri)
  (util.io/delete-recursively tmp-dir))

(deftest tuple-import
  (setup)
  (is (:results (import/run
                  {:target-dir tmp-dir
                   :datomic-uri datomic-uri
                   :import-cfg-file import-cfg-file
                   :disable-remote-calls true
                   :tx-batch-size 50})))
  (teardown))


(comment
  (run-tests *ns*))