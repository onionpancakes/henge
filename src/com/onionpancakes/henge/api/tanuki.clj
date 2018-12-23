(ns com.onionpancakes.henge.api.tanuki
  (:refer-clojure :exclude [compile])
  (:require [com.onionpancakes.henge.core :as h]
            [clojure.spec.alpha :as spec]
            [clojure.string])
  (:import [cljs.tagged_literals JSValue]))

;; Keyword

(def re-token
  #"[#.]|[^#.]+")

(spec/def ::id
  (spec/cat
   ::delimiter #{"#"}
   ::tokens (spec/* (complement #{"."}))))

(spec/def ::class
  (spec/cat
   ::delimiter #{"."}
   ::tokens (spec/* (complement #{"#" "."}))))

(spec/def ::tokens
  (spec/* (spec/alt ::id ::id
                    ::class ::class
                    ::other any?)))

(defn- conformed-token-val [[_ v]]
  (apply str (::tokens v)))

(defn tokens->props-map
  [tokens]
  (let [{::keys [id class other]} (->> (spec/conform ::tokens tokens)
                                       (group-by key))]
    (cond-> nil
      id    (assoc :id (conformed-token-val (first id)))
      class (assoc :className (->> (map conformed-token-val class)
                                   (clojure.string/join " "))))))

;; Maps

(defprotocol Classes
  (transform-classes* [this]))

(extend-protocol Classes
  clojure.lang.PersistentArrayMap
  (transform-classes* [this]
    `(-> (eduction (filter val)
                   (map (comp name key))
                   ~this)
         (into-array)
         (.join " ")))
  clojure.lang.PersistentHashMap
  (transform-classes* [this]
    `(-> (eduction (filter val)
                   (map (comp name key))
                   ~this)
         (into-array)
         (.join " ")))
  Object
  (transform-classes* [this]
    `(-> (eduction (map name) ~this)
         (into-array)
         (.join " ")))
  nil
  (transform-classes* [this] nil))

(defmulti props-map-entry key)

(defmethod props-map-entry ::classes
  [[_ v]]
  [:className (transform-classes* v)])

(defmethod props-map-entry :default
  [entry]
  entry)

(defn map->props-map [m]
  (into {} (map props-map-entry) m))

;;

(defprotocol Props
  (transform-props* [this]))

(extend-protocol Props
  clojure.lang.Keyword
  (transform-props* [this]
    (->> (name this)
         (re-seq re-token)
         (tokens->props-map)
         (JSValue.)))
  clojure.lang.PersistentArrayMap
  (transform-props* [this]
    (->> (map->props-map this)
         (JSValue.)))
  clojure.lang.PersistentHashMap
  (transform-props* [this]
    (->> (map->props-map this)
         (JSValue.)))
  Object
  (transform-props* [this] this)
  nil
  (transform-props* [this] nil))

(defn compile*
  [form]
  (binding [h/transform-props transform-props*]
    (h/compile* form)))

(defmacro compile
  [form]
  (compile* form))
