(ns kanban.ui
  (:require [kanban.task :as task]
            [kanban.ui.elements :as e]
            [phosphor.icons :as icons]))

(defn ^{:indent 1} render-task [state {:task/keys [id title tags priority description] :as task}]
  (let [expanded? (task/expanded? state task)]
    [e/card (cond-> {:on {:dragstart [[:actions/assoc-in [:transient :dragging] id]]}}
              expanded? (assoc ::e/expanded? true)
              (contains? (:new-tasks state) id) (update :class conj :wiggle-in))
     (when (seq tags)
       (conj (let [tag->style (into {} (mapv (juxt :tag/ident :tag/style) (:tags state)))]
               (into [e/badges]
                     (mapv (fn [t]
                             [e/badge {::e/style (tag->style t)} (name t)])
                           tags)))))
     (when description
       [e/card-action
        [::e/toggle-button.btn-small
         {::e/on? expanded?
          :on {:click [[(if expanded? :actions/collapse-task :actions/expand-task) id]]}}
         (icons/icon :phosphor.regular/file)
         (icons/icon :phosphor.regular/x)]])
     [e/card-title
      (when (= :priority/high priority)
        [e/icon {:class [:text-error]} (icons/icon :phosphor.regular/fire)])
      (when (= :priority/low priority)
        [e/icon {:class [:opacity-50]} (icons/icon :phosphor.regular/tray-arrow-down)])
      title]
     (when expanded?
       [e/card-details
        [:p description]])]))

(defn get-drop-action [{:column/keys [status limit]} status->tasks task]
  (when (and task (not= status (:task/status task)))
    (if (or (nil? limit) (< (count (status->tasks status)) limit))
      [[:actions/set-task-status (:task/id task) status]]
      [[:actions/flash 3000 [:transient status :error] :errors/at-limit]])))

(defn render-error [{:column/keys [status limit]} error]
  (e/alert {:class :alert-error
            ::e/actions [[:actions/dissoc-in [:transient status :error]]]}
   (when (= :errors/at-limit error)
     (str "You can't have more than " limit " task" (when-not (= 1 limit) "s") " here"))))

(defn task-sort-k [task]
  [(case (:task/priority task)
     :priority/high -1
     :priority/medium 0
     1)
   (or (:task/changed-status-at task) (:task/created-at task))])

(defn keyword->s [k]
  (str (when-let [ns (namespace k)]
         (str ns "/")) (name k)))

(defn render-task-form [status]
  [:form.bg-base-100.p-4.rounded-md.flex.flex-col.gap-2.z-1.mt-auto
   {:on {:submit [[:actions/prevent-default]
                  [:actions/dissoc-in [:transient status :add?]]
                  [:actions/add-task [:event/form-data] [:random/uuid]]]}}
   [:input {:type "hidden"
            :name "task/status"
            :data-type "keyword"
            :value (keyword->s status)}]
   [e/text-input
    {::e/autofocus? true
     :name "task/title"
     :type "text"
     :placeholder "Task"}]
   [:textarea.textarea
    {:name "task/description"
     :placeholder "Description"}]
   [:select.select
    {:name "task/priority"
     :data-type "keyword"}
    [:option {:value "priority/high"} "High priority"]
    [:option {:value "priority/medium" :selected "selected"} "Medium priority"]
    [:option {:value "priority/low"} "Low priority"]]
   [:input.input
    {:name "tags"
     :type "text"
     :placeholder "Tags"}]
   [:div.flex.gap-2
    [::e/button.btn-primary {:type "submit"}
     [e/icon {::e/size :w-4} (icons/icon :phosphor.regular/plus-circle)]
     "Add task"]
    [::e/button.btn-ghost {:type "button"
                           :on {:click [[:actions/dissoc-in [:transient status :add?]]]}}
     [e/icon {::e/size :w-4} (icons/icon :phosphor.regular/x-circle)]
     "Cancel"]]])

(defn render-columns [state {:keys [tasks error? loading?]}]
  (let [status->tasks (group-by :task/status tasks)
        task (first (filterv (comp #{(-> state :transient :dragging)} :task/id) tasks))
        available? (and (not error?) (not loading?))]
    (into [:div.flex.gap-16.swimlane]
          (for [column (:columns state)]
            (let [adding? (get-in state [:transient (:column/status column) :add?])]
              [e/column {:class (name (:column/status column))}
               [:h2.text-2xl
                (if available?
                  (str (:column/title column) " ("
                       (count (status->tasks (:column/status column)))
                       (when-let [n (:column/limit column)]
                         (str "/" n))
                       ")")
                  (:column/title column))]
               (some->> (get-in state [:transient (:column/status column) :error])
                        (render-error column))
               (if (not available?)
                 [e/column-body {}
                  (when (and loading? (= column (first (:columns state))))
                    [:span.loading.loading-spinner.loading-xl])]
                 (cond-> [e/column-body
                          {:on {:drop (concat [[:actions/prevent-default]]
                                              (get-drop-action column status->tasks task))}}]
                   :then
                   (into
                    (cond->> (status->tasks (:column/status column))
                      :then (sort-by (or (:column/sort-by column) task-sort-k))
                      (= :desc (:column/sort-order column)) reverse
                      :then (mapv #(render-task state %))))

                   (and (:column/add-new? column) (not adding?))
                   (conj [::e/button.z-1.mt-auto
                          {:on
                           {:click
                            [[:actions/assoc-in [:transient (:column/status column) :add?] true]]}}
                          [e/icon {::e/size :w-4} (icons/icon :phosphor.regular/plus)]
                          "Add task"])

                   adding?
                   (conj (render-task-form (:column/status column)))))])))))

(defn render-modal [{:keys [error?]}]
  (when error?
    [e/modal
     [:h2.text-lg.mb-4 "Failed to load tasks"]
     [e/button {::e/actions [[:actions/load-tasks]]}
      "Try again"]]))

(defn render-app [state]
  (let [tasks (task/get-tasks state)]
    [:main.m-8
     (render-modal tasks)
     (render-columns state tasks)]))
