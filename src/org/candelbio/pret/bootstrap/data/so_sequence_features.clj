(ns org.candelbio.pret.bootstrap.data.so-sequence-features
  "This namespace contains utilites for extracting reference data from the
  OBO Foundry Sequence Ontology. The data referred to herein is licensed
  under CC v4.0 which allows commercial use. See:

  https://obofoundry.org/ontology/so.html

  For more information."
  (:require [org.candelbio.pret.bootstrap.obo :as obo]
            [clojure.java.io :as io]
            [org.candelbio.pret.util.io :as util.io]))

;; Thi

(defn term->entity
  [x]
  {:so-sequence-feature/name (:name x)
   :so-sequence-feature/id (:id x)})

(defn init
  "Returns transaction data for sequence-ontology entities for db initialization. opts is a map
  with the following keys
    :obo-file-file    The path to the Sequence Ontology definition file in obo format"
  [obo-file]
  (with-open [rdr (io/reader obo-file)]
    (let [lines (line-seq rdr)
          xf (comp
               (remove #(= (count %) 1))
               (map obo/term->map)
               (map term->entity)
               (map #(vector (:so-sequence-feature/name %) %)))] ; This is necessary to remove duplicates

      (->> lines
           obo/terms
           (into {} xf)
           (vals)))))

(defn generate-tx-data
  [{:keys [obo-file output-file]}]
  (let [all-sequence-ontology-data (init obo-file)]
    (util.io/write-tx-data all-sequence-ontology-data output-file)))

(comment
  (generate-tx-data
    {:obo-file "seed_data/raw/so-sequence-features/so.obo"
     :output-file "seed_data/edn/all-so-sequence-features-tx-data.edn"}))
