(ns kanban.task-test
  (:require [kanban.task :as task]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]))

(defn strip-uuids [data]
  (walk/postwalk
   (fn [x]
     (if (uuid? x)
       "#UUID"
       x))
   data))

(defn expand-action [state action]
  (let [f (get-in task/actions [(first action) :expand])]
    (f state action)))

(deftest set-task-status-test
  (testing "Sets status on the right task"
    (is (= (expand-action
            {:tasks [{:task/id "task1"}]
             :now #inst "2025-05-10"}
            [:actions/set-task-status "task1" :status/in-progress])
           [[:actions/assoc-in
             [:tasks 0 :task/status] :status/in-progress
             [:tasks 0 :task/changed-status-at] #inst "2025-05-10"]])))

  (testing "Sets status with a command when data comes from the server"
    (is (= (expand-action {} [:actions/set-task-status "task1" :status/in-progress])
           [[:actions/command
             {:command/kind :commands/set-task-status
              :command/data
              {:task/id "task1"
               :task/status :status/in-progress}}]]))))

(deftest add-task-test
  (testing "Parses out tags"
    (is (= (->> (expand-action
                 {:now #inst "2025-05-10"
                  :tags [{:tag/ident :tags/feature
                          :tag/style :primary}
                         {:tag/ident :tags/ui
                          :tag/style :accent}]
                  :tasks []}
                 [:actions/add-task
                  {:task/status :status/open
                   :task/title "Support more stuff"
                   :task/priority :priority/medium
                   :tags "feature ui"}])
                strip-uuids)
           [[:actions/conj-in [:tasks]
             {:task/id "#UUID"
              :task/status :status/open
              :task/title "Support more stuff"
              :task/priority :priority/medium
              :task/created-at #inst "2025-05-10"
              :task/tags #{:tags/feature
                           :tags/ui}}]])))

  (testing "Ignores empty tags"
    (is (empty?
         (->> (expand-action
               {:now #inst "2025-05-10"}
               [:actions/add-task
                {:task/status :status/open
                 :task/title "Support more stuff"
                 :task/priority :priority/medium
                 :tags ""}])
              first last :task/tags))))

  (testing "Adds task with a command when data is from the server"
    (is (= (->> [:actions/add-task
                 {:task/status :status/open
                  :task/title "Support more stuff"
                  :task/priority :priority/medium
                  :tags "feature ui"}]
                (expand-action {:now #inst "2025-05-10"})
                strip-uuids)
           [[:actions/command
             {:command/kind :commands/create-task
              :command/data
              {:task/id "#UUID"
               :task/status :status/open
               :task/title "Support more stuff"
               :task/priority :priority/medium
               :task/created-at #inst "2025-05-10"
               :task/tags #{:tags/feature
                            :tags/ui}}}
             {:on-success [[:actions/query {:query/kind :queries/tasks}]]}]]))))
