(ns org.candelbio.pret.util.release
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn version
  "Reads info.edn to retrieve version info."
  []
  (let [info-edn (io/resource "info.edn")]
    (when-not info-edn
      (throw
        (ex-info (str "info.edn file does not exist, you should only encounter this error as a dev in pret repo."
                       "\nRegenerate with echo command below or see Makefile for reference:"
                      "\n----------------------\n"
                      "> echo \"{:pret/version \\\"$(util/version)\\\"}\" > resources/info.edn"
                      "\n----------------------\n"
                      "If you see this error as an end user, report to candel dev team.")
                 {:cli/missing-file "resources/info.edn"})))
    (-> info-edn
        (slurp)
        (edn/read-string)
        (:pret/version))))

(comment
  (version))
