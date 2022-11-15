(ns org.candelbio.pret.db.indexes)

(defn- by-key
  [schema k]
  (into {} (keep (fn [ent]
                   (when-let [lookup-by (get ent k)]
                     [lookup-by ent]))
                 schema)))

(defn by-ident [schema]
  (by-key schema :db/ident))

(defn by-kind-name [schema]
  (by-key schema :kind/name))

(defn by-kind-attr [schema]
  (->> (by-key schema :kind/attr)
       (map #(vec [(:db/ident (first %)) (second %)]))
       (into {})))

(defn by-uid [schema]
  (->> (by-key schema :kind/need-uid)
       (map #(vec [(:db/ident (first %)) (second %)]))
       (into {})))

(defn refs-by-ident [schema]
  (->> (by-ident schema)
       (filter (fn [[ident ent]]
                 (= :db.type/ref
                    (get-in ent [:db/valueType :db/ident]))))
       (into {})))

(defn card-many-by-ident [schema]
  (->> (by-ident schema)
       (filter (fn [[ident ent]]
                 (= :db.cardinality/many
                    (get-in ent [:db/cardinality :db/ident]))))
       (into {})))

(defn all [schema]
  {:index/idents (by-ident schema)
   :index/kinds (by-kind-name schema)
   :index/kind-attrs (by-kind-attr schema)
   :index/uids (by-uid schema)
   :index/refs (refs-by-ident schema)
   :index/card-many (card-many-by-ident schema)})

(comment
  (require '[org.candelbio.pret.db.schema :as db.schema])
  (def schema (db.schema/get-metamodel-and-schema))
  (card-many-by-ident schema))
