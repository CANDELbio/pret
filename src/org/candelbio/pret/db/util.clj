(ns org.candelbio.pret.db.util)

(defn reverse-ref
  "given a datomic reference attr keyword, return the reverse reference form"
  [ref-attr]
  (let [name (name ref-attr)
        ns (namespace ref-attr)
        rev-name (str "_" name)]
    (keyword ns rev-name)))
