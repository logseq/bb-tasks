(ns logseq.bb-tasks.util
  (:require [clojure.edn :as edn]))

(defn read-tasks-config
  []
  (-> (System/getProperty "babashka.config")
      slurp
      edn/read-string
      :tasks/config))
