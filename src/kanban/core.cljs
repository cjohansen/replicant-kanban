(ns kanban.core
  (:require [kanban.actions :as actions]
            [kanban.task :as task]
            [kanban.ui :as ui]
            [nexus.registry :as nxr]
            [replicant.dom :as r]))

::actions/keep

(defn ^{:indent 2} boot [store el & [{:keys [on-render]}]]
  (r/set-dispatch! #(nxr/dispatch store %1 %2))
  (add-watch store ::render
   (fn [_ _ old-state new-state]
     (let [hiccup (-> (assoc new-state :new-tasks (task/get-new-tasks old-state new-state))
                      ui/render-app)]
       (when (fn? on-render)
         (on-render hiccup))
       (r/render el hiccup)))))
