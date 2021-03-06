# Deprecation Notice

Henge has been succeeded by [Veil](https://github.com/onionpancakes/veil).

# Henge

ClojureScript library for transforming vectors into `React.createElement` calls.

Henge is ClojureScript's version of JSX. It follows a few guiding principles which differentiates it from other vector templating libraries.

* Aggressively transform vectors into `React.createElement` calls at compile time.
* No runtime vector interpretation.
* Support the templating of `React.createElement` as transparently as possible with JSX semantics.
  * No mapping between camelCase and hypen-case props keys. Use camelCase just like normal React.
  * Support user defined components with a consistent syntax.
  * Support React features (e.g. `React.Fragment`, contexts, etc...) with as little library maintenance as possible.
* Extendable core api with good defaults.

### Install

Add Henge's git coordinate to your `deps.edn`.

```clojure
{:deps {com.onionpancakes/henge {:git/url <repo url>
                                 :sha     <commit sha>}}}
```

Additional requirements:

* Clojure 1.10.0 or later.
* `React` is in scope.
* `yarn` to run tests.

### Running the Tests

To run clojure tests:

```
$ clj -A:test:run-tests
```

To run JavaScript tests, first build the test components into a JavaScript module:

```
$ clj -A:build-testjs
```

Then run the tests from yarn:

```
$ yarn       # make sure to have npm deps
$ yarn test
```

## Usage

Require Henge's macros in your ClojureScript file.

```clojure
(require-macros '[com.onionpancakes.henge.core :as h])
```

The `compile` macro transforms all keyword vectors, vectors beginning with keywords, into `React.createElement` calls.

```clojure
(h/compile [:h1 nil "Hello World!"])

;; compiles into

(js/React.createElement "h1" nil "Hello World!")
```

Combine `compile` with `defn` to write simple functional components.

```clojure
(defn MyComponent [props]
  (h/compile
   [:div nil
    [:h1 nil "Hello World!"]
    [:p nil "Foo bar baz"]]))
```

Use `compile` where `React.createElement` would normally be needed.

```clojure
(js/ReactDOM.render (h/compile [:MyComponent])
                    (js/document.getElementById "app"))
```

### Tags

The first item in the vector (the tag) must be a keyword. Henge follows JSX's semantics[<sup>[Link]</sup>](https://reactjs.org/docs/jsx-in-depth.html#specifying-the-react-element-type) when determining the type of the element.

Lowercase tags are treated as DOM elements and Henge converts them into strings. 

```clojure
(h/compile [:div])

;; compiles into

(js/React.createElement "div")
```

Capitalized tags are treated as components. Henge converts them into symbols and preserves its namespace.

```clojure
(h/compile [:ns/Widget])

;; compiles into

(js/React.createElement ns/Widget)
```

Access `React.Fragment` and other React features through the component tag functionality.

```clojure
(defn MyFragment [props]
  (h/compile
   [:js/React.Fragment nil
    [:p nil "foo"]
    [:p nil "bar"]]))

(defn Widget [props]
  (h/compile
   [:div nil
    [:h1 nil "Hello World!"]
    [:MyFragment]]))
```

### Props

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

### Children

The remaining items in the vector are treated as the element's children.

Henge recursively compiles nested keyword vectors into React elements.

```clojure
(h/compile [:div nil [:p nil "foo"]])

;; compiles into

(js/React.createElement "div" nil
  (js/React.createElement "p" nil "foo"))
```

All other forms are left as they are.

```clojure
(h/compile [:ol nil
             (for [i (range 5)]
               [:li #js {:key i} i])])

;; compiles into

(js/React.createElement "ol" nil
  (for [i (range 5)]
    (js/React.createElement "li" #js {:key i} i))
```

## Extending

Henge is designed to be extendable. Serveral key transformation functions and values are dynamic and bindable. Write your own macro and rebind these vars.

```clojure
(defmacro mycompile [form]
  (binding [h/*create-element-fn* 'my-func]
    (h/compile* form)))

(mycompile [:div nil "foo"])

;; compiles into

(my-func "div" nil "foo")
```

The following vars are dynamic:

* `create-element-fn`: The function symbol for creating an element. Defaults to `js/React.createElement`.
* `transform-tag`: The function for transforming the tag into a type for `js/React.createElement`. Defaults to `h/tag->type`.
* `transform-props`: The function for transforming the element's props. Defaults to `identity`.
* `transform-element`: The function for transforming a conformed element vector into a conformed create-element call. The default function will invoke the three dynamic vars above to assemble a create-element call.

## The *Tanuki* Extension

Henge comes with an extended api called *tanuki* designed to make handling React props easier.

Require tanuki's `compile` macro from its namespace.

```clojure
(require-macros '[com.onionpancakes.henge.api.tanuki :as t])
```

### Keywords as props

Use keywords as css selector style props. Keywords are parsed into tokens. Tokens beginning with `#` are treated as ids. Tokens beginning with `.` are treated as classes. Tokens are processed into id and classes at **compile** time.

```clojure
(t/compile [:div :#app.foo.bar])

;; compiles into

(js/React.createElement "div" #js {:id "app"
                                   :className "foo bar"})
```

### Maps as props

Tanuki treats Clojure maps as a special extension of js object props. Namespaced keys are used as entrypoints for special behavior. Global, non-namespaced keys are treated as ordinary props with their values passed through untouched.

```clojure
(t/compile [:div {:id "app"}]) ; OK with t/compile
```

Note that for global keys, sub-maps are not handled differently. Send js objects to properties that expects them.

```clojure
(t/compile [:div {:style #js {:color "blue"}}]) ; OK

;; Style needs a js object, not a map.
(t/compile [:div {:style {:color "blue"}}])     ; Bad
```

#### `::t/classes` - Specify `className` with a collection

Use `::t/classes` to specify the element's classes with a collection. The value of `::t/classes` can be one of the following:

* A map of class keywords/strings to condition expressions. Keys whose values evaluate to true are joined into `className`.
* An arbitrary expression which evaluates to a seq of class keywords/strings. Values in the seq are joined into `className`.

The value of `::t/classes` is compiled into an expression which will process and join a `className` property string at **runtime**.

```clojure
;; Use a map with values, each a condition expression.
(t/compile [:div {::t/classes {:foo true
                               :bar (= 0 1)}}])

;; Or use a vector with keywords.
(t/compile [:div {::t/classes [:foo :bar]}])

;; Or an arbitrary expression, as long as it returns a
;; seq of keywords or strings.
(t/compile [:div {::t/classes (vector :foo :bar)}])
```

Currently, only `::t/classes` has special processing.

## Rules of Transformation

Henge recursively transforms all keywords vectors into `React.createElement` calls except for the following situations:

* Keyword vectors inside props.
* Keyword vectors as keys in maps.
* Keyword vectors with `::h/skip` metadata set to `true`.

```clojure
(h/compile
 [:button #js {:onClick #(->> [:click %] ; Safe! Not transformed!
                              (handle-click))}])

(h/compile
 (let [m {[:foo :bar]                 ; Map key not transformed!
          [:p nil "foobar"]}]         ; Map value is tranformed!
   [:div nil
     (get m ^::h/skip [:foo :bar])])) ; The argument not transformed!
```

If Henge is transforming a vector that should not be transformed, use adorn it with `::h/skip` metadata or process the vector outside of `compile`.

```clojure
(let [data [:not :an :element]]
  (h/compile
    [:div nil
      (for [d data]
        [:p nil (name d)])])])
```

## LICENSE

MIT
