(ns com.onionpancakes.henge.testjs.other-components
  (:require-macros [com.onionpancakes.henge.api.tanuki :as t]))

(defn ^:export Other [props]
  (t/compile
   [:div nil "other"]))
