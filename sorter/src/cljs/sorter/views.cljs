(ns sorter.views
    (:require [re-frame.core :as re-frame]
              [re-com.core :as re-com]))


;; home

(defn home-title []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [re-com/title
       :label (str "Hello from " @name ". This is the Home Page.")
       :level :level1])))

(defn link-to-run-page []
  [re-com/hyperlink-href
   :label "start a new run"
   :href "#/run/new"])

(defn link-to-continue-page []
  (let [run-in-progress? (re-frame/subscribe [:run-in-progress?])]
    (fn []
      (when @run-in-progress?
        [re-com/hyperlink-href
         :label "continue current run"
         :href "#/run"]))))

(defn home-panel []
  [re-com/v-box
   :gap "1em"
   :children [[home-title] [link-to-run-page] [link-to-continue-page]]])


;; run

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "back"
   :href "#/"])

(defn run-panel []
  (let [sort-couple (re-frame/subscribe [:couple-to-sort])]
    (fn []
      [re-com/v-box
       :gap "1em"
       :children [[re-com/h-box
                   :gap "1em"
                   :children [[:input {:type "button" :value (or (first @sort-couple) "") :on-click #(re-frame/dispatch [:set-sort-result :left])}]
                              [:input {:type "button" :value (or (second @sort-couple) "") :on-click #(re-frame/dispatch [:set-sort-result :right])}]]]
                  [link-to-home-page]]])))

;; review

(defn list-ranked-members []
  (let [finished-sort (re-frame/subscribe [:finished-sort])]
    (fn []
      [:ol
       (for [item @finished-sort]
         [:li item])])))
         

(defn review-panel []
  [re-com/v-box
   :gap "1em"
   :children [[list-ranked-members]
              [link-to-run-page]
              [link-to-home-page]]])
                  

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :run-panel [run-panel]
    :review-panel [review-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [re-com/v-box
       :height "100%"
       :children [[panels @active-panel]]])))
