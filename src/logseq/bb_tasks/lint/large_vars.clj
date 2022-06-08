(ns logseq.bb-tasks.lint.large-vars
  "Provides lint task to detect large vars. Large vars make it difficult for
  teams to maintain and understand codebases."
  (:require [pod.borkdude.clj-kondo :as clj-kondo]
            [clojure.pprint :as pprint]
            [clojure.edn :as edn]
            [clojure.set :as set]))

(def default-config
  {:max-lines-count 40
   :lang :cljs
   ;; Vars with these metadata flags are allowed. Name should indicate the reason
   ;; it is allowed
   :metadata-exceptions #{:large-vars/data-var
                          ;; TODO: Address vars tagged with cleanup-todo. These
                          ;; are left mostly because they are not high priority
                          ;; or not well understood
                          :large-vars/cleanup-todo}})

(defn lint
  "Lints given classpath for vars that are too large.
Takes optional second argument which is EDN config."
  [& args]
  (let [paths [(or (first args) "src")]
        config (or (some->> (second args) edn/read-string (merge default-config))
                   default-config)
        {{:keys [var-definitions]} :analysis}
        (clj-kondo/run!
         {:lint paths
          :config {:output {:analysis {:var-definitions {:meta true
                                                         :lang (:lang config)}}}}})
        vars (->> var-definitions
                  (keep (fn [m]
                          (let [lines-count (inc (- (:end-row m) (:row m)))]
                            (when (and (> lines-count (:max-lines-count config))
                                       (empty? (set/intersection (set (keys (:meta m)))
                                                                 (:metadata-exceptions config))))
                              {:var (:name m)
                               :lines-count lines-count
                               :filename (:filename m)}))))
                  (sort-by :lines-count (fn [x y] (compare y x))))]
    (if (seq vars)
      (do
        (println (format "\nThe following vars exceed the line count max of %s:"
                         (:max-lines-count config)))
        (pprint/print-table vars)
        (System/exit 1))
      (println "All vars are below the max size!"))))
