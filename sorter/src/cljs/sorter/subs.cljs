(ns sorter.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 :couple-to-sort
 (fn [db _]
   [(first (get-in db [:sort :merge :a]))
    (first (get-in db [:sort :merge :b]))]))