(ns user
  (:require [datomic.api :as d]
            [org.candelbio.pret.util.io :as util.io]
            [org.candelbio.pret.db :as db]
            [org.candelbio.pret.db.schema :as db.schema]
            [clojure.string :as str]))


(comment
  (def db-name "Morrison2_matrix_import")

  ;; list datasets in a database
  (def list-q
    '[:find ?e ?dname
      :in $
      :where
      [?e :dataset/name ?dname]])

  (d/q list-q (d/db conn)))


(comment
  :deena-query-troubleshoot
  ;; env: pici-prod-v1

  (take 100 (d/q '[:find (pull ?d [*])
                   :where
                   [?d :drug/preferred-name ?drug-name]]
                 db))

  (d/q '[:find (sample 100 ?drug-name)
         :in $
         :where
         [?d :drug/preferred-name ?drug-name]]
       db)

  (d/q '[:find [?ds-name ...]
         :in $
         :where
         [?ds :dataset/name ?ds-name]]
       db)

  (d/q '[:find ?assay-name ?measurement-set-name ?sample-id ?value ?cell-population-name ?epitope-id
         :in $ ?epitope-id [?cell-population-name ...]
         :where
         [?p :epitope/id ?epitope-id]
         [?m :measurement/epitope ?p]
         [?c :cell-population/name ?cell-population-name]
         [?m :measurement/cell-population ?c]
         [?m :measurement/median-channel-value ?value]
         [?e :measurement-set/measurements ?m]
         [?e :measurement-set/name ?measurement-set-name]
         [?a :assay/measurement-sets ?e]
         [?a :assay/name ?assay-name]
         [?d :dataset/assays ?a]
         [?d :dataset/name ?dataset-name]
         [?m :measurement/sample ?s]
         [?s :sample/id ?sample-id]]
       db "PDCD1"
       ["CD4 > CD45RA-CD27- > EM2 > CD185+" "CD4 > CD45RA-CD27+ > EM1 > CD185+"
        "CD4 > EMRA > EMRA CCR7- > CD185+"])

  (def q-results
    (d/q '[:find ?sample-id ?value ?cell-population-name
           :in $ ?epitope-id ?dataset-name ?assay-name ?measurement-set-name
           :where
           [?p :epitope/id ?epitope-id] ;;; avet unique lookup
           [?m :measurement/epitope ?p] ;;; 626,467 via vaet
           [?m :measurement/cell-population ?c] ;;; eavt (or reverse vaet?), does not expand past linear
           [?c :cell-population/name ?cell-population-name] ;; eavt, does not expand past linear
           [?m :measurement/median-channel-value ?value] ;; eavt, does not expand past linear
           [?m :measurement/sample ?s] ;; eavt (or reverse vaet?) does not expand past linear
           [?s :sample/id ?sample-id]
           [?e :measurement-set/name ?measurement-set-name]
           [?a :assay/measurement-sets ?e]
           [?a :assay/name ?assay-name]
           [?d :dataset/assays ?a]
           [?d :dataset/name ?dataset-name]]
         db "KI67" "pici0002" "X50" "gated-mfi")))

(comment
  (d/pull db '[*] [:epitope/id "KI67"])

  (count (seq (d/datoms db :vaet 17592186249383 :measurement/epitope)))
  ;; 626467
  (count (seq (d/datoms db :aevt :cell-population/name)))
  ;; 3640

  (count (seq (d/datoms db :aevt :measurement/median-channel-value)))
  (count (seq (d/datoms db :aevt :measurement/cell-population))))


(comment
   (require '[datomic.api :as d])
   (def db-uri "datomic:ddb://us-east-1/candel-prod/pici0002-ph2-30")
   (def conn (d/connect db-uri))
   (def db (d/db conn))
   (d/q '[:find ?tcr
          :where
          [?tcr :tcr/uid ?id]]
        db)

   (require '[clojure.pprint :as pp])

   (d/attribute db :tcr/uid)

   (d/q '[:find 17592203521376])

   @(d/with db [[:db/id]])

   (d/pull db '[*] 17592203421519)


   (-> (d/pull db '[* {:measurement/_tcr [*]}] 17592203521318)
       (:measurement/_tcr)
       (pprint)))

(comment
  :uniprot-stuff
  (def db-info (db/fetch-info "pici0002-ph2-39"))
  (def db (db/latest-db db-info))

  (def prot->gene
    (d/q '[:find ?hugo ?uniprot
           :in $
           :where
           [?g :gene/hgnc-symbol ?hugo]
           [?p :protein/gene ?g]
           [?p :protein/uniprot-name ?uniprot]]
         db))

  (take 100 prot->gene)

  (d/q '[:find (count ?prev-name)
         :in $
         :where
         [_ :gene/previous-hgnc-symbols ?prev-name]]
       db)

  (count prot->gene)

  (def grouped
    (->> prot->gene
         (group-by first)
         (filter (fn [[uniprot maps-to]]
                   (> 1 (count maps-to))))))
  (def sample (take 100 grouped))
  sample)

(comment
  :jmaxey-validation-issue

  (def db-info (db/fetch-info "Morrison2_matrix_import"))
  (def db-uri (:uri db-info))
  (def conn (d/connect db-uri))
  (def db (d/db conn))

  (require '[clojure.pprint :as pp])

  (def q-results
    (d/q '[:find ?dname ?aname ?msname ?ms
           :in $
           :where
           [?d :dataset/name ?dname]
           [?d :dataset/assays ?a]
           [?a :assay/name ?aname]
           [?a :assay/measurement-sets ?ms]
           [?ms :measurement-set/name ?msname]]
         db))

  (pp/pprint (vec q-results))

  (pp/pprint (dissoc (d/pull db '[* {:measurement-set/measurement-matrices [*]}] 17592186337992)
                     :measurement-set/single-cells)))



(comment
  :mtravers-coords-issue
  (def db-info (db/fetch-info "mt-abida2019-14"))
  (def db-uri (:uri db-info))
  (def conn (d/connect db-uri))
  (def db (d/db conn))

  (d/q '[:find (count ?g)
         :in $
         :where
         [?g :genomic-coordinate/id]]
       db)

  (d/q '[:find (count-distinct ?g)
         :in $
         :where
         [?g :genomic-coordinate/id]]
       db)

  (d/q '[:find (count ?g)
         :in $
         :where
         [?g :genomic-coordinate/id]
         [?g :genomic-coordinate/assembly :genomic-coordinate.assembly/GRCh38]]
       db)


  (d/q '[:find (count ?g)
         :in $
         :where
         [?g :genomic-coordinate/id]
         [?g :genomic-coordinate/assembly :genomic-coordinate.assembly/GRCh37]]
       db)

  (def ids
    (d/q '[:find ?g-id
           :in $
           :where
           [?g :genomic-coordinate/id ?g-id]
           [?g :genomic-coordinate/assembly :genomic-coordinate.assembly/GRCh38]]
         db))

  (def ids
    (d/q '[:find ?g-id
           :in $
           :where
           [?g :genomic-coordinate/id ?g-id]
           [?g :genomic-coordinate/assembly :genomic-coordinate.assembly/GRCh37]]
         db))

  (d/q '[:find (count ?g-id)
         :in $
         :where
         [?g :genomic-coordinate/id ?g-id]
         [(clojure.string/starts-with? ?g-id "GRCh38")]
         [?g :genomic-coordinate.assembly/GRCh37]]
       db)

  (require '[clojure.string :as str])
  (take 10 ids)
  (count
    (keep (fn [s]
            (when (str/starts-with? s "GRCh38")
              s))
          (map first ids)))

  (require '[clojure.pprint :as pp])
  (pp/pprint
    (d/pull db
            '[* {:genomic-coordinate/assembly [:db/ident]}]
            [:genomic-coordinate/id "GRCh38:chr3:+:42013802:42225890"]))


  :end)

