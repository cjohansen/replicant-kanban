(ns kanban.task-test
  (:require [clojure.test :refer [deftest is testing]]
            [kanban.task :as task]))

(deftest set-task-status-test
  (testing "Sets status on the right task"
    (is (= (task/set-task-status
            {:tasks [{:task/id "task1"}]
             :now #inst "2025-05-10"}
            "task1" :status/in-progress)
           [[:actions/assoc-in [:tasks "task1" :task/status] :status/in-progress]
            [:actions/assoc-in [:tasks "task1" :task/changed-status-at] #inst "2025-05-10"]])))

  (testing "Sets status with a command when data comes from the server"
    (is (= (task/set-task-status {} "task1" :status/in-progress)
           [[:actions/command
             {:command/kind :commands/set-task-status
              :command/data
              {:task/id "task1"
               :task/status :status/in-progress}}]]))))

(deftest add-task-test
  (testing "Parses out tags"
    (is (= (task/add-task
            {:now #inst "2025-05-10"
             :tags [{:tag/ident :tags/feature
                     :tag/style :primary}
                    {:tag/ident :tags/ui
                     :tag/style :accent}]
             :tasks []}
            {:task/status :status/open
             :task/title "Support more stuff"
             :task/priority :priority/medium
             :tags "feature ui"}
            #uuid "254dcfb3-031f-4256-bcd3-db7a1313a7bb")
           [[:actions/assoc-in [:tasks #uuid "254dcfb3-031f-4256-bcd3-db7a1313a7bb"]
             {:task/id #uuid "254dcfb3-031f-4256-bcd3-db7a1313a7bb"
              :task/status :status/open
              :task/title "Support more stuff"
              :task/priority :priority/medium
              :task/created-at #inst "2025-05-10"
              :task/tags #{:tags/feature
                           :tags/ui}}]])))

  (testing "Ignores empty tags"
    (is (empty?
         (->> (task/add-task
               {:now #inst "2025-05-10"}
               {:task/status :status/open
                :task/title "Support more stuff"
                :task/priority :priority/medium
                :tags ""}
               #uuid "3b287a55-b930-4c30-a58a-d48c50a3ff36")
              first last :task/tags))))

  (testing "Adds task with a command when data is from the server"
    (is (= (task/add-task
            {:now #inst "2025-05-10"}
            {:task/status :status/open
             :task/title "Support more stuff"
             :task/priority :priority/medium
             :tags "feature ui"}
            #uuid "3b287a55-b930-4c30-a58a-d48c50a3ff36")
           [[:actions/command
             {:command/kind :commands/create-task
              :command/data
              {:task/id #uuid "3b287a55-b930-4c30-a58a-d48c50a3ff36"
               :task/status :status/open
               :task/title "Support more stuff"
               :task/priority :priority/medium
               :task/created-at #inst "2025-05-10"
               :task/tags #{:tags/feature
                            :tags/ui}}}
             {:on-success [[:actions/query {:query/kind :queries/tasks}]]}]]))))
