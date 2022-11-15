(ns org.candelbio.pret.import.engine-test
  (:require [clojure.test :refer :all]
            [org.candelbio.pret.import.engine :as sut]))


(def example-job-1
  {:pret/input-file "processed/variants.txt"
   :id              "var.id"
   :genomic-coordinates     "gc.id"
   :gene            "hugo"
   :ref-allele      "ref"
   :alt-allele      "alt"})

(def example-job-2
  {:pret/input-file ["processed/" "cnv_ref_*.tsv"]
   :pret/na ""
   :genomic-coordinates "gc.id"
   :id  "gc.id"
   :genes       {:pret/many-delimiter ";"
                 :pret/many-variable "Genes"}})

(def example-job-3
  {:pret/input-file "processed/coordinates.txt"
   :pret/na ""
   :genes ["gene" "value"]
   :name "signature"})


(deftest get-req-column-names-test
  (testing "Jobs report correct columns as required names."
    (is (= #{"var.id" "gc.id" "hugo" "ref" "alt"}
           (set (sut/get-req-column-names example-job-1))))
    (is (= #{"gc.id" "Genes"}
           (set (sut/get-req-column-names example-job-2))))
    (is (= #{"gene" "value" "signature"}
           (set (sut/get-req-column-names example-job-3))))))


(comment
  (run-tests *ns*))