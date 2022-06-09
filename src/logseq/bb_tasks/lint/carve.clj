(ns logseq.bb-tasks.lint.carve
  "This ns adds a more friendly commandline interface to carve by merging
  options to the default config."
  (:require [clojure.edn :as edn]
            [pod.borkdude.clj-kondo :as clj-kondo]))

;; HACK: define clj-kondo.core ns which is used by carve
;; TODO: Carve should handle this
(intern (create-ns 'clj-kondo.core) 'run! clj-kondo/run!)
(require '[carve.main])

(defn -main
  "Wrapper around carve.main that defaults to .carve/config.edn and merges
in an optional string of options"
  [& args]
  (let [default-opts (slurp ".carve/config.edn")
         opts (if-let [more-opts (first args)]
                (pr-str (merge (select-keys (edn/read-string default-opts) [:paths :api-namespaces])
                               (edn/read-string more-opts)))
                default-opts)]
    (apply carve.main/-main ["--opts" opts])))
