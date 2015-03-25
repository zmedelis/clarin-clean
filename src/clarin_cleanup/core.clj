(ns clarin-cleanup.core
  (:import [org.apache.commons.lang3 StringUtils]
           [com.cybozu.labs.langdetect Detector DetectorFactory Language])
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [crg.turtle.parser :refer :all]))

(defonce initialized (DetectorFactory/loadProfile "resources/profiles"))

;helpers to get lines we are interested in
(defn- starts-with? [prefix line] (if (nil? line) false (re-find (re-pattern prefix) (s/trim line))))
(defn title? [line] (starts-with? "dc:title" line))
(defn text? [line] (starts-with? "lpv:text" line))

(defn lang-detect
  "Get language probablilities. Sort them by highest prob and return best"
  [text]
  (let [detector (DetectorFactory/create)]
    (.append detector text)
    (->> detector
         .getProbabilities
         (map #(vector (.lang %)  (.prob %)))
         (sort-by second)
         reverse
         first)))

(defn onto-assigned-language
  "Get language as it is assigned in ontology file.
  Check the @LANG tag at the end of the string"
  [text]
  (if-let [res (re-find #"\"\s*@(\w+)\s*[ \.;,]" text)]
    (second res) nil))

(defn can-fix?
  "Am I allowed to fix language. See if probablilities allow it"
  [existing-lang [detected-lang probability]]
  (cond
    (= existing-lang detected-lang) false
    (> 0.8 probability) false
    :else true))

(defn fix-language
  "Fix langauge tag for the onto text line entry."
  [line]
  (let [onto-lang (onto-assigned-language line)
        detected (lang-detect line)]
    (if (can-fix? onto-lang detected)
      ;This is dangerous, all @LANG patterns will be replaced
      ;in low liklihood that we'll get @LANG in the midle it will
      ;be replaced
      (s/replace line
                 (re-pattern (str "@" onto-lang))
                 (str "@" (first detected)))
      ;leave line as is if not fixing stuff
      line)))

(defn remove-junk
  "Remove junk from text. For now its basicaly space normalization"
  [line]
  ;we need to parse the line into thre parts, see example
  ; dc:title "Ankstesnio posėdžio protokolo patvirtinimas"@lt .
  ; =>
  ;   dc:title
  ;   "Ankstesnio posėdžio protokolo patvirtinimas"
  ;   @lt .
  (let [[_ onto-key onto-val lang] (re-find #"(?s)^(\s*.*?)\"(.*)\"(@\w{2}.*)" line)]
    (if (and onto-key onto-val lang)
      (str
        onto-key
        "\""
        (-> onto-val
            (StringUtils/normalizeSpace)
            (s/replace #"^\s*[-–]\s*(\(\w{2}\))?\s*" "")
            (s/replace #"\(\)$" "")
            s/trim)
        "\""
        lang)
      line)))

(defn process-line
  "Check if line needs proessing and if so do cleanup"
  [line]
  (remove-junk
    (if (or (title? line)
            (text? line))
      (fix-language line)
      line)))

(defn fix-ttl
  "Main entry point. Pass in ttl file for fixing."
  [file-name]
  (io/delete-file (str file-name ".fixed") true)
  (with-open  [rdr (io/reader file-name)]
    (doseq [line (line-seq rdr)]
      (spit (str file-name ".fixed")
                 (str (process-line line) "\n")
                 :append true))))

(defn fix-all [dir]
  (doseq [file (filter #(.isFile %) (file-seq  (io/file dir)))]
    (println "File: " (.getPath file))
    (fix-ttl (.getPath file))))
