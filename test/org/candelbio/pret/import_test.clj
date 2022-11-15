(ns org.candelbio.pret.import-test
  (:require [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [datomic.api :as d]
            [org.candelbio.pret.util.text :as text]
            [org.candelbio.pret.db :as db]
            [org.candelbio.pret.util.io :as util.io]
            [org.candelbio.pret.util.aws :as aws]
            [org.candelbio.pret.import :as import]
            [org.candelbio.pret.validation.post-import :as post-import]))

(defn untar!
  "Untars target file using CLI tar (`which tar` to find what will be used).

  Note: similar to batch CLI untar, but no coordination around batch data directories."
  [tar-file]
  (let [par-dir (.getParent (io/file tar-file))
        {:keys [exit out err]} (sh "tar" "xzvf" (.getName (io/file tar-file)) :dir par-dir)]
    (when-not (zero? exit)
      (throw (ex-info (str "tar execution did not run as expected: " err)
                      {:std-err err
                       :std-out out})))))

(def template-dir "test/resources/reference-import/template")
(def template-file "test/resources/reference-import/template.tar.gz")

(defn ensure-template-dataset! []
  (let [s3-bucket "cdel-db-ops"
        s3-key "test/datasets/template.tar.gz"
        s3-region :us-east-1
        target-file template-file]
    (try (aws/get-file s3-bucket s3-key target-file s3-region)
         (catch Exception e
           (binding [*out* *err*]
             (println "ERROR: Test could not download template dataset for integration tests. PICI AWS creds are required!"))
           (System/exit 1)))
    (untar! target-file)
    true))

(def import-cfg-file
  "test/resources/reference-import/template/config.edn")

(def dataset-name
  (memoize
    (fn []
      (ensure-template-dataset!)
      (-> import-cfg-file
          (util.io/read-edn-file)
          (get-in [:dataset :name])))))

(def datomic-uri
  (str "datomic:mem://int-tests"))

(def tmp-dir "tmp-output")

(defn setup []
  (ensure-template-dataset!)
  (log/info "Initializing in-memory integration test db.")
  (db/init datomic-uri))

(defn teardown []
  (log/info "Ending integration test and deleting db.")
  (d/delete-database datomic-uri)
  (util.io/delete-recursively template-dir)
  (io/delete-file template-file))

(deftest ^:integration sanity-test
  (try
    (setup)
    (let [import-result (import/run
                           {:target-dir tmp-dir
                            :datomic-uri datomic-uri
                            :import-cfg-file import-cfg-file
                            :disable-remote-calls true
                            :tx-batch-size 50})]
      (testing "Import runs to completion without throwing."
        (is import-result))
      (testing "Right number of txes completed. This implicitly also tests for data import failures."
        (is (= 3464 (get-in import-result [:results :completed]))))
      (testing "No reference data import errors."
        (is (not (seq (:errors import-result)))))
      (testing "Validation runs with expected failures (until test updated)."
        (Thread/sleep 2000)
        (let [dataset-name (dataset-name)
              db-info {:uri datomic-uri}]
          (is (not (seq (post-import/run-all-validations db-info dataset-name)))))))

    (catch Exception e
      (log/error "Test threw during import attempt "
                 :message (.getMessage e)
                 :ex-data (text/->pretty-string (ex-data e)))
      (throw e))
    (finally
      (try
        (util.io/delete-recursively tmp-dir)
        (teardown)
        (catch Exception e
          (log/error (.getMessage e)))))))



(comment
  ;; comment has template for getting queries on db fast, comment out
  ;; teardown call above in finally then after eval on run-tests this will
  ;; set you up for query
  (run-tests *ns*))
