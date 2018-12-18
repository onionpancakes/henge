(ns com.onionpancakes.henge.test.core-test
  (:require [com.onionpancakes.henge.core :as h]
            [clojure.test :refer [deftest is are]]
            [clojure.spec.alpha :as spec]))

(def form->conformed
  {[:foo]          [::h/element #::h{:tag :foo}]
   [:foo nil]      [::h/element #::h{:tag   :foo
                                     :props nil}]
   [:foo nil :bar] [::h/element #::h{:tag      :foo
                                     :props    nil
                                     :children [[::h/other :bar]]}]
   '([:foo])       [::h/coll
                    '([::h/element #::h{:tag :foo}])]
   #{:foo}         [::h/coll #{[::h/other :foo]}]
   {:foo [:foo]}   [::h/map
                    {:foo '[::h/element #::h{:tag :foo}]}]
   :foo            [::h/other :foo]})

(deftest test-conform
  (doseq [[form conformed] form->conformed]
    (is (= (spec/conform ::h/form form) conformed))))

(deftest test-unform
  (doseq [[form conformed] form->conformed]
    (is (= (spec/unform ::h/form conformed) form))))

(deftest test-unform-element-vector
  (are [x] (vector? (spec/unform ::h/form x))
    [::h/element #::h{:tag :foo}]))

