(ns com.onionpancakes.henge.test.core-test
  (:require [com.onionpancakes.henge.core :as h]
            [clojure.test :refer [deftest is are]]
            [clojure.spec.alpha :as spec]))

(deftest test-unform-element
  (are [x y] (let [res (spec/unform ::h/element-form x)]
               (and (= res y) (vector? res)))
    {::h/tag   :foo
     ::h/props nil}                [:foo nil]
    {::h/tag   :foo
     ::h/props :bar}               [:foo :bar]
    {::h/tag      :foo
     ::h/props    :bar
     ::h/children [[::h/other :baz]]} [:foo :bar :baz]))

(deftest test-conform-unform-identical
  (are [x] (->> (spec/conform ::h/form x)
                (spec/unform ::h/form)
                (= x))
    nil
    :foo
    '()
    []
    {}
    #{}
    {:foo :bar}
    [:foo nil]
    '(foo)
    '(foo [:foo {} '(bar [:bar nil #{}])])))

