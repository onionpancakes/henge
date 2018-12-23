(ns com.onionpancakes.henge.api.tanuki
  (:refer-clojure :exclude [compile])
  (:require [com.onionpancakes.henge.core :as h]
            [clojure.spec.alpha :as spec]
            [cljs.tagged-literals :refer [read-js]]
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

(defprotocol Props
  (transform-props* [this]))

(extend-protocol Props
  clojure.lang.Keyword
  (transform-props* [this]
    (->> (name this)
         (re-seq re-token)
         (tokens->props-map)
         (JSValue.)))
  Object
  (transform-props* [this] this))

(defn compile*
  [form]
  (binding [h/transform-props transform-props*]
    (h/compile* form)))

(defmacro compile
  [form]
  (compile* form))
