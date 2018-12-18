(ns com.onionpancakes.henge.core
  (:refer-clojure :exclude [compile])
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [clojure.walk]))

(spec/def ::element-form
  (spec/and
   (spec/coll-of any? :kind vector? :into []
                 :min-count 1 :gen #(spec/gen vector?))
   (complement map-entry?)
   (comp not ::skip meta)
   (spec/cat ::tag keyword?
             ::props (spec/? any?)
             ::children (spec/* ::form))))

(def ^:dynamic *create-element-fn*
  `js/React.createElement)

(defn- create-element-form-gen []
  (->> (spec/gen list?)
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

(defn component-tag?
  [k]
  (Character/isUpperCase (first (name k))))

(defn keyword->symbol
  [k]
  (symbol (namespace k) (name k)))

(defn tag->type
  [k]
  (if (component-tag? k)
    (keyword->symbol k)
    (name k)))

(defmulti process-node first)

(defmethod process-node ::element
  [[_ m]]
  [::create-element (merge m {::fn   *create-element-fn*
                              ::type (tag->type (::tag m))})])

(defmethod process-node :default
  [node]
  node)

(defn node?
  [x]
  (and (vector? x) (= (count x) 2)))

(defn process
  [x]
  (if (node? x)
    (process-node x)
    x))

(defn compile*
  [form]
  (->> (spec/conform ::form form)
       (clojure.walk/postwalk process)
       (spec/unform ::form)))

(defmacro compile
  [form]
  (compile* form))

