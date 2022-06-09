(ns logseq.bb-tasks.lint.datalog
  (:require [datalog.parser.impl :as parser-impl]
            [dlint.core :as dlint]))

(defn- lint-unbound-rule [rule]
  (->> (dlint/lint [rule])
       (keep
        (fn [[k v]]
          (when (seq v)
            {:success false :name k :rule rule :unbound-vars v})))))

(defn- lint-rule [rule]
  (try (parser-impl/parse-rule rule)
    {:success true :rule rule}
    (catch Exception e
      {:success false :rule rule :error (.getMessage e)})))

(defn lint-rules
  "Lint datalog rules for parse-ability and unbound variables"
  [rules]
  (let [invalid-unbound-rules (->> rules
                                   (mapcat lint-unbound-rule)
                                   (remove :success))
        invalid-rules (->> rules
                           (map lint-rule)
                           (remove :success))
        lint-results (concat invalid-unbound-rules invalid-rules)]
    (if (seq lint-results)
      (do
        (println (count lint-results) "rules failed to lint:")
        (println lint-results)
        (System/exit 1))
      (println (count rules) "datalog rules linted fine!"))))
