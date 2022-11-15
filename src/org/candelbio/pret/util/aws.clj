(ns org.candelbio.pret.util.aws
  (:require [clojure.java.io :as jio]
            [cognitect.aws.client.api :as aws]
            [cognitect.anomalies :as anom]
            [org.candelbio.pret.db.config :as db.config])
  (:import (java.io ByteArrayOutputStream)))

(defn candel-deploy-region []
  (or (keyword (db.config/aws-region))
      :us-east-1))

(defn s3-client [region]
  (aws/client {:api :s3
               :region region}))

(defn get-file
  ([bucket path dest region]
   (let [client (s3-client region)
         aws-resp (aws/invoke client {:op :GetObject
                                       :request {:Bucket bucket
                                                 :Key path}})]
     (if (::anom/category aws-resp)
       (throw (ex-info (str "Error loading " bucket "/" path " from S3")
                       {:msg aws-resp}))
       (do
         (jio/make-parents dest)
         (jio/copy (:Body aws-resp) (jio/as-file dest))))))
  ([bucket path dest]
   (get-file bucket path dest (candel-deploy-region))))


(defn file-bytes [file]
  (with-open [xin (jio/input-stream file)
              xout (ByteArrayOutputStream.)]
    (jio/copy xin xout)
    (.toByteArray xout)))


(defn upload-file
  "Uploads the file to the S3 bucket and key"
  ([src-file bucket key]
   (upload-file src-file bucket key {}))
  ([src-file bucket key s3-opts]
   (let [client (s3-client (candel-deploy-region))
         body (file-bytes src-file)
         s3-put-req (merge {:Bucket bucket
                            :Key key
                            :Body body
                            :ContentType "application/octet-stream"}
                           s3-opts)
         aws-resp (aws/invoke client {:op :PutObject
                                      :request s3-put-req})]
     (if (::anom/category aws-resp)
       (throw (ex-info (str "Error uploading " bucket "/" key " to S3")
                       {:msg aws-resp}))))))

