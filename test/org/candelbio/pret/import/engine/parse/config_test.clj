(ns org.candelbio.pret.import.engine.parse.config-test
  (:require [clojure.test :refer :all]
            [org.candelbio.pret.test-util :as tu]
            [contextual.core :as c]
            [org.candelbio.pret.db.schema :as db.schema]
            [org.candelbio.pret.util.collection :as coll]
            [org.candelbio.pret.util.io :as util.io]
            [org.candelbio.pret.import.engine.parse.mapping :as parse.mapping]
            [org.candelbio.pret.import.engine.parse.config :as parse.config]
            [org.candelbio.pret.db.schema :as schema]))

(def good-config (util.io/read-edn-file "test/resources/reference-import/config.edn"))
(def wrong-attr-config (util.io/read-edn-file "test/resources/parse-config-examples/attribute-typos.edn"))
(def wrong-config-keys (util.io/read-edn-file "test/resources/parse-config-examples/wrong-config-keys.edn"))
(def schema (db.schema/get-metamodel-and-schema))


(def import-root-dir "test/resources/reference-import/")
(def ref-config-file (str import-root-dir "config.edn"))
(def mapping-file (str import-root-dir "mappings.edn"))
(def mappings (util.io/read-edn-file mapping-file))
(def config (util.io/read-edn-file ref-config-file))
(defn mapping-lookup []
  (parse.mapping/mappings-edn->lookup mappings))
(defn parsed-config []
  (parse.config/parse-config-map schema config))
(defn job-args-map []
  {:parsed-cfg      (parsed-config)
   :schema          schema
   :mapping         (mapping-lookup)})

(deftest job-extraction-test
  (let [cfg (parsed-config)
        directives (parse.config/cfg-map->directives schema (mapping-lookup) import-root-dir cfg)
        dataset-file-count (coll/find-all-nested-values (:dataset config) :pret/input-file)
        ref-data-only (dissoc config :dataset :pret/import)
        ref-files (coll/find-all-nested-values (concat (vals ref-data-only)) :pret/input-file)
        ref-jobs (parse.config/reference-data-jobs schema ref-data-only (job-args-map))]
    (testing "Directives gets all maps that have a :pret/input-file entry under the :dataset key."
      (is (= (count directives)
             (count dataset-file-count))))
    (testing "Directives maps for ref data."
      (is (= (count ref-files)
             (count ref-jobs))))))

(deftest namespace-config-test
  ;; this pathway is non-obvious and may be subject to cleanup:
  ;; - config file is one map wrapped in a vector, so first unwraps it
  ;; - we need full context of config as per contextualize
  ;; - but for all follow on logic we only operate from :dataset node on
  ;;   (but it _requires_ context from the parent for contextualize to know it
  ;;    is the :dataset node)
  ;; - Note, that's not specific to dataset, but any key we want to namesapce
  ;;   in the map.
  (let [cfg good-config
        ctx-cfg (c/contextualize cfg)
        cfg-dataset-root-ctx (:dataset ctx-cfg)
        ns-cfg (parse.config/namespace-config schema cfg-dataset-root-ctx)]
    (testing "All keys have now been namespaced."
      (is (every? namespace (coll/nested->keyword-keys ns-cfg))))))

(deftest pruning-tests
  (let [pruned (parse.config/remove-directives good-config)]
    (testing "All :pret/input-file keywords pruned from nested structure."
      (is (not-any? #(= :pret/input-file % ) (coll/nested->keywords pruned))))))

(defn raw->ensure-ns
  "From raw config edn. Notice, this duplicates a section only of engine chain. Repetition preferred
  at moment to refactor to couple, but if test fails check that it's in line with first steps in
  engine/cfg-map->literal-data"
  [schema cfg-edn]
  (->> cfg-edn
       (parse.config/ensure-raw schema)
       (c/contextualize)
       (:dataset)
       (parse.config/namespace-config schema)
       (parse.config/remove-directives)
       (parse.config/ensure-ns schema)))

(deftest ensure-tests
  (testing "Good config passes fine (no errors thrown or nil returned)."
    (is (raw->ensure-ns schema good-config)))
  (testing "Bad pret keys are rejected, with invalid ones listed."
    (is (= [:pret/reference-data]
           (:config-file/invalid-pret-keys (tu/thrown->ex-data (parse.config/ensure-raw schema wrong-config-keys))))))
  (testing "Omitted top level keys are rejected, with missing ones enumerated."
    (let [missing-dataset (dissoc good-config :dataset)]
      (is (= [:dataset]
             (:config-file/missing-top-keys (tu/thrown->ex-data (parse.config/ensure-raw schema missing-dataset)))))))
  (testing "Wrong attribute names are rejected, wrong ones are enumerated."
    (is (= [:assay/desc :assay/id]
           (:config-file/invalid-attributes (tu/thrown->ex-data (raw->ensure-ns schema wrong-attr-config)))))))

;; tests for a config that contains matrices
(def matrix-root-dir "test/resources/matrix/")
(def matrix-config (util.io/read-edn-file (str matrix-root-dir "config.edn")))
(def matrix-mapping (util.io/read-edn-file (str matrix-root-dir "mappings.edn")))

(deftest matrix-directives-tests
  (testing "Matrix config parsing sanity checks:"
    (let [pret-schema (schema/get-metamodel-and-schema)
          parsed-cfg-map (parse.config/parse-config-map pret-schema matrix-config)
          dataset-entity (parse.config/cfg-map->dataset-entity pret-schema matrix-mapping parsed-cfg-map)
          import-entity (parse.config/cfg-map->import-entity matrix-config)
          record-directives (parse.config/cfg-map->directives
                              pret-schema
                              matrix-mapping
                              matrix-root-dir
                              parsed-cfg-map)
          matrix-directives (parse.config/cfg-map->matrix-directives
                              pret-schema
                              matrix-mapping
                              matrix-root-dir
                              parsed-cfg-map)]
      (testing "Correct number of matrix directives extracted."
        (is (= 2 (count matrix-directives))))
      (testing "Correct number of normal pret directives extracted"
        (is (= 3 (count record-directives))))
      (testing "Every matrix directive has a node-kind"
        (is (every? :pret/node-kind matrix-directives)))
      (testing "Other entity extraction from config works"
        (is (some? dataset-entity))
        (is (some? import-entity))))))


(comment
  (run-tests *ns*)
  (:cnv (parse.config/pr))
  (parse.config/parse-config-map schema good-config)

  (tu/thrown->ex-data (parse.config/ensure-raw schema wrong-config-keys)))
