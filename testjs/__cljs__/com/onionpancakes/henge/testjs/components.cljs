(ns com.onionpancakes.henge.testjs.components
  (:require-macros [com.onionpancakes.henge.api.tanuki :as t]
                   [com.onionpancakes.henge.core :as h])
  (:require [com.onionpancakes.henge.testjs.other-components :as oc]
            [cljs.nodejs]))

(set! *warn-on-infer* true)

(set! js/React (cljs.nodejs/require "react"))

(defn ^:export Widget [props]
  (t/compile
   [:div nil "foo"]))

(defn ^:export WidgetNested [props]
  (t/compile
   [:div nil [:Widget]]))

(defn ^:export WidgetKeyword [props]
  (t/compile
   [:div :#foo.bar.baz "buz"]))

(defn ^:export WidgetJSProps [props]
  (t/compile
   [:div #js {:id        "foo"
              :className "bar baz"} "buz"]))

(defn ^:export WidgetMapProps [props]
  (t/compile
   [:div {:id "foo"
          ::t/classes (identity [:bar :baz])} "buz"]))

(defn ^:export WidgetMapPropsMapClasses [props]
  (t/compile
   [:div {::t/classes {:foo true
                       :bar (constantly true)
                       :baz (= 0 1)}}]))

(defn ^:export WidgetFor [props]
  (t/compile
   [:div nil
    (for [i (range 3)]
      [:p #js {:key i} i])]))

(defn ^:export WidgetFragment [props]
  (t/compile
   [:js/React.Fragment nil
    [:div nil "foo"]
    [:div nil "bar"]]))

(defn ^:export WidgetOther [props]
  (t/compile
   [:div nil [::oc/Other]]))

(defn ^:export WidgetSkip [props]
  (t/compile
   (let [m {[:a :b] [:p nil "Rendered"]}]
     [:div nil
      (get m ^::h/skip [:a :b])])))

