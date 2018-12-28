(ns com.onionpancakes.henge.core
  (:refer-clojure :exclude [compile])
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [clojure.walk]
            [clojure.string]))

(def ^:dynamic *create-element-fn*
  `js/React.createElement)

(spec/def ::element-form
  (spec/and
   (spec/coll-of any? :kind vector? :into []
                 :min-count 1 :gen #(spec/gen vector?))
   (complement map-entry?)
   (comp not ::skip meta)
   (spec/cat ::tag keyword?
             ::props (spec/? any?)
             ::children (spec/* ::form))))

(defn- create-element-form-gen []
  (->> (spec/gen seq?)
       (gen/fmap (partial cons *create-element-fn*))))

(spec/def ::create-element-form
  (spec/and
   (spec/coll-of any? :kind seq? :min-count 2
                 :gen create-element-form-gen)
   (spec/cat ::fn #{*create-element-fn*}
             ::type any?
             ::props (spec/? any?)
             ::children (spec/* ::form))))

(spec/def ::form
  (spec/or ::element ::element-form
           ::create-element ::create-element-form
           ::map (spec/map-of any? ::form)
           ::coll (spec/coll-of ::form)
           ::other any?))

(defn component-tag? [k]
  (let [s (name k)]
    (or (Character/isUpperCase (first s))
        (clojure.string/includes? s "."))))

(defn tag->type [k]
  (if (component-tag? k) (symbol k) (name k)))

(def ^:dynamic transform-tag tag->type)

(def ^:dynamic transform-props identity)

(defn ^:dynamic transform-element
  [m]
  (cond-> m
    true (assoc ::fn *create-element-fn*)
    true (assoc ::type (transform-tag (::tag m)))
    (contains? m ::props) (update ::props transform-props)))

(defmulti process-node first)

(defmethod process-node ::element
  [[_ m]]
  [::create-element (transform-element m)])

(defmethod process-node :default
  [node]
  node)

(defn- process [x]
  (if (map-entry? x) (process-node x) x))

(defn compile*
  [form]
  (->> (spec/conform ::form form)
       (clojure.walk/postwalk process)
       (spec/unform ::form)))

(defmacro compile
  [form]
  (compile* form))
