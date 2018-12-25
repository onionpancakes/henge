(ns com.onionpancakes.henge.example.todo
  (:require-macros [com.onionpancakes.henge.api.tanuki :as t]))

(defn TodoInput [props]
  (let [[st st!]      (js/React.useState "")
        handleNewTask (.-handleNewTask props)
        handleClear   (.-handleClear props)
        input-ref     (js/React.createRef)]
    (t/compile
     [:div :.TodoInput
      [:div nil
       [:input {:value    st
                :ref      input-ref
                :onChange #(st! (.. % -target -value))
                :onKeyPress #(when (= (.-key %) "Enter")
                               (handleNewTask st)
                               (st! ""))}]
       [:button {:onClick #(when (not (= st ""))
                             (handleNewTask st)
                             (st! "")
                             (.. input-ref -current focus))}
        "Add Task"]
       [:button {:onClick handleClear} "Clear"]]])))

(defn TodoTask [props]
  (let [text       (.-text props)
        done?      (.-done props)
        handleDone (.-handleDone props)]
    (t/compile
     [:div {::t/classes {:TodoTask true
                         :done     done?}}
      [:label nil
       [:input {:type     "checkbox"
                :checked  done?
                :onChange handleDone}]
       text]])))

(defn TodoApp [_]
  (let [[st st!]      (js/React.useState [])
        handleNewTask #(->> {:id (random-uuid) :text % :done? false}
                            (conj st)
                            (st!))
        handleClear   #(->> (remove :done? st)
                            (into [])
                            (st!))]
    (t/compile
     [:div :.TodoApp
      [:h1 nil "Todo"]
      [:div nil
       (if (not (empty? st))
         (for [[idx {:keys [id text done?]}] (map-indexed vector st)]
           [:TodoTask {:key        id
                       :text       text
                       :done       done?
                       :handleDone #(st! (update-in st [idx :done?] not))}])
         [:i :.TodoApp-placeholder
          "What needs to be done?"])]
      [:TodoInput {:handleNewTask handleNewTask
                   :handleClear   handleClear}]])))

(js/ReactDOM.render (t/compile [:TodoApp])
                    (js/document.getElementById "app"))
