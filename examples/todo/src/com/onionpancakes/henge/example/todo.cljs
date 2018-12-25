(ns com.onionpancakes.henge.example.todo
  (:require-macros [com.onionpancakes.henge.api.tanuki :as t]))

(defn TodoInput [props]
  (let [[st st!]      (js/React.useState "")
        handleNewTask (.-handleNewTask props)
        input-ref     (js/React.createRef)]
    (t/compile
     [:div nil
      [:input {:onChange #(st! (.. % -target -value))
               :value    st
               :ref      input-ref}]
      [:button {:onClick #(do
                            (handleNewTask st)
                            (st! "")
                            (.. input-ref -current focus))}
       "Add Task"]])))

(defn TodoApp [_]
  (let [[st st!]      (js/React.useState [])
        handleNewTask #(->> {:id (random-uuid) :task %}
                            (conj st)
                            (st!))]
    (t/compile
     [:div nil
      [:TodoInput {:handleNewTask handleNewTask}]
      [:ol nil
       (for [{:keys [id task]} st]
         [:li {:key id} task])]])))

(js/ReactDOM.render (t/compile [:TodoApp])
                    (js/document.getElementById "app"))
