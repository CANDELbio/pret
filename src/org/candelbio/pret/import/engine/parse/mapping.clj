(ns org.candelbio.pret.import.engine.parse.mapping
  (:require [org.candelbio.pret.util.io :as util.io]
            [org.candelbio.pret.db.metamodel :as metamodel]
            [org.candelbio.pret.db.schema :as schema]))

(defn reverse-unroll
  "For a map of mixed structure {key [item1 item2] key item3} returns
  {item1 key, item2 key, item3 key}."
  [r-map]
  (->> (seq r-map)
       (mapcat (fn [[k entry]]
                 (if (coll? entry)
                   (for [elem entry]
                     [elem k])
                   [[entry k]])))
       (into {})))

(defn validate-enums!
  [mappings-edn]
  (let [schema (schema/get-metamodel-and-schema)
        mapping-keys (-> mappings-edn :pret/mappings keys)
        errors (->> (for [key mapping-keys]
                      (when-let [mapping-target-keys
                                 (->> (get-in mappings-edn [:pret/mappings key])
                                      (keys)
                                      (filter keyword?))]
                        (keep (fn [k]
                                (when-not (metamodel/enum-ident? schema k)
                                  k))
                              mapping-target-keys)))
                    (keep not-empty)
                    (apply concat)
                    (vec))]
    (when (seq errors)
      (throw (ex-info (str "Enums: " errors " in mapping file not in the CANDEL schema.")
               {:mapping-file/enums errors})))))

(defn mappings-edn->lookup
  "Given EDN as read from the mappings file, returns a lookup map for which a value can
  be used to retrieve its mapping/substitution."
  [mappings-edn]
  (validate-enums! mappings-edn)
  (->> (for [[attr enum-name] (:pret/variables mappings-edn)]
         [attr (reverse-unroll (get (:pret/mappings mappings-edn) enum-name))])
       (into {})))

(comment
  (require '[org.candelbio.pret.util.io :as util.io])

  (def mfile
    "/Users/bkamphaus/azure-datasets/abida2019/mappings.edn")

  (def ex-mapping
    (util.io/read-edn-file mfile))
  (-> ex-mapping mfile)
  (mappings-edn->lookup ex-mapping))
