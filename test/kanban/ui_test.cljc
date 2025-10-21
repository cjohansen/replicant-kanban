(ns kanban.ui-test
  (:require [clojure.test :refer [deftest is testing]]
            [kanban.ui :as ui]
            [kanban.ui.elements :as-alias e]
            [lookup.core :as lookup]))

(deftest render-card-test
  (testing "Renders card title"
    (is (= (->> {:task/id "d8a1"
                 :task/status :status/open
                 :task/title "Add keyboard shortcuts for board navigation"
                 :task/priority :priority/medium
                 :task/created-at #inst "2025-04-28T10:15:00.000Z"}
                (ui/render-task {})
                (lookup/select-one ::e/card-title))
           [::e/card-title "Add keyboard shortcuts for board navigation"])))

  (testing "Renders task with tags"
    (is (= (->> {:task/title "Add keyboard shortcuts for board navigation"
                 :task/tags [:tags/feature :tags/accessibility]}
                (ui/render-task {:tags [{:tag/ident :tags/feature
                                         :tag/style :primary}
                                        {:tag/ident :tags/accessibility
                                         :tag/style :secondary}]})
                (lookup/select-one ::e/badges))
           [::e/badges
            [::e/badge {::e/style :primary} "feature"]
            [::e/badge {::e/style :secondary} "accessibility"]])))

  (testing "Renders fire icon for high priority tasks"
    (is (= (->> {:task/title "Add keyboard shortcuts for board navigation"
                 :task/priority :priority/high}
                (ui/render-task {})
                (lookup/select-one [::e/card-title ::e/icon]))
           [::e/icon {:class #{"text-error"}} :phosphor.regular/fire])))

  (testing "Renders tray icon for low priority tasks"
    (is (= (->> {:task/title "Add keyboard shortcuts for board navigation"
                 :task/priority :priority/low}
                (ui/render-task {})
                (lookup/select-one [::e/card-title ::e/icon]))
           [::e/icon {:class #{"opacity-50"}} :phosphor.regular/tray-arrow-down])))

  (testing "Renders button to expand card when there is a description"
    (is (= (->> {:task/id "task1"
                 :task/title "Add keyboard shortcuts for board navigation"
                 :task/description "Allow users to navigate between columns and cards using arrow keys and hotkeys."}
                (ui/render-task {})
                (lookup/select-one [::e/card-action]))
           [::e/card-action
            [::e/toggle-button
             {::e/on? false
              :class #{"btn-small"}
              :on {:click [[:actions/expand-task "task1"]]}}
             :phosphor.regular/file
             :phosphor.regular/x]])))

  (testing "Does not render description initially"
    (is (nil? (->> {:task/id "task1"
                    :task/title "Add keyboard shortcuts for board navigation"
                    :task/description "Allow users to navigate between columns and cards using arrow keys and hotkeys."}
                   (ui/render-task {})
                   (lookup/select-one ::e/card-details)))))

  (testing "Marks card as expanded"
    (is (->> {:task/id "task1"
              :task/title "Add keyboard shortcuts for board navigation"
              :task/description "Allow users to navigate between columns and cards using arrow keys and hotkeys."}
             (ui/render-task {:transient {"task1" {:expanded? true}}})
             (lookup/select-one ::e/card)
             lookup/attrs
             ::e/expanded?)))

  (testing "Renders description on expanded task"
    (is (= (->> {:task/id "task1"
                 :task/title "Add keyboard shortcuts for board navigation"
                 :task/description "Allow users to navigate between columns and cards using arrow keys and hotkeys."}
                (ui/render-task {:transient {"task1" {:expanded? true}}})
                (lookup/select-one [::e/card ::e/card-details]))
           [::e/card-details
            [:p "Allow users to navigate between columns and cards using arrow keys and hotkeys."]])))

  (testing "Renders button to close expanded card"
    (is (= (->> {:task/id "task1"
                 :task/title "Add keyboard shortcuts for board navigation"
                 :task/description "Allow users to navigate between columns and cards using arrow keys and hotkeys."}
                (ui/render-task {:transient {"task1" {:expanded? true}}})
                (lookup/select-one [::e/card-action]))
           [::e/card-action
            [::e/toggle-button
             {::e/on? true
              :class #{"btn-small"}
              :on {:click [[:actions/collapse-task "task1"]]}}
             :phosphor.regular/file
             :phosphor.regular/x]])))

  (testing "Starting to drag card marks it as moving"
    (is (= (->> {:task/id "task1"
                 :task/title "Add keyboard shortcuts for board navigation"}
                (ui/render-task {})
                (lookup/select-one [::e/card])
                lookup/attrs)
           {:on {:dragstart [[:actions/assoc-in [:transient :dragging] "task1"]]}})))

  (testing "Wiggles in new cards"
    (is (contains?
         (->> {:task/id "task1"
               :task/title "Add keyboard shortcuts for board navigation"}
              (ui/render-task {:new-tasks #{"task1"}})
              (lookup/select-one [::e/card])
              lookup/attrs
              :class)
         "wiggle-in"))))

(defn strip-attrs [attrs hiccup]
  (apply update hiccup 1 dissoc attrs))

(deftest render-columns-test
  (testing "Renders todo column with tasks"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open
                             :column/title "Todo"}]}
                 {:tasks
                  [{:task/status :status/open
                    :task/title "Add keyboard shortcuts for board navigation"}
                   {:task/status :status/in-progress
                    :task/title "Add markdown support in descriptions"}]})
                (lookup/select [::e/card-title])
                lookup/text)
           "Add keyboard shortcuts for board navigation")))

  (testing "Dropping open task on open column has no effect"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open
                             :column/title "Todo"}]
                  :transient {:dragging "task1"}}
                 {:tasks
                  [{:task/id "task1"
                    :task/status :status/open
                    :task/title "Add keyboard shortcuts for board navigation"}]})
                (lookup/select-one [::e/column-body])
                lookup/attrs)
           {:on {:drop [[:actions/prevent-default]]}})))

  (testing "Dropping open task on WIP column marks it as in progress"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open
                             :column/title "Todo"}
                            {:column/status :status/in-progress
                             :column/title "WIP"}]
                  :transient {:dragging "task1"}}
                 {:tasks
                  [{:task/id "task1"
                    :task/status :status/open}]})
                (lookup/select [::e/column-body])
                second
                lookup/attrs)
           {:on {:drop [[:actions/prevent-default]
                        [:actions/set-task-status "task1" :status/in-progress]]}})))

  (testing "Dropping open task on done column marks it as closed"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open
                             :column/title "Todo"}
                            {:column/status :status/in-progress
                             :column/title "WIP"}
                            {:column/status :status/closed
                             :column/title "Done"}]
                  :transient {:dragging "task1"}}
                 {:tasks
                  [{:task/id "task1"
                    :task/status :status/open}]})
                (lookup/select [::e/column-body])
                last
                lookup/attrs)
           {:on {:drop [[:actions/prevent-default]
                        [:actions/set-task-status "task1" :status/closed]]}})))

  (testing "Dropping task on column at limit produces an error message"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open}
                            {:column/status :status/in-progress
                             :column/limit 2}]
                  :transient {:dragging "task1"}}
                 {:tasks
                  [{:task/id "task1"
                    :task/status :status/open}
                   {:task/id "task2"
                    :task/status :status/in-progress}
                   {:task/id "task3"
                    :task/status :status/in-progress}]})
                (lookup/select [::e/column-body])
                last
                lookup/attrs)
           {:on {:drop [[:actions/prevent-default]
                        [:actions/flash 3000 [:transient :status/in-progress :error] :errors/at-limit]]}})))

  (testing "Renders limit error on column"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open
                             :column/limit 2}]
                  :transient {:status/open {:error :errors/at-limit}}}
                 {:tasks []})
                (lookup/select-one [::e/column :.alert-error])
                lookup/strip-attrs)
           [:div
            "You can't have more than 2 tasks here"
            [:button {:class #{"cursor-pointer" "justify-self-end"}
                      :on {:click [[:actions/dissoc-in [:transient :status/open :error]]]}}
             [::e/icon {::e/size :w-4} :phosphor.regular/x]]])))

  (testing "Indicates limit in column title"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open
                             :column/title "Todo"}
                            {:column/status :status/in-progress
                             :column/title "WIP"
                             :column/limit 2}]
                  :transient {:status/open {:error :errors/at-limit}}}
                 {:tasks
                  [{:task/status :status/open}
                   {:task/status :status/in-progress}
                   {:task/status :status/open}]})
                (lookup/select [::e/column :h2])
                (mapv lookup/text))
           ["Todo (2)" "WIP (1/2)"])))

  (testing "Sorts tasks by priority and state changed or created date"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open}
                            {:column/status :status/in-progress
                             :column/limit 2}]}
                 {:tasks
                  [{:task/title "Important #1"
                    :task/status :status/open
                    :task/priority :priority/high
                    :task/changed-status-at #inst "2025-05-10T12:00:00"}
                   {:task/title "Important #2"
                    :task/status :status/open
                    :task/priority :priority/high
                    :task/created-at #inst "2025-05-10T11:00:00"}
                   {:task/title "Not important"
                    :task/status :status/open
                    :task/priority :priority/low
                    :task/changed-status-at #inst "2025-05-10T12:00:00"}
                   {:task/title "Medium #1"
                    :task/status :status/open
                    :task/priority :priority/medium
                    :task/changed-status-at #inst "2025-05-10T12:00:00"}
                   {:task/title "Medium #2"
                    :task/status :status/open
                    :task/priority :priority/medium
                    :task/changed-status-at #inst "2025-05-10T11:00:00"}
                   {:task/title "Medium #3"
                    :task/status :status/open
                    :task/priority :priority/medium
                    :task/created-at #inst "2025-05-10T13:00:00"}]})
                (lookup/select [::e/card-title])
                (mapv lookup/text))
           [":phosphor.regular/fire Important #2"
            ":phosphor.regular/fire Important #1"
            "Medium #2"
            "Medium #1"
            "Medium #3"
            ":phosphor.regular/tray-arrow-down Not important"])))

  (testing "Applies column-specific custom sorting"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/closed
                             :column/sort-by :task/changed-status-at
                             :column/sort-order :desc}]}
                 {:tasks
                  [{:task/title "Closed 1"
                    :task/status :status/closed
                    :task/priority :priority/high
                    :task/changed-status-at #inst "2025-05-10T11:00:00"}
                   {:task/title "Closed 2"
                    :task/status :status/closed
                    :task/priority :priority/low
                    :task/changed-status-at #inst "2025-05-09T13:00:00"}
                   {:task/title "Closed 3"
                    :task/status :status/closed
                    :task/priority :priority/medium
                    :task/changed-status-at #inst "2025-05-11T13:00:00"}]})
                (lookup/select [::e/card-title])
                (mapv lookup/text))
           ["Closed 3"
            ":phosphor.regular/fire Closed 1"
            ":phosphor.regular/tray-arrow-down Closed 2"])))

  (testing "Renders button to add tasks to column"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open
                             :column/add-new? true}]}
                 {:tasks []})
                (lookup/select-one [::e/column-body ::e/button])
                (strip-attrs #{:class}))
           [::e/button
            {:on {:click [[:actions/open-new-task-form :status/open]]}}
            [::e/icon {::e/size :w-4} :phosphor.regular/plus]
            "Add task"])))

  (testing "Renders form for adding task"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open
                             :column/add-new? true}]
                  :transient {:status/open {:add? true}}}
                 {:tasks []})
                (lookup/select-one [::e/column-body :form])
                lookup/attrs
                :on :submit)
           [[:actions/prevent-default]
            [:actions/close-new-task-form :status/open]
            [:actions/add-task [:event/form-data] [:random/id]]])))

  (testing "Includes a cancel button on the new task form"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open
                             :column/add-new? true}]
                  :transient {:status/open {:add? true}}}
                 {:tasks []})
                (lookup/select [::e/column-body :form ::e/button])
                last
                lookup/attrs
                :on :click)
           [[:actions/close-new-task-form :status/open]])))

  (testing "Renders loading spinner when tasks aren't available"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/status :status/open}
                            {:column/status :status/in-progress}]
                  :transient {:status/open {:add? true}}}
                 {:loading? true})
                (lookup/select [::e/column-body :span.loading]))
           [[:span {:class #{"loading-xl" "loading" "loading-spinner"}}]])))

  (testing "Does not render spinner when loading tasks failed"
    (is (nil?
         (->> (ui/render-columns
               {:columns [{:column/status :status/open}
                          {:column/status :status/in-progress}]
                :transient {:status/open {:add? true}}}
               {:error? true})
              (lookup/select-one [::e/column-body :span.loading])))))

  (testing "Does not render counts on column headers when data is loading"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/title "Todo"
                             :column/status :status/open}
                            {:column/title "WIP"
                             :column/limit 2
                             :column/status :status/in-progress}]
                  :transient {:status/open {:add? true}}}
                 {:loading? true})
                (lookup/select [::e/column :h2])
                (mapv lookup/text))
           ["Todo" "WIP"])))

  (testing "Does not render counts on column headers when loading data failed"
    (is (= (->> (ui/render-columns
                 {:columns [{:column/title "Todo"
                             :column/status :status/open}
                            {:column/title "WIP"
                             :column/limit 2
                             :column/status :status/in-progress}]
                  :transient {:status/open {:add? true}}}
                 {:error? true})
                (lookup/select [::e/column :h2])
                (mapv lookup/text))
           ["Todo" "WIP"]))))

(deftest render-modal-test
  (testing "Renders a modal when failing to load tasks"
    (is (= (->> (ui/render-modal {:error? true})
                lookup/children
                (mapv lookup/text))
           ["Failed to load tasks" "Try again"]))))
