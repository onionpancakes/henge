(ns com.onionpancakes.henge.example.todo.app
  (:require [com.onionpancakes.henge.example.todo.components :as c])
  (:require-macros [com.onionpancakes.henge.api.tanuki :as t]))

(set! *warn-on-infer* true)

(js/ReactDOM.render (t/compile [::c/TodoApp])
                    (js/document.getElementById "app"))
