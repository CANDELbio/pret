(ns org.candelbio.pret.db.query
  (:require [datomic.client.api :as d]))

(defn q+retry
  "Invoke Datomic query wrapped in retry with simple linear retry logic.

  Note: this reflects previous client implementation for query. Not strictly
  necessary to peer, but does reflect separation of concerns: individual
  callers point to common params/retry. In Datomic peer/on-prem this is managed
  for us, but if this is ever refactored to use query in client, we want to
  constrain it so that all callers use retry, etc. set here."
  [& args]
  (apply d/q args))
