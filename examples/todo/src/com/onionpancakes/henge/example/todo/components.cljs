(ns com.onionpancakes.henge.example.todo.components
  (:require-macros [com.onionpancakes.henge.api.tanuki :as t]))

(defn TodoInput [^js/Props props]
  (let [[st st!]          (js/React.useState "")
        handleNew         (.-handleNew props)
        handleClear       (.-handleClear props)
        ^js/Ref input-ref (js/React.createRef)]
    (t/compile
     [:div :.TodoInput
      [:div nil
       [:input {:value      st
                :ref        input-ref
                :onChange   (fn [^js/Event e]
                              (st! (.. e -target -value)))
                :onKeyPress (fn [^js/Event e]
                              (when (and (= (.-key e) "Enter")
                                         (not= st ""))
                                (handleNew st)
                                (st! "")))}]
       [:button {:onClick #(when (not= st "")
                             (handleNew st)
                             (st! "")
                             (.. input-ref -current focus))}
        "Add Task"]
       [:button {:onClick handleClear} "Clear"]]])))

(defn TodoTask [^js/Props props]
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

(defn reducer [state action]
  (case (:action action)
    :new   (conj state {:id    (random-uuid)
                        :text  (:text action)
                        :done? false})
    :done  (update-in state [(:idx action) :done?] not)
    :clear (into [] (remove :done?) state)))

(defn TodoApp [_]
  (let [[st dispatch] (js/React.useReducer reducer [])]
    (t/compile
     [:div :.TodoApp
      [:h1 nil "Todo"]
      [:div nil
       (if-not (empty? st)
         (for [[idx {:keys [id text done?]}] (map-indexed vector st)]
           [:TodoTask {:key        id
                       :text       text
                       :done       done?
                       :handleDone #(dispatch {:action :done :idx idx})}])
         [:i :.TodoApp-placeholder "What needs to be done?"])]
      [:TodoInput {:handleNew   #(dispatch {:action :new :text %})
                   :handleClear #(dispatch {:action :clear})}]])))
