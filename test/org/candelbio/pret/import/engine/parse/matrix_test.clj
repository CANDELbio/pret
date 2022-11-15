(ns org.candelbio.pret.import.engine.parse.matrix-test
  (:require [org.candelbio.pret.db.schema :as schema]
            [org.candelbio.pret.import.engine.parse.matrix :as sut]
            [org.candelbio.pret.import.engine.parse.config :as parse.config]
            [org.candelbio.pret.import.engine.parse.mapping :as parse.mapping]
            [org.candelbio.pret.util.io :as util.io]
            [clojure.test :refer :all]))


(def pret-schema (schema/get-metamodel-and-schema))
(def config-file "test/resources/matrix/config.edn")
(def config-map (util.io/read-edn-file config-file))
(def parsed-cfg
  (parse.config/parse-config-map pret-schema config-map))
(def mappings (util.io/read-edn-file "test/resources/matrix/mappings.edn"))
(def mappings (parse.mapping/mappings-edn->lookup mappings))

(def job-context
  {:parsed-cfg parsed-cfg
   :schema pret-schema
   :mapping mappings
   :import-name "matrix-blah"})

(def mtx-job-1
  {:pret/node-kind :measurement-matrix
   :pret/ns-node-ctx [:dataset :dataset/assays 0
                      :assay/measurement-sets 0
                      :measurement-set/measurement-matrices 0]
   :pret.matrix/input-file "test/resources/matrix/short-processed-counts.tsv"
   :pret.matrix/format :pret.matrix.format/sparse
   :pret.matrix/indexed-by {"barcode" :measurement-matrix/single-cells
                            "hugo" :measurement-matrix/gene-products}
   :measurement-matrix/measurement-type :measurement/read-count
   :measurement-matrix/name "sparse example"
   :pret.matrix/constants {:measurement-matrix/samples ["SOME ID"]}})

(def mtx-job-2
  {:pret/node-kind :measurement-matrix
   :pret/ns-node-ctx [:dataset :dataset/assays 0
                      :assay/measurement-sets 0
                      :measurement-set/measurement-matrices 1]
   :pret.matrix/input-file "test/resources/matrix/dense-rnaseq.tsv"
   :pret.matrix/format :pret.matrix.format/dense
   :pret.matrix/column-attribute :measurement-matrix/gene-products
   :pret.matrix/indexed-by {"sample.id" :measurement-matrix/samples}
   :measurement-matrix/name "dense example"
   :measurement-matrix/measurement-type :measurement/fpkm})

(defn- lookup-ref?
  [v]
  (and (seq v)
       (= 2 (count v))
       (keyword? (first v))))

(defn- entity-map-ref?
  [m]
  (and (map? m)
       (= 1 (count (keys m)))))

(defn- entity-ref?
  [m-or-v]
  (or (lookup-ref? m-or-v)
      (entity-map-ref? m-or-v)))

(deftest sparse_matrix_test
  (testing "sparse matrix parsing"
    (let [result (sut/matrix->entity job-context mtx-job-1)]
      (testing "parsed sparse matrix map returns necessary keys for copying the matrix file"
        (is (:pret.matrix/input-file result))
        (is (:pret.matrix/output-file result)))
      (testing "resolves UID"
        (is (= ["matrix-test" "rna-seq|:~rna seq data|:~sparse example"]
               (:measurement-matrix/uid result))))
      (testing "generates backing file key"
        (is (:measurement-matrix/backing-file result)))
      (testing "all attributes specified exist in parsed map"
        (is (:measurement-matrix/samples result))
        (is (:measurement-matrix/backing-file result))
        (is (:measurement-matrix/single-cells result))
        (is (:measurement-matrix/gene-products result))
        (is (:measurement-matrix/name result))
        (testing "and have been resolved to lookup refs when appropriate"
          (is (every? entity-ref? (:measurement-matrix/samples result)))
          (is (every? entity-ref? (:measurement-matrix/single-cells result)))
          (is (every? entity-ref? (:measurement-matrix/gene-products result))))))))

(deftest dense_matrix_test
  (testing "dense matrix parsing")
  (let [result (sut/matrix->entity job-context mtx-job-2)]
    (testing "parsed dense matrix map returns necessary keys for copying the matrix file"
      (is (:pret.matrix/input-file result))
      (is (:pret.matrix/output-file result)))
    (testing "resolves UID"
      (is (= ["matrix-test" "rna-seq|:~rna seq data|:~dense example"]
             (:measurement-matrix/uid result))))
    (testing "generates backing file key"
      (is (:measurement-matrix/backing-file result)))
    (testing "all attributes specified exist in parsed map"
      (is (:measurement-matrix/samples result))
      (is (:measurement-matrix/backing-file result))
      (is (:measurement-matrix/gene-products result))
      (is (:measurement-matrix/name result))
      (testing "and have been resolved to lookup refs when appropriate"
        (is (every? entity-ref? (:measurement-matrix/samples result)))
        (is (every? entity-ref? (:measurement-matrix/gene-products result)))))))


(comment
  (run-tests *ns*))
