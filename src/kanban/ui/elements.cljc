(ns kanban.ui.elements
  (:require [kanban.actions :as actions]
            [phosphor.icons :as icons]
            [replicant.alias :refer [defalias]]))

(def badge-styles
  {:primary "badge-primary"
   :secondary "badge-secondary"
   :accent "badge-accent"
   :info "badge-info"
   :success "badge-success"
   :warning "badge-warning"
   :error "badge-error"})

(defalias badge [{::keys [style] :as attrs} body]
  [:li.badge.badge-soft (assoc attrs :class (badge-styles style))
   body])

(defalias badges [attrs xs]
  (into [:ul.flex.gap-2 attrs] xs))

(defalias card-title [attrs body]
  [:h2.text-base.font-bold.flex.gap-2.items-start attrs body])

(defalias card-details [attrs body]
  [:div.text-base attrs body])

(defalias card-action [attrs body]
  (into [:div.absolute.top-0.right-0.m-4 attrs] body))

(defn augment-event [action]
  (fn [handler]
    (if (or (nil? handler) (fn? handler))
      (fn [e]
        (actions/handle-actions nil {:replicant/dom-event e} nil [action])
        (when (fn? handler)
          (handler e)))
      (concat handler [action]))))

(defalias card [attrs body]
  [:article.card.shadow-sm.bg-base-100.relative
   (cond-> (assoc attrs :draggable true)
     (::expanded? attrs) (assoc-in [:style :transform] "scale(1.2")
     (::expanded? attrs) (update :class concat [:border :z-5])
     :then (update-in [:on :dragstart] (augment-event [:actions/start-drag-move]))
     :then (update-in [:on :dragend] (augment-event [:actions/end-drag-move])))
   (into [:div.card-body.flex.flex-col.gap-4] body)])

(defalias column [attrs body]
  (into [:section.column.min-h-full.flex.flex-col.basis-full.gap-4 attrs] body))

(defalias column-body [attrs body]
  (into [:div.column-body.rounded-lg.p-6.flex.flex-col.gap-4
         (assoc-in attrs [:on :dragover]
                   (fn [#?(:cljs ^js e :clj e)]
                     (.preventDefault e)
                     (set! (.-dropEffect (.-dataTransfer e)) "move")))]
        body))

(defalias button [attrs body]
  (into [:button.btn
         (cond-> attrs
           (and (= 1 (count body))
                (= ::icon (ffirst body)))
           (update :class conj :btn-square))] body))

(def w->h
  {:w-4 :h-4
   :w-6 :h-6
   :w-8 :h-8
   :w-12 :h-12
   :w-16 :h-16})

(defalias icon [attrs [icon]]
  (let [h (w->h (::size attrs))]
    (icons/render icon (update attrs :class concat (if h
                                                     [(::size attrs) h]
                                                     [:w-6 :h-6])))))

(defalias toggle-button [attrs [on off]]
  [:button.btn.btn-square
   (cond-> attrs
     (::on? attrs)
     (update :class conj :toggle-on))
   [icon {::size :w-4 :class #{:toggle-to-on}} on]
   [icon {::size :w-4 :class #{:toggle-to-off}} off]])

(defn ^{:indent 1} alert [attrs content]
  [:div.alert.alert-soft.duration-250.wiggle-in
   (assoc attrs :replicant/unmounting {:class :opacity-0})
   content
   (when-let [actions (::actions attrs)]
     [:button.cursor-pointer.justify-self-end
      {:on {:click actions}}
      [icon {::size :w-4} (icons/icon :phosphor.regular/x)]])])

(defalias modal [attrs content]
  [:dialog.modal {:open "open"}
   [:div.modal-box attrs
    content]])

(defalias text-input [attrs]
  [:input.input
   (cond-> attrs
     (::autofocus? attrs)
     (assoc :replicant/on-mount
            (fn [{:replicant/keys [node]}]
              (.focus node))))])
