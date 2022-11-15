(ns org.candelbio.pret.db.matrix
  (:require [org.candelbio.pret.import.file-conventions :as file-conventions]
            [org.candelbio.pret.db.config :as db.config]
            [clojure.tools.logging :as log]
            [org.candelbio.pret.util.aws :as aws]
            [clojure.java.io :as io]))


(defn upload-matrix-files->s3!
  [working-dir]
  (let [matrix-files (file-conventions/matrix-filenames working-dir)]
    (doseq [matrix-key matrix-files]
      (let [src-path (file-conventions/in-matrix-dir working-dir matrix-key)]
        (log/info "Uploading matrix file: " matrix-key)
        (aws/upload-file src-path
                         (db.config/matrix-bucket) matrix-key
                         ;; this content type must be put on s3 object or
                         ;; pre-signed url will be a pain to deal with
                         ;; from httr, possibly other clients.
                         {:ContentType "text/tab-separated-values"})))
    true))

(defn noop [_working-dir] true)

(defn copy-matrix-files!
  [working-dir]
  (let [matrix-write-dir (db.config/matrix-dir)
        _ (io/make-parents (str matrix-write-dir "/---"))
        matrix-files (file-conventions/matrix-filenames working-dir)]
    (doseq [matrix-file matrix-files]
      (let [src-path (file-conventions/in-matrix-dir working-dir matrix-file)
            dest-path (str matrix-write-dir matrix-file)]
        (io/copy src-path dest-path)))
    true))

(defn get-uploader [candel-matrix-backend]
  (case candel-matrix-backend
    "s3" upload-matrix-files->s3!
    "file" copy-matrix-files!
    "noop" noop
    :default (throw (ex-info "Not a valid matrix backend!"
                             {:matrix-backend/provided-value candel-matrix-backend
                              :matrix-backend/allowed-values #{"s3" "file" "noop"}}))))

(defn upload-matrix-files!
  [working-dir]
  (let [backend (db.config/matrix-backend)
        backend-upload-fn (get-uploader backend)]
    (backend-upload-fn working-dir)))
