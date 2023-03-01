(ns logseq.bb-tasks.bin.logseq-export
  "A CLI that exports a graph's files for use with logseq-import"
  (:require [babashka.fs :as fs]
            [clojure.string :as string]
            [clojure.pprint :as pprint]))

(defn ^:api -main
  "Entry fn for logseq-export"
  [& files]
  (when (or (empty? files) (contains? (set files) "-h"))
    (println "Usage: logseq-export [& FILES]")
    (System/exit 0))
  (when-let [nonexistent (seq (remove #(fs/exists? %) files))]
    (println "Error: These files don't exist -" (string/join ", " nonexistent))
    (System/exit 1))

  (let [files (mapv #(hash-map :file/path % :file/content (slurp %))
                    files)]
    (pprint/pprint files)))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
