(ns kanban.pages.board-scenes
  (:require [kanban.query :as query]
            [kanban.sample-data :as sample-data]
            [kanban.task :as task]
            [kanban.ui :as ui]
            [kanban.ui.elements :as e]
            [lookup.core :as lookup]
            [nexus.registry :as nxr]
            [portfolio.replicant :refer-macros [defscene]]))

(defn ->tasks [tasks]
  (some->> tasks
           (map (juxt :task/id identity))
           (into {})))

(defn render-app [state]
  [:div {:style {:min-height 800}}
   (ui/render-app
    (-> {:columns sample-data/columns
         :tags sample-data/tags}
        (merge state)))])

(defn render-scenario [state]
  {:state state
   :hiccup (render-app state)})

(defn dispatch-actions [state actions]
  (let [store (atom state)]
    (nxr/dispatch store {} actions)
    @store))

(defn trigger-event [app selector event]
  (let [state (->> (:hiccup app)
                   (lookup/select-one selector)
                   lookup/attrs :on event
                   (dispatch-actions (:state app)))]
    {:state state
     :hiccup (render-app state)}))

(defscene empty
  (render-app {}))

(defscene loading
  (-> (query/send-request {} (js/Date.) task/task-query)
      render-app))

(defscene failed-to-load
  (-> (query/send-request {} (js/Date.) task/task-query)
      (query/receive-response (js/Date.) task/task-query
       {:query/status :query.status/error})
      render-app))

(defscene single-todo-task
  (render-app
   {:tasks (->> sample-data/tasks
                (filterv (comp #{:status/open} :task/status))
                (take 1)
                ->tasks)}))

(defscene a-bunch-of-tasks
  (render-app
   {:tasks (->tasks sample-data/tasks)}))

(defscene expanded-task
  (-> {:tasks (->tasks sample-data/tasks)}
      render-scenario
      (trigger-event [::e/card ::e/toggle-button] :click)
      :hiccup))

(defscene overflowing-wip-column
  (let [[a b c] sample-data/tasks]
    (-> {:tasks (->tasks
                 [(assoc a :task/status :status/open)
                  (assoc b :task/status :status/in-progress)
                  (assoc c :task/status :status/in-progress)])}
        render-scenario
        (trigger-event ::e/card :dragstart)
        (trigger-event [:.in-progress ::e/column-body] :drop)
        :hiccup)))

(defscene open-form
  (-> {:tasks (->tasks (take-last 1 sample-data/tasks))}
      render-scenario
      (trigger-event [::e/column ::e/button] :click)
      :hiccup))
