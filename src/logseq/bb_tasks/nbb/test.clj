(ns logseq.bb-tasks.nbb.test
  "Tests to run on nbb-logseq projects. Requires @logseq/nbb-logseq to be
  declared as a dev dependency."
  (:require [pod.borkdude.clj-kondo :as clj-kondo]
            [clojure.string :as str]
            [babashka.process :refer [shell]]))

(defn- validate-namespaces
  [namespaces classpath dir]
  (assert (seq namespaces) "There must be some namespaces to check")
  ;; distinct b/c sometimes namespaces are duplicated with .cljc analysis
  (doseq [n (distinct namespaces)]
    (println "Requiring" n "...")
    ;; Run from current dir so that yarn command runs correctly
    (shell {:dir dir} "yarn nbb-logseq -cp" classpath "-e" (format "(require '[%s])" n)))
  (println "Success!"))

(defn load-all-namespaces
  "Verify that all namespaces in a directory can be required by nbb-logseq. Uses
  directory's deps.edn for classpath"
  [dir]
  (let [{{:keys [namespace-definitions]} :analysis}
        (clj-kondo/run!
         {:lint (map #(str dir "/" %) ["src"])
          :config {:output {:analysis {:namespace-definitions {:lang :cljs}}}}})]
    (validate-namespaces (map :name namespace-definitions)
                         (str/trim (:out (shell {:dir dir :out :string} "clojure -Spath")))
                         dir)))
