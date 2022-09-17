(ns logseq.bb-tasks.lint.ns-docstrings
  "Provides lint task to detect all desired namespaces are documented.
  Documented namespaces help teams distribute their knowledge"
  (:require [pod.borkdude.clj-kondo :as clj-kondo]))

(defn- missed-docstrings [paths {:keys [ignore-regex]}]
  (let [analysis (:analysis (clj-kondo/run! {:lint paths
                                             :config {:output {:analysis true}}}))
        {:keys [namespace-definitions]} analysis
        ignore-var? (if ignore-regex
                      #(re-find (re-pattern ignore-regex) %)
                      (constantly false))
        undocumented-namespaces (filter
                                 (fn [{:keys [name doc no-doc lang]}]
                                   (and (not doc)
                                        (not no-doc)
                                        (not= lang :clj)
                                        (not (ignore-var? (str name)))))
                                 namespace-definitions)]
    (if (seq undocumented-namespaces)
      (do
        (println "\nThe following namespaces are undocumented:")
        (run! println (map :name undocumented-namespaces))
        (System/exit 1))
      (println "\nAll namespaces are documented!"))))

(defn -main
  "Lint given classpath for namespaces missing docstrings."
  [& args]
  (let [paths (or (first args) "src")]
    (missed-docstrings [paths] {})))
