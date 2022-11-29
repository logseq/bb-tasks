(ns logseq.bb-tasks.nbb.cached-db
  "Handles nbb side of cached-db functionality. File path and cache implementation
encapsulated in this ns"
  (:require [datascript.transit :as dt]
            [datascript.core :as d]
            [logseq.graph-parser.cli :as gp-cli]
            [logseq.graph-parser :as graph-parser]
            ["fs" :as fs]))

(def ^:private cache-file
  "Cache file for storing transit db"
  ".cached-db-transit.json")

(defn ^:api read-db
  "Reads db from cache file"
  []
  (dt/read-transit-str (fs/readFileSync cache-file)))

(defn write-db
  "Writes cache db file given the graph directory. First time writing the cache
  can take long for large graphs since all files are parsed. Subsequent writes
  are quick as the cache only needs to update changed files."
  [dir changed-files]
  (if (fs/existsSync cache-file)
    (let [old-conn (d/conn-from-db (read-db))
          files (map
                 #(hash-map :file/path % :file/content (str (fs/readFileSync %)))
                 changed-files)
          delete-blocks (fn [db file-page file-path uuid-blocks]
                          (into (graph-parser/get-blocks-to-delete db file-page file-path uuid-blocks)
                                ;; Delete page to allow for page properties to be redefined
                                ;; Assumes no page rename for now
                                [[:db.fn/retractEntity [:block/name (:block/name file-page)]]]))
          conn (:conn (gp-cli/parse-graph dir
                                          {:conn old-conn
                                           :files files
                                           :verbose true
                                           :parse-file-options {:delete-blocks-fn delete-blocks}}))]
      (println "Updating cache file" cache-file)
      (fs/writeFileSync cache-file (dt/write-transit-str @conn)))
    (let [conn (:conn (gp-cli/parse-graph dir {:verbose false}))]
      (println "Writing cache file" cache-file)
      (fs/writeFileSync cache-file (dt/write-transit-str @conn))))
  nil)
