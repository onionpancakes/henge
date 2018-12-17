(ns com.onionpancakes.henge.test.core-test
  (:require [com.onionpancakes.henge.core :as h]
            [clojure.test :refer [deftest is are]]
            [clojure.spec.alpha :as spec]))

(deftest test-unform-element
  (are [x y] (let [res (spec/unform ::h/element-form x)]
               (and (= res y) (vector? res)))
    {:tag   :foo
     :props nil}                [:foo nil]
    {:tag   :foo
     :props :bar}               [:foo :bar]
    {:tag      :foo
     :props    :bar
     :children [[:other :baz]]} [:foo :bar :baz]))

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

