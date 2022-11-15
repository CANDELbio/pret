(ns org.candelbio.pret.test-util
  (:require [clojure.edn :as edn]
            [clojure.data.csv :as data.csv]
            [clojure.java.io :as io]))

(defmacro thrown->ex-data
  [body]
  `(try
     ~body
     (catch Exception e#
       (ex-data e#))))

(defn ensure-filepath!
  "Ensures file can be written to."
  [filepath]
  (io/make-parents filepath)
  filepath)

(defn file->edn-seq
  "test util eagerly reads multiple forms from an edn file with no containing form"
  [f]
  (with-open [rdr (io/reader f)]
    (into [] (map edn/read-string (line-seq rdr)))))

(defn data-file-head [f n]
  (with-open [rdr (io/reader f)]
    (let [csv-stream (data.csv/read-csv rdr :separator \tab)]
      {:header (first csv-stream)
       :data (into [] (take n (rest csv-stream)))})))