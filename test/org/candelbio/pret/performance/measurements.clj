(ns org.candelbio.pret.performance.measurements
  (:require [org.candelbio.pret.import :as import]
            [clojure.test :refer :all]
            [clojure.java.shell :refer [sh]]
            [org.candelbio.pret.import :as import]))


(def tmp-dir "tmp-perf-output")

(def import-cfg-file
  "test/resources/perf-benchmark/x50-only/config.edn")

(defn run []
  (import/prepare-import {:target-dir tmp-dir
                          :import-cfg-file import-cfg-file
                          :disable-remote-calls true}))

(defn -main [& args]
  (let [_results (run)]
    (println "done")))

