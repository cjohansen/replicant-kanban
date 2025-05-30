(ns kanban.core
  (:require [kanban.actions :as actions]
            [kanban.forms :as forms]
            [kanban.task :as task]
            [kanban.ui :as ui]
            [replicant.dom :as r]))

(def placeholders
  {:event/target.value
   (fn [{:replicant/keys [^js dom-event]}]
     (some-> dom-event .-target .-value))

   :event/form-data
   (fn [{:replicant/keys [^js dom-event]}]
     (some-> dom-event .-target forms/gather-form-data))

   :clock/now #(js/Date.)})

(defn ^{:indent 2} boot [store el & [{:keys [on-render]}]]
  (r/set-dispatch! #(actions/handle-actions store %1 placeholders %2))
  (add-watch store ::render (fn [_ _ old-state new-state]
                              (let [hiccup (-> (assoc new-state :new-tasks (task/get-new-tasks old-state new-state))
                                               ui/render-app)]
                                (when (fn? on-render)
                                  (on-render hiccup))
                                (r/render el hiccup)))))
