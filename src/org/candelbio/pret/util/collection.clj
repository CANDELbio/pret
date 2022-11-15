(ns org.candelbio.pret.util.collection
  (:require [org.candelbio.pret.util.text :refer [match-ns]]))


(defn traversable? [node]
  (or (map? node)
      (sequential? node)
      (set? node)))

(defn nested->keywords
  "Given a nested map (vec and maps), returns all keywords in the map wherever they appear.
  NOTE: This finds all keywords, not just keywords that appear as map keys, at the moment."
  [nested-map]
  (filter keyword? (tree-seq
                     traversable?
                     (fn [n]
                       (cond
                         (map? n) (interleave (keys n) (vals n))
                         (sequential? n) (seq n)
                         (set? n) (seq n)))
                     nested-map)))

(defn nested->keyword-keys
  "Given a nested map (vec and maps), returns all keywords in the map that appear as keys."
  [nested-map]
  (filter keyword? (tree-seq
                     traversable?
                     (fn [n]
                       (cond
                         (map? n) (concat (keys n) (filter traversable? (vals n)))
                         (vector? n) (seq n)
                         (set? n) (seq n)))
                     nested-map)))

(defn filter-map
  "Filters a map m via function f and returns a map."
  [f m]
  (into {} (filter f m)))

(defn remove-keys-by-ns
  "Dissoc keys from map m that have namespace ns."
  [m ns]
  (filter-map #(not (match-ns ns (first %))) m))

(defn find-all-nested-values
  "finds all nested values for a given key `k` in map `m`"
  [m k]
  (->> m
       (tree-seq traversable?
                 (fn [node]
                   (if (map? node)
                     (vals node)
                     (seq node))))
       (filter map?)
       (keep k)))

(defn all-nested-maps
  "finds all nested values for a given key `k` in map `m`"
  [m k]
  (->> m
       (tree-seq traversable?
                 (fn [node]
                   (if (map? node)
                     (vals node)
                     (seq node))))
       (filter #(and (map? %) (get % k)))))

(defn csv-data->maps [csv-data]
  "Converts CSV data into a collection of maps with keys corresponding to the column headers"
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            repeat)
       (rest csv-data)))
