(ns org.candelbio.pret.db.config)


(defn wrap-config
  [env property default]
  (fn []
    (or (System/getenv env)
        (System/getProperty property)
        default)))


(def aws-region
  (wrap-config
    "CANDEL_AWS_REGION"
    "candel.awsRegion"
    "us-east-1"))

(def ddb-table
  (wrap-config
    "CANDEL_DDB_TABLE"
    "candel.ddbTable"
    "candel-prod"))

(def reference-data-bucket
  (wrap-config
    "CANDEL_REFERENCE_DATA_BUCKET"
    "candel.referenceDataBucket"
    "pici-pret-processed-reference-data-prod-right")) ;mt: this is where it actually is

(def matrix-bucket
  (wrap-config
    "CANDEL_MATRIX_BUCKET"
    "candel.matrixBucket"
    "candel-matrix"))

(def matrix-dir
  (wrap-config
    "CANDEL_MATRIX_DIR"
    "candel.matrixDir"
    "matrix-store"))

(def matrix-backend
  (wrap-config
    "CANDEL_MATRIX_BACKEND"
    "candel.matrixBackend"
    "s3"))

;; don't default base-uri, only used to override, nil puna/when check for
;; conrol flow when not present
(defn base-uri []
  (System/getenv "CANDEL_BASE_URI"))
