(ns com.onionpancakes.henge.testjs.components
  (:require-macros [com.onionpancakes.henge.api.tanuki :as t])
  (:require [cljs.nodejs]))

(set! js/React (cljs.nodejs/require "react"))

(defn ^:export Widget [props]
  (t/compile
   [:div {::t/classes {:foo true}}
    "foo"]))
