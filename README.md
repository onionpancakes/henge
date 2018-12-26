# Henge

ClojureScript library for transforming vectors into `React.createElement` calls.

## Install

Add Henge's git coordinate to your `deps.edn`.

```
{:deps {com.onionpancakes/henge {:git/url <repo url>
                                 :sha     <commit sha>}}}
```

Additional requirements

* Henge requires Clojure 1.10.0 or later.
* Henge assumes `React` is in scope.

# Usage

Require Henge's macros in your ClojureScript file.

```
(require-macros '[com.onionpancakes.henge.core :as h])
```

The `compile` macro will transform all vectors beginning with keywords into `React.createElement` calls.

```
(h/compile [:h1 nil "Hello World!"])

;; expands into this

(js/React.createElement "h1" nil "Hello World!")
```

## Tags

The first item in the vector (the tag) must be a keyword. Henge follows JSX's semantics[<sup>[Link]</sup>](https://reactjs.org/docs/jsx-in-depth.html#specifying-the-react-element-type) when determining the type of the element.

Lowercase tags are treated as DOM elements and Henge will convert them into strings. 

```
(h/compile [:div])

;; Since :div lower case, the tag becomes a string.

(js/React.createElement "div")
```

Capitalized tags are treated as components and Henge will convert them into namespace preserving into symbols.

```
(h/compile [:ns/Widget])

;; Since :ns/Widget is capitalized, the tag becomes a symbol.

(js/React.createElement ns/Widget)
```

## Props

The second item in the vector (the props) must be a Javascript object or nil. By default, it is passed untransformed to `React.createElement`. Keep in mind that React only understands Javascript objects for props.

```
(h/compile [:div #js {:id "foo}]) ; OK

(h/compile [:div {:id "foo"}])    ; Bad, map is not js object!
```

If the element has children, declaring the props is mandatory since the second item in the vector always treated as props.

```
(h/compile [:div nil "foo"]) ; OK

(h/compile [:div "foo"])     ; Bad, string are not props!
```

## Children

The remaining items in the vector will be treated as the element's children.

Henge will compile nested keyword vectors into React elements.

```
(h/compile [:div nil [:p nil "foo"]])

;; becomes

(js/React.createElement "div" nil
  (js/React.createElement "p" nil "foo"))
```

All other forms will be ignored.

```
(h/compile [:ol nil
             (for [i (range 5)]
               [:li nil i])])

;; becomes

(js/React.createElement "ol" nil
  (for [i (range 5)]
    (js/React.createElement "li" nil i))
```

# Extending

Henge is designed to be extendable. Serveral key transformation functions and values are dynamic and bindable. Write your own macro and rebind these vars.

```
(defmacro mycompile [form]
  (binding [h/*create-element-fn* 'my-func]
    (h/compile* form)))

(mycompile [:div nil "foo"])

;; becomes

(my-func "div" nil "foo")
```

Read the source to find out whats is dynamically bindable.

# Tanuki Extension

Henge comes with an extended api called *tanuki* which makes handling React props easier.

```
(require-macros '[com.onionpancakes.henge.api.tanuki :as t])
```

## Keywords as props

Use keywords as css selector style props. Keywords are parsed into tokens. Tokens beginning with `#` are treated as ids. Tokens beginning with `.` are treated as classes.

```
(t/compile [:div :#app.foo.bar])

;; becomes

(js/React.createElement "div" #js {:id "app"
                                   :className "foo bar"})
```

## Maps as props

Use ClojureScript maps as props. Certain namespaced keys will process their values differently. Global keys are treated normally.

```
(t/compile [:div {:id "app"}]) ; OK
```

Note that for global keys, sub-maps are not handled differently.

```
(t/compile [:div {:style #js {:color "blue"}}]) ; OK

;; Style needs a js object, not a map.
(t/compile [:div {:style {:color "blue"}}])     ; Bad
```

Use `::t/classes` to specify the element's classes with a collection.

```
;; Use a vector with keywords.
(t/compile [:div {::t/classes [:foo :bar]}])

;; Or a map with values as condition expressions.
(t/compile [:div {::t/classes {:foo true
                               :bar (= 0 1)}}])
```

Currently, only `::t/classes` has special processing. 

## LICENSE

MIT
