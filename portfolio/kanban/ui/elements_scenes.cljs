(ns kanban.ui.elements-scenes
  (:require [kanban.ui.elements :as e]
            [portfolio.replicant :as portfolio :refer-macros [defscene]]))

(defscene modal
  :params (atom {:open? false})
  [store]
  [:div {:style {:min-height 500}}
   [:h2.text-lg.mb-4 "Click button to open modal"]
   [:button.btn {:on {:click #(swap! store update :open? not)}}
    "Open!"]
   (when (:open? @store)
     [e/modal
      [:h2.text-lg "Hi!"]
      [:p.my-4 "Hope you're enjoying this modal"]
      [:button.btn {:on {:click #(swap! store update :open? not)}}
       "Close"]])])

(defscene alert
  :params (atom {:show? true})
  [store]
  [:div {:style {:height 100}
         :on {:click #(swap! store update :show? not)}}
   (when (:show? @store)
     (e/alert {:class :alert-error
               ::e/actions []}
              "You can't have more than 2 tasks here"))])
