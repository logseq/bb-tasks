(ns logseq.bb-tasks.lint.large-vars
  "Provides lint task to detect large vars. Large vars make it difficult for
  teams to maintain and understand codebases."
  (:require [pod.borkdude.clj-kondo :as clj-kondo]
            [babashka.fs :as fs]
            [clojure.pprint :as pprint]
            [clojure.edn :as edn]
            [clojure.set :as set]
            [logseq.bb-tasks.util :as util]))

(def ^:private default-config
  {:max-lines-count 35
   :lang :cljs
   ;; Vars with these metadata flags are allowed. Name should indicate the
   ;; reason it is allowed
   :metadata-exceptions #{:large-vars/data-var}})

(defn- read-config
  [config-arg]
  (merge-with
   (fn deep-merge [old new] (if (coll? new) (into old new) new))
   default-config
   (:large-vars (util/read-tasks-config))
   (some->> config-arg edn/read-string (merge default-config))))

(defn -main
  "Lints given classpath for vars that are too large.
Takes optional second argument which is EDN config."
  [& args]
  (let [paths [(or (first args) "src")]
        _ (doseq [f paths]
            (when-not (fs/exists? f)
              (throw (ex-info (str f " is not a valid path") {}))))
        config (read-config (second args))
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
