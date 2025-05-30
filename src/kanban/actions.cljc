(ns kanban.actions
  (:require [clojure.walk :as walk]
            [kanban.command :as command]
            [kanban.http :as http]
            [kanban.query :as query]
            [kanban.task :as task]))

(defn action? [data]
  (and (sequential? data)
       (keyword? (first data))))

(defn action-list? [data]
  (and (sequential? data)
       (every? action? data)))

(defn expand-1 [state actions action]
  (let [kind (first action)
        f (get-in actions [kind :expand])
        expanded (if (ifn? f)
                   (let [expansion (f state action)]
                     (assert (action-list? expansion)
                             (str kind " should expand to a collection of actions"))
                     expansion)
                   (cond->> action
                     (ifn? f) (f state)))]
    (if (= expanded action)
      [expanded]
      (mapcat #(expand-1 state actions %) expanded))))

(defn expand-actions [state actions action-data]
  (mapv (fn [action]
          {:action action
           :expanded (expand-1 state actions action)})
        action-data))

(defn expand-placeholders [ctx placeholders action-data]
  (walk/postwalk
   (fn [x]
     (if-let [f (get placeholders x)]
       (f ctx x)
       x))
   action-data))

(defn assoc-in* [m kvs]
  (reduce (fn [m [path v]]
            (loop [[k & ks] path
                   curr-path []
                   m m]
              (let [path (conj curr-path k)
                    new-m (cond-> m
                            (and (number? k) (sequential? (get-in m curr-path)))
                            (update-in curr-path vec))]
                (if (nil? ks)
                  (assoc-in new-m path v)
                  (recur ks path new-m))))) m kvs))

(defn update-in* [m path f & args]
  (loop [[k & ks] path
         curr-path []
         m m]
    (let [path (conj curr-path k)
          new-m (cond-> m
                  (and (number? k) (sequential? (get-in m curr-path)))
                  (update-in curr-path vec))]
      (if (nil? ks)
        (apply update-in new-m path f args)
        (recur ks path new-m)))))

(defn dissoc-in* [m paths]
  (reduce (fn [m path]
            (loop [[k & ks] path
                   curr-path []
                   m m]
              (let [new-m (cond-> m
                            (and (number? k) (sequential? (get-in m curr-path)))
                            (update-in curr-path vec))]
                (if (nil? ks)
                  (update-in new-m curr-path dissoc k)
                  (recur ks (conj curr-path k) new-m))))) m paths))

(defn tick
  ([f]
   #?(:cljs (js/requestAnimationFrame f)
      :clj (f)))
  ([#?(:clj _ :cljs ms) f]
   #?(:cljs (js/setTimeout f ms)
      :clj (f))))

(defn prevent-default #?(:cljs [^js event] :clj [event])
  (some-> event .preventDefault)
  nil)

(defn start-drag-move #?(:cljs [^js event] :clj [event])
  (when event
    (tick #(-> event .-target .-classList (.add "invisible")))
    (set! (.-effectAllowed (.-dataTransfer event)) "move"))
  nil)

(defn end-drag-move #?(:cljs [^js event] :clj [event])
  (some-> event .-target .-classList (.remove "invisible"))
  nil)

(def actions
  (-> {:actions/assoc-in
       (fn [{:keys [store]} [_ & args]]
         (swap! store assoc-in* (partition 2 args)))

       :actions/dissoc-in
       (fn [{:keys [store]} [_ & args]]
         (swap! store dissoc-in* args))

       :actions/command
       (fn [{:keys [store handle-actions]} [_ & args]]
         (apply http/make-http-request store "/command"
                command/issue-command command/receive-response
                handle-actions args))

       :actions/conj-in
       (fn [{:keys [store]} [_ path el]]
         (swap! store update-in* path conj el))

       :actions/delay
       (fn [{:keys [handle-actions]} [_ ms actions]]
         (tick ms #(handle-actions actions)))

       :actions/end-drag-move
       (fn [{:keys [event]} _]
         (end-drag-move event))

       :actions/prevent-default
       (fn [{:keys [event]} _]
         (prevent-default event))

       :actions/query
       (fn [{:keys [store handle-actions]} [_ & args]]
         (apply http/make-http-request store "/query"
                query/send-request query/receive-response
                handle-actions args))

       :actions/start-drag-move
       (fn [{:keys [event]} _]
         (start-drag-move event))

       :actions/subscribe-query
       (fn [{:keys [store]} [_ & args]]
         (apply http/connect-event-source store "/query/subscribe" "query"
                query/send-request query/receive-response args))}
      (update-vals (fn [f] {:execute f}))
      (into task/actions)))

(defn handle-actions [store event-opts placeholders action-data]
  (let [state (when store
                (assoc @store :now #?(:cljs (js/Date.) :clj (java.util.Date.))))
        ctx {:store store
             :event (:replicant/dom-event event-opts)
             :handle-actions (partial handle-actions store event-opts placeholders)}]
    (doseq [action (->> (expand-actions state actions action-data)
                        (mapcat :expanded)
                        (expand-placeholders event-opts placeholders))]
      (let [f (get-in actions [(first action) :execute])]
        (f ctx action)))))
