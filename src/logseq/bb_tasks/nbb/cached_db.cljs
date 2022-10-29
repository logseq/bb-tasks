(ns logseq.bb-tasks.nbb.cached-db
  "Handles nbb side of cached-db functionality. File path and cache implementation
encapsulated in this ns"
  (:require [datascript.transit :as dt]
            [logseq.graph-parser.cli :as gp-cli]
            ["fs" :as fs]))

(def ^:private cache-file
  "Cache file for storing transit db"
  ".transit-db-cache.json")

(defn read-db
  "Reads db from cache file"
  []
  (dt/read-transit-str (fs/readFileSync cache-file)))

(defn write-db
  "Writes cache db file given the graph directory"
  [dir]
  (let [conn (:conn (gp-cli/parse-graph dir {:verbose false}))]
    (println "Writing cache file" cache-file)
    (fs/writeFileSync cache-file (dt/write-transit-str @conn))
    nil))
