(ns kanban.id)

(def id (atom 9261))
(def alphabet "s4cqh587fxr93kwgjza26")

(defn random-id []
  (let [num-chars (count alphabet)
        _id (swap! id inc)]
    (->> _id
         (iterate #(/ % num-chars))
         (map long)
         (take-while pos?)
         (map #(mod % num-chars))
         (map #(nth alphabet %))
         (apply str))))
