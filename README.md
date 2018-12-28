# Henge

ClojureScript library for transforming vectors into `React.createElement` calls.

## Install

Add Henge's git coordinate to your `deps.edn`.

```clojure
{:deps {com.onionpancakes/henge {:git/url <repo url>
                                 :sha     <commit sha>}}}
```

Additional requirements

* Henge requires Clojure 1.10.0 or later.
* Henge assumes `React` is in scope.

# Usage

Require Henge's macros in your ClojureScript file.

```clojure
(require-macros '[com.onionpancakes.henge.core :as h])
```

The `compile` macro will transform all vectors beginning with keywords into `React.createElement` calls.

```clojure
(h/compile [:h1 nil "Hello World!"])

;; compiles into

(js/React.createElement "h1" nil "Hello World!")
```

## Tags

The first item in the vector (the tag) must be a keyword. Henge follows JSX's semantics[<sup>[Link]</sup>](https://reactjs.org/docs/jsx-in-depth.html#specifying-the-react-element-type) when determining the type of the element.

Lowercase tags are treated as DOM elements and Henge will convert them into strings. 

```clojure
(h/compile [:div])

;; compiles into

(js/React.createElement "div")
```

Capitalized tags are treated as components and Henge will convert them into namespace preserving into symbols.

```clojure
(h/compile [:ns/Widget])

;; compiles into

(js/React.createElement ns/Widget)
```

## Props

The second item in the vector (the props) must be a Javascript object or nil. By default, it is passed untransformed to `React.createElement`. Keep in mind that React only understands Javascript objects for props.

```clojure
(h/compile [:div #js {:id "foo"}]) ; OK

(h/compile [:div {:id "foo"}])     ; Bad, map is not js object!
```

If the element has children, declaring the props is mandatory since the second item in the vector always treated as props.

```clojure
(h/compile [:div nil "foo"]) ; OK

(h/compile [:div "foo"])     ; Bad, strings are not props!
```

Henge does not transform prop keys. React expects **camelCase** keys. Do not use hypen-separated keys.

```clojure
(h/compile [:button #js {:onClick #(handle %}])   ; OK

(h/compile [:button #js {:on-click #(handle %)}]) ; Bad
```

## Children

The remaining items in the vector will be treated as the element's children.

Henge will compile nested keyword vectors into React elements.

```clojure
(h/compile [:div nil [:p nil "foo"]])

;; compiles into

(js/React.createElement "div" nil
  (js/React.createElement "p" nil "foo"))
```

All other forms will be ignored.

```clojure
(h/compile [:ol nil
             (for [i (range 5)]
               [:li nil i])])

;; compiles into

(js/React.createElement "ol" nil
  (for [i (range 5)]
    (js/React.createElement "li" nil i))
```

# Extending

Henge is designed to be extendable. Serveral key transformation functions and values are dynamic and bindable. Write your own macro and rebind these vars.

```clojure
(defmacro mycompile [form]
  (binding [h/*create-element-fn* 'my-func]
    (h/compile* form)))

(mycompile [:div nil "foo"])

;; compiles into

(my-func "div" nil "foo")
```

Read the source to find out whats is dynamically bindable.

# Tanuki Extension

Henge comes with an extended api called *tanuki* which makes handling React props easier.

```clojure
(require-macros '[com.onionpancakes.henge.api.tanuki :as t])
```

## Keywords as props

Use keywords as css selector style props. Keywords are parsed into tokens. Tokens beginning with `#` are treated as ids. Tokens beginning with `.` are treated as classes. Tokens are processed into id and classes at **compile** time.

```clojure
(t/compile [:div :#app.foo.bar])

;; compiles into

(js/React.createElement "div" #js {:id "app"
                                   :className "foo bar"})
```

## Maps as props

Use ClojureScript maps as props. Certain namespaced keys will process their values differently. Global keys are treated normally.

```clojure
(t/compile [:div {:id "app"}]) ; OK
```

Note that for global keys, sub-maps are not handled differently. Send js objects to properties that expects them.

```clojure
(t/compile [:div {:style #js {:color "blue"}}]) ; OK

;; Style needs a js object, not a map.
(t/compile [:div {:style {:color "blue"}}])     ; Bad
```

Use `::t/classes` to specify the element's classes with a collection. Classes will be processed and joined at **runtime**.

```clojure
;; Use a vector with keywords.
(t/compile [:div {::t/classes [:foo :bar]}])

;; Or a map with values, each as a condition expression.
(t/compile [:div {::t/classes {:foo true
                               :bar (= 0 1)}}])

;; Or an arbitrary expression, as long as it returns a
;; seq of keywords or strings.
(t/compile [:div {::t/classes (vector :foo :bar)}])
```

Currently, only `::t/classes` has special processing.

# Rules of Transformation

Henge recursively transforms all keywords vectors into `React.createElement` calls except for the following situations:

* Keyword vectors inside props.
* Keyword vectors as keys in maps.
* Keyword vectors with `::h/skip` metadata set to `true`.

```clojure
(h/compile
 [:button #js {:onClick #(->> [:click %] ; Safe! Not transformed!
                              (handle-click))}])

(h/compile
 (let [m {[:foo :bar] "baz"}]         ; Key not transformed!
   [:div nil
     (get m ^::h/skip [:foo :bar])])) ; Arg not transformed!
```

# LICENSE

MIT
