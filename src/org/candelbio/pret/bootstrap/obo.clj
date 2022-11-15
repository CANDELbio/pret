(ns org.candelbio.pret.bootstrap.obo
  (:require [clojure.string :as string]))


(defn- process-string
  [s]
  (-> s
      (string/replace #"\"" "")
      string/trim))


(defn- skip-line?
  [s]
  (let [v (string/split s #":")]
    (some #{"format-version" "data-version" "subsetdef" "date" "saved-by"
            "auto-generated-by" "default-namespace"
            "synonymtypedef" "remark" "ontology" "property_value"} [(first v)])))



(defn term->map
  [s]
  (let [xf (comp
             (remove #(= % ""))
             (map #(let [v (string/split % #":")
                         k (keyword (first v))
                         value (if (= (count v) 2)
                                 (second v)
                                 (string/join ":" (rest v)))]
                     [k (process-string value)])))]
    (into {} xf s)))


(defn terms
  [lines]
  (lazy-seq
    (loop [v []
           ls lines]
      (if (seq ls)
        (if (not (skip-line? (first ls)))
          (if (re-find #"^\[" (first ls))
            (cons v (terms (rest ls)))
            (recur (conj v (first ls)) (rest ls)))
          (recur v (rest ls)))
        [v]))))
