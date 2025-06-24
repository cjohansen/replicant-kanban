(ns kanban.actions
  (:require [kanban.command :as command]
            #?(:cljs [kanban.forms :as forms])
            [kanban.http :as http]
            [kanban.id :as id]
            [kanban.query :as query]
            [kanban.task :as task]
            [nexus.registry :as nxr]))

(defn tick
  ([f]
   #?(:cljs (js/requestAnimationFrame f)
      :clj (f)))
  ([#?(:clj _ :cljs ms) f]
   #?(:cljs (js/setTimeout f ms)
      :clj (f))))

(nxr/register-effect! :actions/prevent-default
  (fn [{:keys [dispatch-data]}]
    (some-> dispatch-data :replicant/dom-event .preventDefault)))

(nxr/register-effect! :actions/start-drag-move
  (fn [{:keys [dispatch-data]}]
    (when-let [event (:replicant/dom-event dispatch-data)]
      (tick #(-> event .-target .-classList (.add "invisible")))
      (set! (.-effectAllowed (.-dataTransfer event)) "move"))))

(nxr/register-effect! :actions/end-drag-move
  (fn [{:keys [dispatch-data]}]
    (some-> dispatch-data :replicant/dom-event
            .-target .-classList (.remove "invisible"))))

(nxr/register-effect! :actions/delay
  (fn [{:keys [dispatch]} _ ms actions]
    (tick ms #(dispatch actions))))

(nxr/register-effect! :effects/save
  ^:nexus/batch
  (fn [_ store ops]
    (swap! store
     (fn [state]
       (reduce (fn [s [op path v]]
                 (case op
                   :assoc-in (assoc-in s path v)
                   :dissoc-in (update-in s (butlast path) dissoc (last path))))
               state ops)))))

(nxr/register-effect! :actions/command
  (fn [{:keys [dispatch]} store & args]
    (apply http/make-http-request store "/command"
           command/issue-command command/receive-response
           dispatch args)))

(nxr/register-effect! :actions/query
  (fn [{:keys [dispatch]} store & args]
    (apply http/make-http-request store "/query"
           query/send-request query/receive-response
           dispatch args)))

(nxr/register-effect! :actions/subscribe-query
  (fn [_ store & args]
    (apply http/connect-event-source store "/query/subscribe" "query"
           query/send-request query/receive-response args)))

(nxr/register-action! :actions/assoc-in
  (fn [_ path v]
    [[:effects/save :assoc-in path v]]))

(nxr/register-action! :actions/dissoc-in
  (fn [_ path]
    [[:effects/save :dissoc-in path]]))

(nxr/register-action! :actions/set-task-status task/set-task-status)
(nxr/register-action! :actions/add-task task/add-task)
(nxr/register-action! :actions/expand-task task/expand-task)
(nxr/register-action! :actions/collapse-task task/collapse-task)

(nxr/register-action! :actions/open-new-task-form
  (fn [_ status]
    [[:actions/assoc-in [:transient status :add?] true]]))

(nxr/register-action! :actions/close-new-task-form
  (fn [_ status]
    [[:actions/dissoc-in [:transient status :add?]]]))

(nxr/register-action! :actions/flash
  (fn [_ ms path v]
    [[:actions/assoc-in path v]
     [:actions/delay ms
      [[:actions/dissoc-in path]]]]))

#?(:cljs
   (nxr/register-placeholder! :event/form-data
     (fn [{:replicant/keys [^js dom-event]}]
       (some-> dom-event .-target forms/gather-form-data))))

(nxr/register-placeholder! :random/id (fn [_] (id/random-id)))

(nxr/register-system->state!
 (fn [store]
   (assoc @store :now #?(:cljs (js/Date.) :clj (java.util.Date.)))))
