(ns logseq.bb-tasks.nbb.cached-db
  "Handles bb side of cached-db functionality"
  (:require [babashka.fs :as fs]
            [babashka.process :refer [shell]]))

(defn ensure-latest-cached-db
  "Writes a new cached db if any of the graph's files have changed"
  ;; :depends fn are weirdly pass their dependent's args so allow anything through
  [& _args]
  (when-let [files (seq (fs/modified-since (fs/file "script/.transit-db-cache.json")
                                           (fs/glob "." "{pages,journals,assets,logseq,whiteboards,draws}/*")))]
    (println (format "Updating db since %s file(s) have changed!" (count files)))
    (shell {:dir "script"} "yarn nbb-logseq -e"
           (str "(require '[logseq.bb-tasks.nbb.cached-db]) "
                "(logseq.bb-tasks.nbb.cached-db/write-db \"..\")"))))
