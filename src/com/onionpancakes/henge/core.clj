(ns com.onionpancakes.henge.core
  (:require [clojure.spec.alpha :as spec]
            [clojure.walk]))

(spec/def ::element-seq
  (spec/cat ::tag keyword?
            ::props (spec/? any?)
            ::children (spec/* ::form)))

(spec/def ::element-form
  (spec/and (spec/coll-of any? :kind vector? :into []
                          :min-count 1 :gen #(spec/gen vector?))
            (complement map-entry?)
            ::element-seq))

(spec/def ::form
  (spec/or ::element ::element-form
           ::map (spec/map-of any? ::form)
           ::coll (spec/coll-of ::form)
           ::other any?))

