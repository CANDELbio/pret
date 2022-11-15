(ns dataset.tools
  (:require [org.candelbio.pret.util.io :as util.io]
            [clojure.string :as str]
            [org.candelbio.pret.util.collection :as util.coll]
            [clojure.java.io :as io]
            [org.candelbio.pret.util.text :as text]
            [org.candelbio.pret.import.engine :as engine]
            [clojure.edn :as edn]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]))

;; these are dev time only tools for a mix of edn and file system munging
;; filename other than root, currently broken

(defn with-dir [dir fname]
  (str dir "/" fname))

(defn copy-n-lines [src-path dest-path n]
  ;; inc n here to not count header against total lines
  (let [result (sh "bash" "-c" (str/join " "
                                         ["head"
                                          "-n"
                                          (inc n)
                                          src-path
                                          ">"
                                          dest-path]))]))

(defn copy-file [src-fpath dest-fpath]
  (sh "cp" src-fpath dest-fpath))

(defn all-import-files [src-import-cfg-file]
  (let [cfg-edn (util.io/read-edn-file src-import-cfg-file)
        cfg-path (text/folder-of src-import-cfg-file)
        mapping-file (->> (get-in cfg-edn [:pret/import :mappings])
                          (text/filename))]
    {:src-dir        cfg-path
     :relative-files (reduce conj [(text/filename src-import-cfg-file)
                                   mapping-file]
                             (util.coll/find-all-nested-values cfg-edn :pret/input-file))}))

(defn subset-file-import
  "For a particular file import, copies a subset of data from that import into
  dest-dir."
  [src-import-cfg-file dest-dir record-count]
  (let [{:keys [src-dir relative-files]} (all-import-files src-import-cfg-file)
        ;; keep import cfg and mappings fully intact
        [to-keep to-subset] (split-at 2 relative-files)]
    ;; just straight copy of files to keep
    (doseq [rel-f to-keep]
      (let [src-f (with-dir src-dir rel-f)
            target-f (with-dir dest-dir rel-f)]
        ;; ugly but harmless to call make-parents multiple times
        (io/make-parents target-f)
        (copy-file src-f target-f)))
    (doseq [rel-f to-subset]
      (let [src-f (with-dir src-dir rel-f)
            dest-fpath (with-dir dest-dir rel-f)]
        ;; make-parents needed here because can be any arbitrary relative path
        ;; from import root
        (io/make-parents dest-fpath)
        (copy-n-lines src-f dest-fpath record-count)))))

(comment
  (def config-file "/Users/Marshall/Documents/Dev/PICI/pret-datasets/roh2017/config.edn")
  (subset-file-import config-file "/Users/Marshall/Documents/Dev/PICI/pret/test/resources/reference-import" 1000))
