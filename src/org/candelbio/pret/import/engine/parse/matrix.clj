(ns org.candelbio.pret.import.engine.parse.matrix
  (:require [org.candelbio.pret.import.engine.parse.data :as parse.data]
            [org.candelbio.pret.db.metamodel :as metamodel]
            [org.candelbio.pret.util.collection :as util.coll]
            [org.candelbio.pret.util.uuid :as util.uuid]
            [org.candelbio.pret.import.engine.parse.matrix-file :as parse.matrix-file]
            [org.candelbio.pret.import.engine.parse.config :as parse.config]
            [org.candelbio.pret.util.io :as util.io]
            [org.candelbio.pret.import.engine.parse.mapping :as parse.mapping]))


(defn matrix->entity
  "Given a directive for parsing a matrix file, parses the file and returns an entity
  map representation of the matrix blob corresponding to the given matrix spec + file
  content combination. The entity representation contains reference data for all
  measurement references and targets, but does not contain the numeric matrix
  representation.

  Along with the entity map representation, matrix->entity also returns :pret.matrix/
  namespaced keyword used by downstream processing (matrix file prepare and upload)."
  [{:keys [parsed-cfg schema mapping] :as job-context}
   {:keys [pret.matrix/input-file
           pret.matrix/format
           pret.matrix/column-attribute ;; only dense, otherwise all in indexed-by
           pret.matrix/indexed-by
           pret/node-kind] :as mtx-directive}]
  (let [data-type-key (metamodel/matrix-data-type-attr schema node-kind)
        value-type (get mtx-directive data-type-key)
        db-value-type (metamodel/attr->db-type schema value-type)
        data-type-kv {data-type-key value-type}
        _ (when-not (and input-file format indexed-by value-type)
            (throw (ex-info (str "Every matrix directive requires at minimum :pret.matrix prefixed:"
                                 "input-file, format, indexed-by, and a measurement/data type specification.")
                            {::matrix-directive mtx-directive})))
        resolved-constants (parse.data/extract-constant-data job-context mtx-directive #{})
        parsed-mtx-file (parse.matrix-file/parse-matrix-file
                          input-file
                          ;; default to sparse
                          (merge {:indexed-by (util.coll/remove-keys-by-ns indexed-by "pret")
                                  :data-spec  value-type
                                  :data-type db-value-type
                                  :sparse?    true}
                                 ;; when not sparse, add column-attribute/target
                                 ;; for dense matrix column interpretation
                                 (when (not= :pret.matrix.format/sparse format)
                                   {:sparse? false
                                    :target column-attribute})))
        no-pret-parsed-mtx-file (util.coll/remove-keys-by-ns parsed-mtx-file "pret")
        raw-entity (util.coll/remove-keys-by-ns mtx-directive "pret")
        resolve-fn (fn [attr val]
                     (parse.data/resolve-value parsed-cfg schema mapping mtx-directive attr val))
        resolved-entity (into {} (for [[attr v] raw-entity]
                                   [attr (if (coll? v)
                                           (map (partial resolve-fn attr) v)
                                           (resolve-fn attr v))]))
        resolved-refs (into {} (for [[ind vals] no-pret-parsed-mtx-file]
                                 [ind (map (partial resolve-fn ind) vals)]))
        key-pt1 (util.uuid/random)
        key (str key-pt1 ".tsv")
        key-attr (metamodel/matrix-key-attr schema node-kind)
        precomputed (:pret/precomputed mtx-directive)
        _ (when-not key-attr
            (throw (ex-info (str "Could not resolve backing file attribute for: " input-file)
                            {::job mtx-directive})))
        backing-key {key-attr key}
        entity-map (merge resolved-entity
                          precomputed
                          no-pret-parsed-mtx-file
                          resolved-refs
                          backing-key
                          data-type-kv
                          resolved-constants
                          {:pret.matrix/input-file input-file
                           :pret.matrix/output-file key
                           :pret.matrix/header-substitutions
                           (-> (util.coll/remove-keys-by-ns indexed-by "pret")
                               (merge (when (= :pret.matrix.format/sparse format)
                                        {(:pret.matrix/sparse-value-column parsed-mtx-file)
                                         value-type})))})
        ;; self-uid comes in last, potentially dependent on prior entity parsing stuff.
        self-uid (parse.data/create-self-uid parsed-cfg schema mtx-directive entity-map)
        validation-errors (seq (:validation-errors entity-map))]
      (if validation-errors
        (throw (ex-info "Measurements in matrix file did not pass spec!"
                        {::spec value-type
                         ::validation-errors (take 1000 validation-errors)}))
        (-> entity-map
            (merge self-uid)
            (merge {:pret.matrix/constant-columns
                    (util.coll/remove-keys-by-ns (:pret.matrix/constants mtx-directive) "pret")})
            (dissoc :validation-errors)))))
