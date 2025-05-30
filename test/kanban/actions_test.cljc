(ns kanban.actions-test
  (:require [clojure.test :refer [deftest is testing]]
            [kanban.actions :as actions]))

(deftest assoc-in*-test
  (testing "Converts lists to vectors"
    (is (= (actions/assoc-in* {:tasks '()} [[[:tasks 0 :task/status] :status/in-progress]])
           {:tasks [{:task/status :status/in-progress}]}))))

(deftest update-in*-test
  (testing "Adds to list in nested structure"
    (is (= (actions/update-in* {:tasks '()} [:tasks 0 :task/tags] conj :tags/features)
           {:tasks [{:task/tags [:tags/features]}]}))))

(deftest dissoc-in*-test
  (testing "Dissocs key"
    (is (= (actions/dissoc-in* {:tasks '({:task/status :status/open})}
                               [[:tasks 0 :task/status]])
           {:tasks [{}]}))))

(deftest expand-1-test
  (testing "Expands single action to list of one action"
    (is (= (actions/expand-1 {} nil [:actions/assoc-in [:a] "B"])
           [[:actions/assoc-in [:a] "B"]])))

  (testing "Expands single action to two actions"
    (is (= (actions/expand-1
            {}
            {:mydomain/doit
             {:expand
              (fn [_ [_ a b]]
                [[:actions/assoc-in [:a] a]
                 [:actions/assoc-in [:b] b]])}}
            [:mydomain/doit "One" "Two"])
           [[:actions/assoc-in [:a] "One"]
            [:actions/assoc-in [:b] "Two"]]))))
