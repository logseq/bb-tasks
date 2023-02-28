(ns logseq.bb-tasks.bin.logseq-import
  "A CLI that imports an export file created by logseq-export"
  (:require [babashka.fs :as fs]
            [clojure.string :as string]
            [clojure.edn :as edn]))

(defn -main
  "Entry fn for logseq-import"
  [& args]
  (when (or (empty? args) (contains? (set args) "-h"))
    (println "Usage: logseq-import EXPORT-FILE")
    (System/exit 0))
  (let [exported-files (edn/read-string (slurp (first args)))]
    (when-let [conflicts (->> exported-files
                              (map :file/path)
                              (keep #(when (fs/exists? %) %))
                              seq)]
      (println "Error: These files already exist and should be moved before import -"
               (string/join ", " conflicts))
      (System/exit 1))

    (doseq [{:file/keys [path content]} exported-files]
      (spit path content))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
