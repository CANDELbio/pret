(ns org.candelbio.pret.bootstrap.data.cell-types
  "This namespace provides utilities for mapping OBO Cell Ontology definitions
  into CANDEL schema. OBO Foundry data is CC v4.0 and allows for commercial use.
  For more information:

  https://obofoundry.org/ontology/cl.html

  Files can also be downloaded from the above site."
  (:require [org.candelbio.pret.bootstrap.obo :as obo]
            [clojure.java.io :as io]
            [org.candelbio.pret.util.io :as util.io]))


(defn- term->entity
  [x]
  (let [ret {:cell-type/co-id (:id x)
             :cell-type/co-name (:name x)}]
    (if-let [def-line (:def x)]
      (assoc ret :cell-type/description def-line)
      ret)))

(defn init
  "Returns transaction data for cell-type entities for db initialization. opts is a map
  with the following keys
    :obo-file-fname    The path to the Cell Ontology definition file in obo format"
  [obo-file]
  (with-open [rdr (io/reader obo-file)]
    (let [lines (line-seq rdr)
          xf (comp
               (remove #(= (count %) 1))
               (map obo/term->map)
               (filter #(= (subs (:id %) 0 3) "CL:"))
               (map term->entity))]
      (->> lines
           obo/terms
           (into [] xf)))))

(defn generate-tx-data
  [{:keys [obo-file output-file]}]
  (let [all-cell-type-data (init obo-file)]
    (util.io/write-tx-data all-cell-type-data output-file)))


(comment
  (generate-tx-data
    {:obo-file "seed_data/raw/cell-types/cl.obo"
     :output-file "seed_data/edn/all-cell-type-tx-data.edn"}))
