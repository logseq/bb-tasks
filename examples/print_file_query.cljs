(ns print-file-query
  "This is an example script for use with
  logseq.bb-tasks.nbb.watch/portal-watch. The query is not important but
  printing it as edn is so that portal can render it"
  (:require [logseq.graph-parser.cli :as gp-cli]
            [datascript.core :as d]))

(defn- print-query-result
  [db file]
  (let [results (map first
                     (d/q '[:find (pull ?b [*])
                            :in $ ?path
                            :where
                            [?b :block/page ?page]
                            [?page :block/file ?file]
                            [?file :file/path ?path]]
                          db
                          file))]
    (prn results)))

(defn -main
  "Prints blocks for given file along with basic file stats"
  [& args]
  (when-not (= 2 (count args))
    (throw (ex-info "Usage: $0 DIR FILE" {})))
  (let [[dir file] args
        {:keys [conn]} (gp-cli/parse-graph dir
                                           {:verbose false
                                            :files [{:file/path file
                                                     :file/content (gp-cli/slurp file)}]})]
    (print-query-result @conn file)))

(apply -main *command-line-args*)
