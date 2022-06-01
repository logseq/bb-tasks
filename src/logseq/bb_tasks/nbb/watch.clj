(ns logseq.bb-tasks.nbb.watch
  "To use tasks in this ns, first install nbb-logseq:
`npm install -g @logseq/nbb-logseq`"
  (:require [pod.babashka.fswatcher :as fw]
            [babashka.tasks :refer [shell]]
            [babashka.classpath :as classpath]
            [clojure.edn :as edn]
            [portal.api :as p])
  (:import (java.time Instant)))

(def last-file (atom nil))

(defn- watch-with-fn
  "Runs watch fn when given dir or script is written to.
NOTE: If the script fails, the watcher stops watching"
  [[dir nbb-script] watch-fn]
  (println "Watching" dir "...")
  (fw/watch dir
            (fn [event]
              ;; Don't use :chmod as it sometimes triggers twice on osx
              (when (#{:write|chmod :write} (:type event))
                (watch-fn nbb-script dir (:path event))
                (reset! last-file (:path event))))
            {:recursive true})
  ;; Get live-editing experience by re-parsing last file
  (fw/watch nbb-script
            (fn [event]
              (when (#{:write|chmod :write} (:type event))
                (watch-fn nbb-script dir @last-file)))))

(defn- run-script
  [nbb-script dir file]
  (shell "nbb-logseq --debug -cp" (classpath/get-classpath) nbb-script dir file))

(defn watch
  "Watch a graph dir and nbb script and run nbb script when either changes.
Nbb takes graph dir and last modified graph file."
  [& args]
  (when-not (= 2 (count args))
    (throw (ex-info "Usage: $0 DIR NBB-SCRIPT" {})))
  (watch-with-fn args run-script)
  (deref (promise)))

(defn- run-script-and-tap
  [nbb-script dir file]
  (let [result (-> (shell {:out :string} "nbb-logseq -cp" (classpath/get-classpath) nbb-script dir file)
                   :out
                   edn/read-string)]
    (println (format "[%s] Query result sent to portal" (str (Instant/now))))
    (tap> (with-meta result {:portal.viewer/default :portal.viewer/table}))))

(defn portal-watch
  "Watches files like `watch` and also sends script results to portal"
  [& args]
  (when-not (= 2 (count args))
    (throw (ex-info "Usage: $0 DIR NBB-SCRIPT" {})))

  (p/open)
  (add-tap #'p/submit)
  (watch-with-fn args run-script-and-tap)
  (deref (promise)))
