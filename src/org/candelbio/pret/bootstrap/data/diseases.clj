(ns org.candelbio.pret.bootstrap.data.diseases
  "This namespace contains utilities for processing MedDRA disease ontology
  information into CANDEL. The data referred to is proprietary and cannot be
  redistributed."
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [org.candelbio.pret.util.io :as util.io]))

(defn- parse-preferred-term-line
  [s]
  (let [v (string/split s #"\$")]
    [(v 0) {:meddra-disease/preferred-name (v 1)
            :meddra-disease/synonyms []}]))

(defn- load-preferred-terms
  [file]
  (with-open [reader (io/reader file)]
    (let [lines (line-seq reader)]
      (into {} (map parse-preferred-term-line lines)))))

(defn- add-ll-terms
  [file x]
  (with-open [reader (io/reader file)]
    (loop [m x
           lines (line-seq reader)]
      (if (seq lines)
        (let [[_ synonym code] (string/split (first lines) #"\$")]
          (recur
            (update-in m [code :meddra-disease/synonyms] #(conj % synonym))
            (rest lines)))
        m))))

(defn init
  "Returns transaction data for disease entities for db initialization. opts is a map
  with the following keys
    :preferred-terms-file  The path to the MedDRA file containing the preferred terms table
                            (pt.asc)
    :ll-terms-file         The path to the MedDRA file containing the lower level terms table
                            (llt.asc)"
  [preferred-terms-file ll-terms-file]
  (->> (load-preferred-terms preferred-terms-file)
       (add-ll-terms ll-terms-file)
       (vals)))

(defn generate-tx-data
  [{:keys [preferred-terms-file ll-terms-file output-file]}]
  (let [all-disease-data (init preferred-terms-file ll-terms-file)]
    (util.io/write-tx-data all-disease-data output-file)))

(comment
  (generate-tx-data
    {:preferred-terms-file "seed_data/raw/diseases/pt.asc"
     :ll-terms-file "seed_data/raw/diseases/llt.asc"
     :output-file "seed_data/edn/all-disease-tx-data.edn"}))
