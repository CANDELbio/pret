(ns org.candelbio.pret.util.timestamps
  (:import (java.text SimpleDateFormat)
           (java.util Date)))

(defn format-date [date]
  (.format (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm") date))

(defn now []
  (format-date (Date.)))
