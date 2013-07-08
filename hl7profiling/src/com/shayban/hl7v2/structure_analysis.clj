(ns com.shayban.hl7v2.structure-analysis
  (:gen-class)
  (:require [com.shayban.hl7v2.api :as hl7]
           [clojure.core.cache :as c]
           [clojure.core.reducers :as r]
           [clojure.java.io :as io]
           [clojure.string :as str]
           [clojure.tools.cli :refer (cli)]
           [com.shayban.hl7v2.bench :refer (native-xz-input-stream)]
           [com.shayban.hl7v2.output-stats :refer (spit-stats)]
           [com.shayban.hl7v2.codec :refer (hl7-messages)]
           [com.shayban.hl7v2.parse :refer
                            (*unescape* read-message string-reader)]))

(defn fingerprint
  "Fingerprint a message, returning a structure
   that looks like: [event [segments ...]]"
  [m]
  
  [(-> m
      (hl7/segment "MSH")
      (hl7/field 9))
   (hl7/structure m)])

(defn ->stream
  "Creates a reader from a File or StdIn or an xz file"
  [option]
  (cond
    (= option "-")
    (clojure.java.io/reader System/in :buffer-size 1024000)
    (.endsWith option ".xz")
    (native-xz-input-stream option)
    :else
    (clojure.java.io/reader option)))

(def ^{:const true
       :doc "This is the cap for items remembered in a single
             field's cache"} NUM-ENTRIES 250)

(defn remember
  "Hot caching action"
  [cache val]
  (if (c/has? cache val)
    (c/hit cache val)
    (c/miss cache val nil)))

(defn warm-nested-cache
  "Updates the nested cache with a key and value"
  [cache-map k val]
  (if-let [cache (get cache-map k)]
    (assoc cache-map k (remember cache val))
    (assoc cache-map k (remember (c/lu-cache-factory {} :threshold NUM-ENTRIES) 
                                 val))))

(defn lu-cache-frequency
  "This pulls the raw stats out of a clojure.core.cache Least Used Cache"
  [cache]
  (let [freqs (.-lu cache)] ;; pull out internal cache hit stats
    (into {} (for [k (keys cache)]
               [k (get freqs k)]))))

(defn collect-field-stats
  "Shoves all fields into a nested cache"
  [msgs]
  (let [caches (r/reduce #(warm-nested-cache %1 (dissoc %2 :value)
                                              (get %2 :value))
                       {}
                       (->> msgs (r/mapcat :segments)
                                (r/mapcat hl7/field-seq)))]
    (into {}
      (for [[field cache] caches]
        [field (lu-cache-frequency cache)]))))

(defn probable-field-type
  "Guesses what kind of field you're dealing with based on cardinality"
  [stat]
  (let [cnt (count stat)]
    (cond
      (< cnt 2)
      {:field-type :nil-or-constant
       :values stat}
      (>= cnt NUM-ENTRIES)
      {:field-type :unique}
      :else
      {:field-type :dictionary
       :values stat})))

(defn interpret-stats
  [field-stats]
  (into []
    (for [[fld statistics] field-stats]
      (merge fld (probable-field-type statistics)))))

(defmacro analyze-stream [f]
  `(with-open [f# ~f]
     (binding [*unescape* false]
       (-> f#
           hl7-messages
           collect-field-stats
           interpret-stats))))

(defmulti run-task :task)

;; this task needs an output directory specified,
;; it shoves all field stats into a file there
(defmethod run-task :analyze
  [mopts]
  (println "Running stats...")
  (spit-stats (analyze-stream (->stream (:file mopts)))
                 (:output-dir mopts)))

;; if you specify multiple fields, only one line per message will be
;; printed, the first instance of each field.
;; However, if you specify a single field, all it's instances will be
;; printed per message

(defmethod run-task :snip
  [mopts]
  (let [field-fns (mapv (comp hl7/accessor hl7/parse-field-spec) (:args mopts))]
    (if (= (count field-fns) 0)
      (throw (ex-info "You must choose a field for output" {}))
      (binding [*unescape* (:unescape mopts)
                *flush-on-newline* false]
        (with-open [rdr (->stream (:file mopts))]
          (reduce (fn [_ msg]
                    (let [data (mapv #(% msg) field-fns)]
                      (if (= (count field-fns) 1)
                        (doseq [line data]
                          (println (str (first line))))
                        (println (str/join (:delim mopts)
                                           (map (comp str first) data))))))
                  nil
                  (hl7-messages rdr)))))))

(def cmdopts [["-f" "--file" "A file, or - for stdin"]
              ["-o" "--output-dir"
               "Output directory, must exist. Used by the analyze task"
               :default "out"]
              ["-d" "--delim" "Delimiter for" :default "\t"]
              ["-h" "--help" "Show help options" :default false :flag true]
              ["--unescape" "Parse unescaping" :default false]
              ["-t" "--task"
               "Run a task, can be snip or analyze. Snip needs fieldspecs:
                SEG:field.comp.sub*?, eg MSH:4.0 or PID:3.0*"
               :default :snip :parse-fn keyword]])

(defn -main [& xs]
  (let [[options args banner] (try
                                (apply cli xs cmdopts)
                                (catch Exception e
                                  [{:help true}]))]
  (when (:help options)
    (println banner)
    (System/exit 0))
  (run-task (assoc options
              :args args))))
