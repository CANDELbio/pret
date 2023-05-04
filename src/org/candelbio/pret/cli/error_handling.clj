(ns org.candelbio.pret.cli.error-handling
  (:require [org.candelbio.pret.util.text :as text]
            [clojure.tools.logging :as log])
  (:import (java.util.concurrent ExecutionException)))


#_
(defn exit [code msg]
  (println msg)
  (shutdown-agents)
  (System/exit code))

(defn exit [code msg]
  (println :exiting-not code msg))

(defn report-errors
  "For each key and value in ex-info, report error state."
  [err-map]
  (println "-------- Error occurred during import --------")
  (doseq [[k v] err-map]
    (println "Error in: " (namespace k) \newline
             "Problem: " (name k) (text/->pretty-string v))))

(defn report-and-exit [t]
  "If anticipated error (proxied by ex-info being thrown entity) we report errors in
  a standard way and exit. Otherwise, we terminate with re-throw (stack trace and all)"
  (log/info ::stack-trace (.printStackTrace t))
  (if-let [err-map (or (ex-data t)
                       (ex-data (and (instance? ExecutionException t)
                                     (.getCause t))))]
    (report-errors err-map)
    (println (.getMessage t)))
  (exit 1 "Pret encountered error while executing."))
