(ns sorter.localstorage
  (:require [tailrecursion.cljson :as json]))

(defn set-item!
  "Set `key' in browser's localStorage to `val`."
  [key val]
  (->> (json/clj->cljson val)
       (.setItem (.-localStorage js/window) key)))

(defn get-item
  "Returns value of `key' from browser's localStorage."
  [key]
  (println (-> (.getItem (.-localStorage js/window) key)
               (json/cljson->clj)))
  (-> (.getItem (.-localStorage js/window) key)
      (json/cljson->clj)))

(defn remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) key))