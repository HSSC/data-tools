(ns ^{:doc "Helpers for extracting values from messages"}
  com.shayban.hl7v2.api
  (:refer-clojure :exclude [accessor])
  (:require com.shayban.hl7v2.parse
            [clojure.string :as str])
  (:import com.shayban.hl7v2.parse.RepeatingField))
 
;; TODO Use cgrand/regex to extract segments

(defn structure
  "Returns a seq of the segment headers."
  [m]
  (map :id (:segments m)))

(defn segment-name-pred
  "Makes a predicate that matches a segment type"
  [name]
  (fn [seg]
    (= (:id seg) name)))

(defn all-segments
  "Retrieves a seq of all segments matching segment-id"
  [msg segment-id]
  (filter (segment-name-pred segment-id) (:segments msg)))

(defn segment
  "Retrieves only the first matching segment-id."
  [msg segment-id]
  (-> msg
    (all-segments segment-id)
    first))

(defn field
  "Takes a segment and returns a field from it, or nil if it field
   doesn't exist. N.B. field-num is a one-based index.  If the field
   is repeating, this returns the first repetition only."
  [segment field-num]
  (let [idx (dec field-num)]
    (if (< idx (count (:fields segment)))
      (let [fld (nth (:fields segment) idx)]
        (if (:fields fld)
          (first (:fields fld))
          fld)))))

(defn repeating-field
  "Retrieves a seq of fields from a special repeating field from a segment.
   N.B., field-num is a one-based field index. If the field didn't repeat,
   returns a seq of the simple field."
  [segment field-num]
  (let [idx (dec field-num)]
    (if (< idx (count (:fields segment)))
      (let [fld (nth (:fields segment) idx)]
        (or (:fields fld) [fld])))))

(defn component
  "Indexes (0-based) into a field. Doesn't throw for out of bounds index."
  ([field idx]
    (cond
      (and (vector? field) (< idx (count field)))
      (nth field idx)
      (= idx 0)
      field))
  ([field idx sub-idx]
    (let [subcomps (component field idx)]
      (cond
        (vector? subcomps)
        (if (< sub-idx (count subcomps))
          (nth subcomps sub-idx))
        (= 0 sub-idx)
        subcomps
        :else
        (throw (Exception. "Unknown field structure"))))))

(defn field-seq
  "This takes a segment and unrolls into list of maps of the form
   {:segment \"PID\" :field 3 :value \"123\"}, handling repeating fields properly.
   You probably don't want to call this directly except for source feed
   analysis, as it is space inefficient."
   [seg]
   (let [seg-id (:id seg)
         {compound true simple false} (group-by #(instance? RepeatingField (second %))
                                                (map list (iterate inc 1) (:fields seg)))
         all-fields (concat simple
                      (for [[n {fld :fields}] compound] [n fld]))]
       (for [[field-num field-value] all-fields :when field-value]
         {:segment seg-id
          :field field-num
          :value field-value})))

(def spec-rgx #"(\p{Upper}{1,3}\d{0,3})[\.\:\-]([\d\.]+)(\*)?")

(defn- parse-components-spec [s]
  (if-let [[_ _ comp-part rep-flag] (re-find spec-rgx s)]
    (let [parts (str/split comp-part #"\.")]
      (if (> 4 (count parts) 0)
        (let [xs (try (into [] (map #(Long/parseLong %) parts))
                      (catch Exception e
                        (throw (ex-info "AField spec didn't parse" {:spec s}))))
              spec (zipmap [:field :component :subcomponent] xs)]
          (if (= rep-flag "*")
            (assoc spec :repeating true)
            spec))))
    (throw (ex-info "Field/component/sub spec didn't parse" {:spec s}))))

(defn- parse-segment-spec [s]
  (if-let [[_ seg] (re-find spec-rgx s)]
    (if (= 3 (count seg))
      {:segment seg}
      (throw (ex-info "Invalid segment specified")))))

(defn parse-field-spec
  "A spec is a segment followed by field (1-based index),
   and optional component/subcomponents (0-based index).
   An optional * at the end means to get all instances of repeating fields."
  [s]
  (merge (parse-segment-spec s)
         (parse-components-spec s)))

(defn accessor
  "Builds a fn that pulls info out of messages, using a map as a spec"
  [spec]
  (let [{compo :component
         sub  :subcomponent
         seg  :segment
         fld :field} spec
         seg-fn (fn [m] (all-segments m (:segment spec)))
         field-fn (if (:repeating spec)
                    (fn [segments] (mapcat #(repeating-field % fld) segments))
                    (fn [segments] (map #(field % fld) segments)))
         comp-fn (let [get-comp (cond
                                 (and compo sub)
                                 #(component % compo sub)
                                 compo
                                 #(component % compo))]
                   (if get-comp
                     (fn [fields] (map get-comp fields))
                     identity))]
    (comp vec comp-fn field-fn seg-fn)))

(defn info-at
  "Spec should be like \"MSH:4.3.2\" or \"PID:3*\" for repeating fields"
  [msg spec]
  (let [f (accessor (parse-field-spec spec))]
    (f msg)))
