(ns logseq.bb-tasks.util
  "Misc util fns"
  (:require [clojure.edn :as edn]))

(defn read-tasks-config
  "Read task configuration under :tasks/config of current bb.edn"
  []
  (-> (System/getProperty "babashka.config")
      slurp
      edn/read-string
      :tasks/config))
