(ns org.candelbio.pret.validation.record
  "For any given record, functionality, we can ensure correspondence with spec for
  an indivual attribute/value pair, if a spec is defined."
  (:require [clojure.spec-alpha2 :as s]
            ;; don't invoke directly, but this is a load-order dependency, e.g. for repl workflows.
            ;; i.e., if specs namespace not loaded, these specs won't exist.
            [cognitect.anomalies :as anom]))

(defn validate [ent-map]
  (let [ent (dissoc ent-map :pret/annotations)
        issues (seq (remove nil? (for [[a-kw val] ent]
                                   (when-let [spec (s/get-spec a-kw)]
                                     (when-not (s/valid? a-kw val)
                                       {a-kw {:failing-value val
                                              :location (:pret/annotations ent-map)
                                              :spec-failure (s/explain-str a-kw val)}})))))]
    (if-not issues
      ent-map
      {::anom/category ::anom/incorrect
       :data/attribute-value-spec-failures (vec issues)})))

(comment
  (s/get-spec :measurement/cell-population)
  (s/valid? :measurement/cell-count 2))
