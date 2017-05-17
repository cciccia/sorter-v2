(ns sorter.db
  (:require [cljs.reader]
            [cljs.spec :as s]
            [re-frame.core :as re-frame]))

(s/def ::name string?)
(s/def ::report-id int)
(s/def ::id int)
(s/def ::a (s/coll-of ::id))
(s/def ::b (s/coll-of ::id))
(s/def ::c (s/coll-of ::id))
(s/def ::merge (s/keys ::req-un [::a ::b ::c]))
(s/def ::pieces (s/coll-of (s/coll-of ::id)))
(s/def ::sort (s/keys :req-un [::pieces ::merge]))
(s/def ::db (s/keys :req-un [::sort ::name]
                    :opt-un [::report-id]))

(def sort-bootstrap {:merge {:a []
                             :b []
                             :c []}
                     :pieces [[8] [2] [5] [4] [1] [7] [3] [6]]})
                         

(def default-db
  {:name "Bonesaus"
   :sort sort-bootstrap})

(def ls-key "merge-reframe")                          ;; localstore key
(defn merge->local-store
  "Puts merge state into localStorage"
  [merge]
  (.setItem js/localStorage ls-key (str merge)))     ;; sorted-map writen as an EDN map

(re-frame/reg-cofx
 :local-store-merge
 (fn [cofx _]
      "Read in merge from localstore, and process into a map we can merge into app-db."
      (assoc cofx :local-store-merge
             (into (sorted-map)
                   (some->> (.getItem js/localStorage ls-key)
                            (cljs.reader/read-string))))))
