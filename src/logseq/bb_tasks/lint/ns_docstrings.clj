(ns logseq.bb-tasks.lint.ns-docstrings
  "Provides lint task to detect all desired namespaces are documented.
  Documented namespaces help teams distribute their knowledge"
  (:require [pod.borkdude.clj-kondo :as clj-kondo]
            [logseq.bb-tasks.util :as util]))

(defn- get-undocumented-namespaces [paths {:keys [ignore-regex] :as config}]
  (let [{{:keys [namespace-definitions]} :analysis}
        (clj-kondo/run! {:lint paths
                         :config {:output {:analysis true}}})
        ignore-var? (if ignore-regex
                      #(re-find (re-pattern ignore-regex) %)
                      (constantly false))
        undocumented-namespaces (filter
                                 (fn [{:keys [name doc no-doc lang]}]
                                   (and (not doc)
                                        (not no-doc)
                                        ;; Most langs are weirdly nil
                                        (contains? #{nil (:lang config)} lang)
                                        (not (ignore-var? (str name)))))
                                 namespace-definitions)]
    undocumented-namespaces))

(defn- read-config
  []
  (merge
   {:lang :cljs}
   (:ns-docstrings (util/read-tasks-config))))

(defn -main
  "Lint given classpath for namespaces missing docstrings."
  [& args]
  (let [config (read-config)
        paths (or (seq args)
                  (:paths config)
                  ["src"])
        namespaces (get-undocumented-namespaces paths config)]
    (if (seq namespaces)
      (do
        (println "\nThe following namespaces are undocumented:")
        (run! println (map :name namespaces))
        (System/exit 1))
      (println "\nAll namespaces are documented!"))))
