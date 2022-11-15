(ns org.candelbio.pret.import.tx-data-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [org.candelbio.pret.util.io :as util.io]
            [org.candelbio.pret.import.tx-data :as sut]
            [org.candelbio.pret.test-util :as util]
            [org.candelbio.pret.util.uuid :as uuid]))

(def flat-measurement-entity-maps-path "test/resources/flat_measurement_entity_data.edn")

(deftest process-one-file-test
  (let [test-output-dir (str "tmp-test-" (uuid/random))
        import-config "process-one-file-test"
        out-filepath (->> "measurement-txes.edn"
                          (str test-output-dir "/"))]
    (try
      (testing "Produces a file containing valid tx data"
        (sut/process-one-file!
          import-config
          flat-measurement-entity-maps-path
          (util/ensure-filepath! out-filepath)
          3)
        (let [results (util/file->edn-seq out-filepath)
              metadata (map first results)]
          (is (every? (partial s/valid? ::sut/valid-tx-data) results))
          (testing "And contains valid metadata as the first tx item per batch."
            (is (every? (partial s/valid? ::sut/metadata) metadata)))))
      (finally
        (util.io/delete-recursively test-output-dir)))))

(comment
  (run-tests *ns*))
