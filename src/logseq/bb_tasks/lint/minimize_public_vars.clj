(ns logseq.bb-tasks.lint.minimize-public-vars
  "Linter that finds public vars to make private. This is useful in conjunction
  with the clj-kondo :missing-docstring linter to minimize the amount of vars to
  document"
  (:require [pod.borkdude.clj-kondo :as clj-kondo]
            [clojure.set :as set]))

(defn- find-public-vars-to-make-private [paths]
  (let [analysis (:analysis (clj-kondo/run! {:lint paths
                                             :config {:output {:analysis {:var-definitions {:meta true}}}}}))
        {:keys [var-usages var-definitions]} analysis
        var-meta-map (into {}
                           (keep #(when (:meta %)
                                    [(str (:ns %) "/" (:name %))
                                     (:meta %)])
                                 var-definitions))
        indexed-usages
        (reduce (fn [acc {:keys [to name from from-var]  :as usage}]
                  (let [var (str to "/" name)]
                    (if (= to from)
                      ;; an entry already exists
                      (if (or (get acc var)
                              ;; multi-arity false positive
                              (= var (str from "/" from-var))
                              ;; var has :api flag to indicate it is a
                              ;; purposefully public fn. Chose :api so it can be
                              ;; used across tools
                              (get-in var-meta-map [var :api]))
                        acc
                        ;; Would be more explicit if this was set to :private
                        (assoc acc var []))
                      ;; only save usages that are external to the var's namespace
                      (update acc var (fnil conj []) usage))))
                {}
                var-usages)
        should-be-private-vars (set (map first
                                         (filter (fn [[_k v]] (empty? v))
                                                 indexed-usages)))
        private-vars (set (keep (fn [{:keys [private ns name]}]
                                  (when private (str ns "/" name)))
                                var-definitions))]
    (set/difference should-be-private-vars private-vars)))

(defn -main
  "Lint given classpaths for vars that should be private. Should be is
  determined by if a var has any usage outside its own ns"
  [& args]
  (let [path (or (first args) "src")
        vars-to-make-private (find-public-vars-to-make-private [path])]
    (if (seq vars-to-make-private)
      (do
        (println "\nFound the following vars that should be private:")
        (run! println (sort vars-to-make-private))
        (System/exit 1))
      (println "\nSuccess!"))))
