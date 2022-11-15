(ns org.candelbio.pret.import.engine.parse.data-test
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [clojure.string :as str]
            [org.candelbio.pret.test-util :as test-util]
            [org.candelbio.pret.import.engine.parse.data :as sut]
            [org.candelbio.pret.db.schema :as db.schema]
            [org.candelbio.pret.import.engine.parse.config :as parse.config]
            [org.candelbio.pret.import.engine.parse.mapping :as parse.mapping]
            [org.candelbio.pret.util.io :as util.io]))

(def import-root-dir "test/resources/reference-import/")
(def ref-config-file (str import-root-dir "config.edn"))
(def mapping-file (str import-root-dir "mappings.edn"))
(def mappings (util.io/read-edn-file mapping-file))
(def config (util.io/read-edn-file ref-config-file))
(def schema (db.schema/get-metamodel-and-schema))

(defn parsed-config []
  (parse.config/parse-config-map schema config))

(defn mapping-lookup []
  (parse.mapping/mappings-edn->lookup mappings))

(defn full-import-ctx []
  {:parsed-cfg      (parsed-config)
   :schema          schema
   :mapping         (mapping-lookup)})

(defn directives []
  (parse.config/cfg-map->directives schema mappings import-root-dir (parsed-config)))

;; these two examples cover most (all?) non-trivial uid resolution
(def bendall-meas-fname "cytof_measurements_Bendall.txt")
(def bendall-measurements
  (test-util/data-file-head
    (str import-root-dir "processed/" bendall-meas-fname)
    10))
(defn bendall-meas-job []
  (first (filter #(str/ends-with? (:pret/input-file %) bendall-meas-fname)
                 (directives))))

(def bendall-cellpop-fname "cell_populations_Bendall.txt")
(def bendall-cellpops
  (test-util/data-file-head
    (str import-root-dir "processed/" bendall-cellpop-fname)
    10))

(defn bendall-cellpop-job []
  (first (filter #(str/ends-with? (:pret/input-file %) bendall-cellpop-fname)
                 (directives))))


(def import-context {:filename "blah.txt"
                     :line-number 3})

(defn extract-na-val-set [job]
  (let [raw-na (:pret/na job)]
    (if (coll? raw-na)
      (set raw-na)
      #{raw-na})))

(deftest card-many-resolution-test
  (let [result (sut/extract-card-many-data (parsed-config) schema (mapping-lookup)
                                           (get-in config [:cnv])
                                           {"Genes" "blah1;blah2;blah3"}
                                           #{"NA"})]
    (testing "Multiple values are separated by delimiter into separate assertions."
      (is 3 (count (:genes result))))))

(deftest create-self-uid-test
  (let [bendall-pop-uid
        (sut/create-self-uid
          (parsed-config)
          schema
          (bendall-cellpop-job)
          (zipmap
            (:header bendall-cellpops)
            (first (:data bendall-cellpops))))]
    (is (= {:cell-population/uid ["pici0002"
                                  (str "CyTOF" sut/context-id-delim
                                       "Bendall" sut/context-id-delim
                                       "NK cells")]}
           bendall-pop-uid))))

(deftest record->entity-test
  (let [cytof-meas (map (fn [raw-record]
                          (sut/record->entity
                            (full-import-ctx)
                            (bendall-meas-job)
                            (:header bendall-measurements)
                            raw-record
                            (extract-na-val-set (bendall-meas-job))
                            import-context))
                        (:data bendall-measurements))
        cell-pop (map (fn [raw-record]
                        (sut/record->entity
                          (full-import-ctx)
                          (bendall-cellpop-job)
                          (:header bendall-cellpops)
                          raw-record
                          (extract-na-val-set (bendall-cellpop-job))
                          import-context))
                      (:data bendall-cellpops))]
    ;; in initial state some of these example tests are brittle and/or coupled to metamodel, but
    ;; this gives us a fairly easy fast failure for now, until there's time or regression
    ;; fix opportunities to rewrite with more general predicates.
    (testing "Non-trivial UID resolution by context."
      (let [ex-record (first cytof-meas)
            ex-record2 (second cytof-meas)
            exp-uid (get-in ex-record [:measurement-set/_measurements 1])
            sample-uid (get-in ex-record [:measurement/sample :sample/uid])
            cp-uid (get-in ex-record2 [:measurement/cell-population :cell-population/uid])]
        (is (= ["pici0002" (str "CyTOF" sut/context-id-delim "Bendall")]
               exp-uid))
        (is (= ["pici0002" "PICI0002_A03_K00916CP01_SPB_A01"] sample-uid))
        (is (= [ "pici0002" (str "CyTOF" sut/context-id-delim
                                 "Bendall" sut/context-id-delim
                                 "NK cells")]
               cp-uid))))
    (testing "Per-record self UID resolution in context."
      (let [cell-pop-ex (first cell-pop)
            cell-pop-ex2 (second cell-pop)
            cell-pop-uid (:cell-population/uid cell-pop-ex)
            cp-gene-ex (:cell-population/positive-markers cell-pop-ex2)]
        (is (= ["pici0002" (str "CyTOF" sut/context-id-delim
                                "Bendall" sut/context-id-delim
                                "NK cells")]
               cell-pop-uid))
        (testing "Card many refs wrapped at process record level."
          (is (= [[:epitope/id "KI67"]] cp-gene-ex)))))))

(comment
  (run-tests *ns*)
  (require '[clojure.pprint :refer [pprint]])
  (let [{:keys [header data]} bendall-cellpops
        processed (map #(sut/record->entity
                          (full-import-ctx)
                          (bendall-cellpop-job)
                          header
                          %
                          import-context)
                       data)]
    (pprint (nth processed 1))))
