(ns com.onionpancakes.henge.test.core-test
  (:require [com.onionpancakes.henge.core :as h]
            [clojure.test :refer [deftest is are]]
            [clojure.spec.alpha :as spec]))

(def cfn h/*create-element-fn*)

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
   :foo            [::h/other :foo]
   [:foo nil
    [:bar]]        [::h/element
                    #::h{:tag      :foo
                         :props    nil
                         :children [[::h/element
                                     #::h{:tag :bar}]]}]
   [:foo nil
    '([:bar])]     [::h/element
                    #::h{:tag      :foo
                         :props    nil
                         :children [[::h/coll
                                     '([::h/element
                                        #::h{:tag :bar}])]]}]
   `(~cfn nil)     [::h/create-element
                    #::h{:fn   cfn
                         :type nil}]})

(deftest test-conform
  (doseq [[form conformed] form->conformed]
    (is (= (spec/conform ::h/form form) conformed))))

(deftest test-unform
  (doseq [[form conformed] form->conformed]
    (is (= (spec/unform ::h/form conformed) form))))

(deftest test-unform-element-vector
  (are [x] (vector? (spec/unform ::h/form x))
    [::h/element #::h{:tag :foo}]))

(deftest test-compile*
  (are [x y] (= (h/compile* x) y)
    [:foo]               `(~cfn "foo")
    [:a/foo]             `(~cfn "foo")
    [:Foo]               `(~cfn ~'Foo)
    [:a/Foo]             `(~cfn ~'a/Foo)
    [:foo nil]           `(~cfn "foo" nil)
    [:foo nil :bar]      `(~cfn "foo" nil :bar)
    ^::h/skip [:foo]     [:foo]
    '(foo [:foo])        `(~'foo (~cfn "foo"))
    '(let [c [:foo]] c)  `(~'let [~'c (~cfn "foo")] ~'c)
    [:foo {:bar [:baz]}] `(~cfn "foo" {:bar [:baz]})
    {[:foo] [:bar]}      {[:foo] `(~cfn "bar")}))

