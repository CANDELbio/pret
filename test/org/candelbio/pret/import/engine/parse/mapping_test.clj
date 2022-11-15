(ns org.candelbio.pret.import.engine.parse.mapping-test
  (:require [clojure.test :refer :all]
            [org.candelbio.pret.import.engine.parse.mapping :as sut]))

(def mappings-entry
  {:pret/mappings {:enum/yes-no {true ["Y" "y"]
                                 false ["N" "n"]}
                   :enum/recist {:clinical-observation.recist/CR "CR"
                                 :clinical-observation.recist/PR "PR"
                                 :clinical-observation.recist/PD ["PD"]
                                 :clinical-observation.recist/SD ["SD"]}
                   :ref/timepoint {"Ipilimumab/pre-treatment" ["preCTLA4"]
                                   "Ipilimumab/treatment" ["onCTLA4"]
                                   "Nivolumab/pre-treatment" ["postCTLA4_prePD1"]
                                   "Nivolumab/treatment" ["onPD1"]
                                   "Nivolumab/post-treatment" ["postPD1"]}
                   :enum/variant.type {:variant.type/del ["D" "ID"]
                                       :variant.type/ins ["I"]
                                       :variant.type/snp ["SNP"]}}
   :pret/variables {:subject/dead :enum/yes-no
                    :subject/smoker :enum/yes-no
                    :clinical-observation/recist :enum/recist
                    :variant/type :enum/variant.type
                    :sample/timepoint :ref/timepoint}})

(deftest mapping-basic-test
  (let [result (sut/mappings-edn->lookup mappings-entry)]
    (testing "Correct values end up as keys."
      (is (= (keys (:pret/variables mappings-entry))
             (keys result))))
    (testing "Single values end up as keys in sub-map"
      (is (contains? (:clinical-observation/recist result) "CR")))
    (testing "Collection values all are keys in sub-map"
      (is (every? #(contains? (:variant/type result) %)
                  ["D" "ID" "SNP"])))))


(comment
  (run-tests *ns*)
  (sut/mappings-edn->lookup mappings-entry))
