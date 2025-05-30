(ns kanban.task
  (:require [kanban.query :as query]
            [clojure.string :as str]))

(def task-query {:query/kind :queries/tasks})

(defn get-tasks [state]
  (if (:tasks state)
    {:tasks (:tasks state)}
    (if-let [results (query/get-result state task-query)]
      {:tasks results}
      {:loading? (query/loading? state task-query)
       :error? (query/error? state task-query)})))

(defn get-new-tasks [state-old state-new]
  (let [old-tasks (:tasks (get-tasks state-old))
        new-tasks (:tasks (get-tasks state-new))]
    (when (and (seq old-tasks) (seq new-tasks))
      (->> (mapv :task/id new-tasks)
           (remove (set (mapv :task/id old-tasks)))
           set))))

(defn set-task-status [state id status]
  (if (:tasks state)
    (let [idx (->> (:tasks state)
                   (map-indexed vector)
                   (filter (comp #{id} :task/id second))
                   ffirst)]
      [[:actions/assoc-in
        [:tasks idx :task/status] status
        [:tasks idx :task/changed-status-at] (:now state)]])
    [[:actions/command
      {:command/kind :commands/set-task-status
       :command/data {:task/id id, :task/status status}}]]))

(defn add-task [state form-data]
  (let [tags (->> (str/split (:tags form-data) #" ")
                  (mapv str/trim)
                  (filter not-empty)
                  (mapv #(keyword "tags" %)))
        task (cond-> (-> (dissoc form-data :tags)
                         (assoc :task/id (random-uuid))
                         (assoc :task/created-at (:now state)))
               (not-empty tags) (assoc :task/tags (set tags)))]
    (if (:tasks state)
      [[:actions/conj-in [:tasks] task]]
      [[:actions/command
        {:command/kind :commands/create-task
         :command/data task}
        {:on-success [[:actions/query {:query/kind :queries/tasks}]]}]])))

(def actions
  {:actions/add-task
   {:expand (fn [state [_ & args]]
              (apply add-task state args))}

   :actions/set-task-status
   {:expand (fn [state [_ & args]]
              (apply set-task-status state args))}})
