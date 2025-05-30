(ns kanban.ui.column-scenes
  (:require [kanban.ui :as ui]
            [kanban.ui.card-scenes :as card]
            [kanban.ui.elements :as e]
            [phosphor.icons :as icons]
            [portfolio.replicant :as portfolio :refer-macros [defscene]]))

(defscene empty-column
  [:div {:style {:height 500}}
   [e/column
    [:h2.text-2xl "Todo"]
    [e/column-body]]])

(defscene column-with-cards
  [:div {:style {:height 500}}
   [e/column
    [:h2.text-2xl "Todo"]
    [e/column-body
     card/sample-card
     card/sample-card
     card/sample-card
     card/sample-card
     card/sample-card
     card/sample-card]]])

(defscene multiple-columns-with-cards
  [:div {:style {:height 400}}
   [:div.flex.gap-16.min-h-full
    [e/column
     [:h2.text-2xl "Todo"]
     [e/column-body
      card/sample-card
      card/sample-card]]
    [e/column
     [:h2.text-2xl "WIP"]
     [e/column-body
      card/sample-card]]]])

(def drag-card
  [e/card
   [e/badges
    [e/badge {::e/style :primary} "feature"]
    [e/badge {::e/style :accent} "ui"]]
   [e/card-title
    [e/icon {:class [:opacity-50]} (icons/icon :phosphor.regular/tray-arrow-down)]
    "Support custom tags with colors"]])

(defn drag-attrs [store target-col]
  {:on {:drop
        (fn [e]
          (.preventDefault e)
          (swap! store assoc :card-column target-col))}})

(defscene columns-with-draggable-card
  :params (atom {:title "Todo"
                 :card-column :todo})
  [store]
  [:div {:style {:height 400}}
   [:div.flex.gap-16.min-h-full
    [e/column
     [:h2.text-2xl (:title @store)]
     [e/column-body (drag-attrs store :todo)
      (when (= :todo (:card-column @store))
        drag-card)]]
    [e/column
     [:h2.text-2xl "WIP"]
     [e/column-body (drag-attrs store :wip)
      (when (= :wip (:card-column @store))
        drag-card)]]]])

(defscene column-with-error-message
  [:div {:style {:height 400}}
   [:div.flex.gap-16.min-h-full
    [e/column
     [:h2.text-2xl "Todo"]
     [e/column-body
      card/sample-card]]
    [e/column
     [:h2.text-2xl "WIP"]
     [e/alert {:class :alert-error
               ::e/actions []}
      "You can't have more than 2 tasks here"]
     [e/column-body
      card/sample-card
      card/sample-card]]]])

(defscene column-with-add-button
  [:div {:style {:height 300}}
   [:div.flex.gap-16.min-h-full
    [e/column
     [:h2.text-2xl "Todo"]
     [e/column-body
      card/sample-card
      [e/button {:class #{"z-1"}}
       [e/icon {::e/size :w-4} (icons/icon :phosphor.regular/plus)]
       "Add task"]]]]])

(defscene column-with-form
  [:div {:style {:height 420}}
   [:div.flex.gap-16.min-h-full
    [e/column
     [:h2.text-2xl "Todo"]
     [e/column-body
      card/sample-card
      (ui/render-task-form :status/open)]]]])

(defscene column-with-loader
  [:div {:style {:height 420}}
   [:div.flex.gap-16.min-h-full
    [e/column
     [:h2.text-2xl "Todo"]
     [e/column-body
      [:span.loading.loading-spinner.loading-xl]]]]])
