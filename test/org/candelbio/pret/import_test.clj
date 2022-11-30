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


(def template-dir "test/resources/reference-import/template-dataset")

(defn clone-template!
  []
  (sh "git" "clone" "git@github.com:CANDELbio/template-dataset.git"
      :dir "test/resources/reference-import/"))

(defn local-template []
  (when-let [root-dir (System/getenv "CANDEL_TEST_DATA_DIR")]
    (str root-dir "/template")))

(defn ensure-template-dataset! []
  (when-not (local-template)
    (clone-template!)))

(defn import-cfg-file []
  (if-let [template-root (local-template)]
    (str template-root "/config.edn")
    "test/resources/reference-import/template-dataset/config.edn"))

(def dataset-name
  (memoize
    (fn []
      (ensure-template-dataset!)
      (-> (import-cfg-file)
          (util.io/read-edn-file)
          (get-in [:dataset :name])))))

(def datomic-uri
  (str "datomic:mem://int-tests"))

(def tmp-dir "tmp-output")

(defn setup []
  (ensure-template-dataset!)
  (log/info "Initializing in-memory integration test db.")
  (db/init datomic-uri)
  ;; Temp fix for drug ordering issue with template dataset.
  (let [conn (d/connect datomic-uri)]
    @(d/transact conn [{:drug/preferred-name "PEMBROLIZUMAB"}])))

(defn teardown []
  (log/info "Ending integration test and deleting db.")
  (d/delete-database datomic-uri)
  (util.io/delete-recursively template-dir))

(deftest ^:integration sanity-test
  (try
    (setup)
    (let [import-result (import/run
                           {:target-dir tmp-dir
                            :datomic-uri datomic-uri
                            :import-cfg-file (import-cfg-file)
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
