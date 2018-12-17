(ns com.onionpancakes.henge.test.core-prop-test
  (:require [com.onionpancakes.henge.core :as h]
            [clojure.test :refer [deftest is are]]
            [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as stest]))

(alias 'stc 'clojure.spec.test.check)

(def ^:dynamic *num-tests* 100)

(def check-opts
  {::stc/opts {:num-tests *num-tests*}})

(defn check
  [func]
  (-> (stest/check func check-opts)
      (stest/summarize-results)
      (as-> x
        (= (:total x) (:check-passed x)))))

;; Properties

(defn property-round-trip
  [form]
  (try
    (->> (spec/conform ::h/form form)
         (spec/unform ::h/form))
    (catch Exception e
      (throw (ex-info "Error in round-trip!" {:form form} e)))))

(spec/fdef property-round-trip
  :args (spec/cat :form any?)
  :fn (fn [{{form :form} :args ret :ret}]
        (= form ret)))

(deftest test-property-round-trip
  (is (check `property-round-trip)))

