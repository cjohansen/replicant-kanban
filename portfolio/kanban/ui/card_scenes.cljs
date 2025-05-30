(ns kanban.ui.card-scenes
  (:require [kanban.ui.elements :as e]
            [phosphor.icons :as icons]
            [portfolio.replicant :refer-macros [defscene]]))

(defscene card-title
  [e/card-title "Add keyboard shortcuts for board navigation"])

(defscene card-title-with-icon
  [e/card-title
   [e/icon {:class #{:opacity-50}} (icons/icon :phosphor.regular/tray-arrow-down)]
   "Add keyboard shortcuts for board navigation"])

(defscene simple-card
  [e/column
   [e/column-body
    [e/card
     [e/card-title "Add keyboard shortcuts for board navigation"]]]])

(defscene card-with-badges
  [e/column
   [e/column-body
    [e/card
     [e/badges
      [e/badge {::e/style :primary} "feature"]
      [e/badge {::e/style :secondary} "accessibility"]]
     [e/card-title "Display a subtle age indicator on each card"]]]])

(defscene card-with-other-badges
  [e/column
   [e/column-body
    [e/card
     [e/badges
      [e/badge {::e/style :accent} "ui"]
      [e/badge {::e/style :info} "editor"]
      [e/badge {::e/style :success} "responsive-design"]]
     [e/card-title "Enable markdown formatting"]]]])

(def sample-card
  [e/card
   [e/badges
    [e/badge {::e/style :info} "editor"]
    [e/badge {::e/style :primary} "feature"]]
   [e/card-title
    [e/icon {:class [:text-error]} (icons/icon :phosphor.regular/fire)]
    "Let users define custom tags with colors"]])

(defscene card-with-badges-and-icon
  [_]
  [e/column
   [e/column-body
    sample-card]])

(defscene expanded-card
  [:div.bg-base-content {:style {:padding 100}}
   [e/card {::e/expanded? true}
    [e/badges
     [e/badge {::e/style :primary} "feature"]
     [e/badge {::e/style :secondary} "accessibility"]]
    [e/card-title {:class :text-xl}
     [e/icon {:class #{:text-error}} (icons/icon :phosphor.regular/fire)]
     "Add keyboard shortcuts for board navigation"]
    [e/card-details
     [:p "Allow users to navigate between columns and cards using arrow keys and hotkeys."]]]])

(defscene toggle-card-expand
  "Click top right corner button to expand/collapse"
  :params (atom {:expanded? false})
  [store]
  (let [{:keys [expanded?]} @store]
    [:div.bg-base-content {:style {:padding 100}}
     [e/card {::e/expanded? expanded?}
      [e/badges
       [e/badge {::e/style :primary} "feature"]
       [e/badge {::e/style :secondary} "accessibility"]]
      [e/card-action
       [e/toggle-button
        {::e/on? expanded?
         :class #{:btn-small}
         :on {:click (fn [_]
                       (swap! store update :expanded? not))}}
        (icons/icon :phosphor.regular/file)
        (icons/icon :phosphor.regular/x)]]
      [e/card-title {:class (when expanded? :text-xl)}
       [e/icon {:class #{:text-error}} (icons/icon :phosphor.regular/fire)]
       "Add keyboard shortcuts for board navigation"]
      (when expanded?
        [e/card-details
         [:p "Allow users to navigate between columns and cards using arrow keys and hotkeys."]])]]))
