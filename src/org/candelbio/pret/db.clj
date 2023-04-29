(ns org.candelbio.pret.db
  (:require [datomic.client.api :as d]
            [clojure.core.async :as a]
            [org.candelbio.pret.db.backend :as backend]
            [org.candelbio.pret.db.query :as dq]
            [org.candelbio.pret.db.transact :as db.tx]
            [org.candelbio.pret.bootstrap.data :as bootstrap.data]
            [clojure.tools.logging :as log]
            [org.candelbio.pret.db.schema :as schema]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; Non-master database peer/client API info -----------------------------
;;

(defn fetch-info [database]
  (let [info (backend/database-info database)]
    (if (some? info)
      (assoc info :database database)
      (let [msg (str "Couldn't load datomic info for branch " database)]
        (throw (ex-info
                 msg
                 {:candelabra/access {:message msg}}))))))


;; Peer Fns ----------------------------------------------------------------
;;


;;; Only used by diff and validation?

#_
(defn get-connection [info]
  (backend/connect (:uri info)))

(defn latest-db [info]
  (backend/db info))







(defn exists? [datomic-uri]
  (try
    (backend/connect datomic-uri)
    true
    (catch RuntimeException e
      ;; since no ex-info on distributed Datomic, and only get RuntimeException,
      ;; this string hack lets us see if we get failure for the _right_ reason.
      (let [msg (.getMessage e)]
        (if (and msg (.startsWith msg "Could not find"))
          false
          (throw (ex-info "Could not connect to Datomic."
                   {:datomic/could-not-connect msg})))))))

(defn transact-bootstrap-data
  "Transact bootstrap data."
  [conn local-file-path]
  (a/<!! (:result (db.tx/run-txns! conn [local-file-path] 20))))

(defn tx-effect?
  "Given a connection and map with tx-data and query, determines whether or not transacting
  the data in tx-data would change the results of query. Note: work is done on `conn`, so
  be wary of races and don't rely on this outside of e.g. constrained additive schema."
  [conn {:keys [tx-data query name]}]
  (let [db (d/db conn)
        {:keys [db-after]} (d/with db tx-data)]
      (not= (dq/q+retry query db)
            (dq/q+retry query db-after))))

(defn fulfilled?
  "Given a validation map, returns `true` if this database fulfills validation
  described in map. A validation map consists of a query and its expected results."
  [db {:keys [query expected]}]
  (= (dq/q+retry query db)
     expected))


(defn connect
  [uri]
  (backend/connect uri))

(defn apply-schema [datomic-uri]
  (let [conn (connect datomic-uri)
        schema-work schema/schema-txes]
    (doseq [raw-tx schema-work]
      ;; if a schema attr is not indexed, we add index true. this allows us to keep
      ;; schema edn in resources datomic impl agnostic while optimizing on-prem queries.
      (let [tx raw-tx #_ (update-in raw-tx [:tx-data]
                          (fn [tx-data]
                            (mapv (fn [schema-ent]
                                    (if (and (:db/valueType schema-ent)
                                             (not (:db/unique schema-ent)))
                                       (assoc schema-ent :db/index true)
                                       schema-ent))
                                 tx-data)))]
        (if true #_ (tx-effect? conn tx)
          (do (log/info ::schema (:name tx) " not in database, transacting.")
              (db.tx/sync+retry conn (:tx-data tx)))
          (log/info ::schema "Skipping schema install for: " (:name tx)))))))





(defn version [datomic-uri]
  (let [conn (connect datomic-uri)
        db (d/db conn)]
    (-> db
        (d/pull '[:candel.schema/version] :candel/schema)
        (:candel.schema/version))))

(defn init
  "Loads all base schema, enums, and metamodel into database if necessary."
  [datomic-uri & {:keys [skip-bootstrap seed-data-dir include-proprietary]}]
  (let [;; _ (backend/create-database datomic-uri) ;?? shoudln't this be done earlier
        ;; _ (do (log/info "Database created."))
        ;; db isn't ready yet if it hasn't been created, this timeout seems sufficient
        conn (connect datomic-uri)
        _ (log/info "Connected to database")]
    (apply-schema datomic-uri)
    (when-not skip-bootstrap
      (doseq [dataset (if-not include-proprietary
                        (bootstrap.data/open-datasets)
                        (bootstrap.data/all-datasets))]
        (println "\nDataset: " dataset "\n")
        (if-not (fulfilled? (d/db conn) dataset)
          (let [{:keys [name]} dataset
                version (version datomic-uri)
                files (if seed-data-dir
                        (map #(io/file seed-data-dir %) (dataset :files))
                        (bootstrap.data/maybe-download version dataset))]
            (log/info ::bootstrap-data "Dataset " name " not present, installing.")
            (doseq [f files]
              (println "\n" (.toString f) "\n")
              (transact-bootstrap-data conn f)))
          (log/info ::bootstrap-data "Dataset " (:name dataset) " already present, skipping."))))
    datomic-uri))


(defn version->map
  [version-str]
  (zipmap [:major :minor :revision]
          (->> (str/split version-str #"\.")
               (map #(Long/parseLong %)))))

(defn compare-schema-version
  "Compares schema at datomic-uri to cached schema in pret URI. Returns a kw indicating whether
  or not the schema are :incompatible, :identical, or :compatible"
  [datomic-uri]
  (let [db-schema-map (-> datomic-uri version version->map)
        pret-schema-map (-> (schema/version) version->map)]
    (cond
      (or (not= (:major db-schema-map) (:major pret-schema-map))
          (not= (:minor db-schema-map) (:minor pret-schema-map)))
      :incompatible

      (= (:revision db-schema-map) (:revision pret-schema-map))
      :identical

      :else
      :compatible)))


(defn ensure-db
  "Returns map with schema included as a key, will throw at Datomic call level if
  unable to connect to database. Will also throw if versions are incompatible."
  [datomic-uri]
  (let [version-outcome (compare-schema-version datomic-uri)]
    (cond
      (= version-outcome :identical)
      datomic-uri

      (= version-outcome :compatible)
      (do (log/info "Compatible schema installed, applying necessary updates.")
          (init datomic-uri :skip-bootstrap true)
          datomic-uri)

      (= version-outcome :incompatible)
      (throw (ex-info "Version of candel schema in database is not compatible."
                      {:candel.schema/version {:db/version (version datomic-uri)
                                               :pret/version (schema/version)}})))))


(defn contains-txn?
  "Does the database contain the id (ie. :import/txn-id)"
  [db txn-id]
  (some?
    (ffirst (d/q '[:find ?e
                   :in $ ?id
                   :where
                   [?e :import/txn-id ?id]] db txn-id))))

(defn touch
  [db id]
  #_ (d/touch (d/entity db id))
  nil)    ;TODO
  

(defn head
  "Returns metadata about the last transaction:

  {:timestamp ...
   :txn-id ...
   :import-name ...}"
  [db]
  (let [txn (ffirst
              (d/q
                '[:find (max 1 ?tx)
                  :where
                  [?tx :db/txInstant]]
                db))
        txn-data (touch (first txn))
        import-name (if (contains? txn-data :import/import)
                      (-> (touch db (-> (:import/import txn-data)
                                                    :db/id))
                          :import/name)
                      (throw (ex-info "No datasets transacted"
                                      {:error :no-imports-on-database})))]
    {:timestamp (:db/txInstant txn-data)
     :txn-id (:import/txn-id txn-data)
     :import-name  import-name}))

(defn ordered-imports
  "Returns all import entity data. The import entity is the first entity to be
  imported during transact. They are ordered by date and have the form:
  {:name ...
   :txn-id <transaction entity id>
   :timestamp ...}"
  [db]
  (->> (d/q '[:find ?name ?tx
              :where
              [_ :import/name ?name ?tx]]
            db)
       (map
         (fn [[name tx-id]]
           (let [tx (touch db tx-id)]
             {:timestamp (:db/txInstant tx)
              :import-name name
              :ent-id tx-id})))
       (sort-by
         :timestamp
         #(.before %2 %1))))

