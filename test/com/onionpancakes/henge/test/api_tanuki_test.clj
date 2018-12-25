(ns com.onionpancakes.henge.test.api-tanuki-test
  (:require [com.onionpancakes.henge.api.tanuki :as t]
            [clojure.test :refer [deftest are]]))

(deftest test-keyword->props-map
  (are [x y] (= (t/keyword->props-map x) y)
    :#foo            {:id "foo"}
    :.foo            {:className "foo"}
    :#foo.bar.baz    {:id        "foo"
                      :className "bar baz"}
    :##foo#          {:id "#foo#"}
    :foo             nil
    :foo.bar#baz.buz {:id        "baz"
                      :className "bar buz"}))
