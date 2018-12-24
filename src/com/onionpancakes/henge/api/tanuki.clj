(ns com.onionpancakes.henge.api.tanuki
  (:refer-clojure :exclude [compile])
  (:require [com.onionpancakes.henge.core :as h]
            [clojure.spec.alpha :as spec]
            [clojure.string])
  (:import [cljs.tagged_literals JSValue]))

;; Keyword props

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

(defn- tokens-val [[_ v]]
  (apply str (::tokens v)))

(defn tokens->props-map
  [tokens]
  (let [ct (spec/conform ::tokens tokens)
        {::keys [id class other]} (group-by key ct)]
    (cond-> nil
      id    (assoc :id (tokens-val (first id)))
      class (assoc :className (->> (map tokens-val class)
                                   (clojure.string/join " "))))))

(defn keyword->props-map [k]
  (->> (name k)
       (re-seq re-token)
       (tokens->props-map)))

;; Map props

(defn map-classes-form [m]
  `(-> (eduction (filter val)
                 (map (comp name key))
                 ~m)
       (into-array)
       (.join " ")))

(defn default-classes-form [obj]
  `(-> (eduction (map name) ~obj)
       (into-array)
       (.join " ")))

(defprotocol Classes
  (transform-classes* [this]))

(extend-protocol Classes
  clojure.lang.PersistentArrayMap
  (transform-classes* [this]
    (map-classes-form this))
  clojure.lang.PersistentHashMap
  (transform-classes* [this]
    (map-classes-form this))
  Object
  (transform-classes* [this]
    (default-classes-form this))
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

;; Compile API

(defprotocol Props
  (transform-props* [this]))

(extend-protocol Props
  clojure.lang.Keyword
  (transform-props* [this]
    (->> (keyword->props-map this)
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
