(ns org.candelbio.pret.util.text
  (:require [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]))

(defn match-ns
  "Predicate to determine if the namespace of x is equal to ns."
  [ns x]
  (and (keyword? x)
       (namespace x)
       (str/starts-with? (namespace x) ns)))

(defn ->pretty-string
  "Returns a pretty string of x."
  [x]
  (with-out-str (pprint x)))

(defn ->str
  "Simple to string for keywords and other datatypes of x."
  [x]
  (if (keyword? x)
    (subs (str x) 1)
    (str x)))

(defn file->str
  "Coerce file to string or preserve filename as string."
  [f]
  (if (string? f)
    f
    (.getAbsolutePath f)))

(def windows-absolute-path-start-re #"(?i)^(\w)\:.*")

(defn filename-absolute?
  "Determines if filename x is absolute (works for both Windows and Mac/Linux)."
  [x]
  (when x
    (let [s (str x)]
      (or (str/starts-with? s "/")
          (boolean (re-matches windows-absolute-path-start-re s))))))

(defn filename-relative?
  "Determines if filename x is relative (works for both Windows and Mac/Linux)."
  [x]
  (not (filename-absolute? x)))


(defn last-index-of
  "Applies string/last-index-of to the string s and the characters in the collection x (one at a time).
  Returns the first non-nil result"
  [s x]
  (->> (keep (partial str/last-index-of s) x)
       first))

(defn filename
  "Extracts the string filename from a qualified string path to file x."
  [x]
  (when x
    (let [s (->str x)
          sin (last-index-of s ["/" "\\"])]
      (case sin
        nil s
        (when (< sin (dec (count s)))
          (subs s (inc sin)))))))

(defn folder
  "Extracts the string folder path from a qualified string path to file x."
  [x]
  (when x
    (let [s (->str x)
          sin (last-index-of s ["/" "\\"])]
      (case sin
        nil s
        (when (> sin 0)
          (subs s 0 sin))))))

(defn folder-of
  "Extracts the fully-qualified folder string of the filename s."
  [s]
  (-> s
      io/file
      (.getCanonicalPath)
      folder))

(defn stacktrace->string
  "Converts a throwable t to a string stack trace."
  [t]
  (str/join "\n  - " (map str (.getStackTrace t))))
