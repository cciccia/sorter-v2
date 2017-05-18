(ns sorter.events
  (:require-macros [sorter.env :refer [cljs-env]])
  (:require [re-frame.core :as re-frame]
            [sorter.db :as db]
            [sorter.localstorage :as l]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx])
  (:import goog.History))

; effects
(re-frame/reg-fx
 :location
 (fn [[route]]
   "Invoke a secretary route change programmatically"
   (set! (.-location js/window) route)))

(re-frame/reg-fx
 :local-store
 (fn [[key val]]
   (if (nil? val)
     (l/remove-item! key)
     (l/set-item! key val))))

; coeffects 
(re-frame/reg-cofx
   :local-store 
   (fn [coeffects local-store-key]
      (assoc coeffects 
             :local-store
             (l/get-item local-store-key))))

; interceptors
(def store-sort-locally
  (re-frame/->interceptor
   :id :store-sort-locally
   :after (fn [context]
            (assoc-in context
                      [:effects :local-store]
                      ["sort" (get-in context [:effects :db :sort])]))))

; handlers
(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 :set-report-id
 (fn [db [_ id]]
   (assoc db :report-id id)))

(defn- take-pieces-for-merge
  [pieces]
  (let [zeroed-merge {:a [] :b [] :c []}]
    (if (< 1 (count pieces))
      (let [new-merge (into [] (take 2 pieces))
            new-pieces (into [] (drop 2 pieces))]
        {:merge {:a (first new-merge)
                 :b (second new-merge)
                 :c []}
         :pieces new-pieces})
      {:pieces pieces
       :merge zeroed-merge})))

(re-frame/reg-event-fx
 :reset-sort
 [store-sort-locally]
 (fn [{:keys [db]} _]
   (let [pieces (get-in db [:sort :pieces])]
     (if (>= 1 (count pieces))
       {:location ["/#/review"]
        :db (assoc db :run-in-progress? true)}
       {:db (assoc db
                   :sort (take-pieces-for-merge pieces)
                   :run-in-progress? true)}))))

(re-frame/reg-event-fx
 :load-sort
 [(re-frame/inject-cofx :local-store "sort")]
 (fn [{:keys [db local-store]} _]
   {:db (assoc db :sort local-store)}))
    
(re-frame/reg-event-fx
 :set-sort-result
 [store-sort-locally]
 (fn [{:keys [db]} [_ result]]
   "Merge the stuff"
   (let [merge (get-in db [:sort :merge])
         pieces (get-in db [:sort :pieces])
         post-decision (if (= result :left)
                         {:c (conj (:c merge) (first (:a merge)))
                          :a (into [] (rest (:a merge)))
                          :b (:b merge)}
                         {:c (conj (:c merge) (first (:b merge)))
                          :b (into [] (rest (:b merge)))
                          :a (:a merge)})]
     (if (or (empty? (:a post-decision))
             (empty? (:b post-decision)))
       (let [appended-pieces (->> (into (:c post-decision) (if (empty? (:a post-decision))
                                                             (:b post-decision)
                                                             (:a post-decision)))
                                  (conj pieces))
             new-fx {:db (assoc db :sort (take-pieces-for-merge appended-pieces))}]
         (if (>= 1 (count appended-pieces))
           (assoc new-fx :location ["/#/review"])
           new-fx))
       {:db (assoc-in db [:sort :merge] post-decision)}))))

(re-frame/reg-event-fx
 :clear-current-run
 (fn [{:keys [db]} _]
   "Clear the current sorting run"
   {:local-store ["sort" nil]
    :db (assoc db :sort db/sort-bootstrap)}))

(re-frame/reg-event-fx
 :save-run
 (fn [{:keys [db]} [_ payload]]
   (println (cljs-env :api-root))
   "Save to db"
   {:local-store ["sort" nil]
    :http-xhrio {:method          :post
                 :data            payload
                 :uri             (cljs-env :api-root)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true}) 
                 :on-success      [:location "#/"]}
    :db (assoc db :show-twirly true)}))