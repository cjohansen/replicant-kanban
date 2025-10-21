(ns kanban.scenes
  (:require [kanban.actions]
            [kanban.pages.board-scenes]
            [kanban.ui.card-scenes]
            [kanban.ui.column-scenes]
            [kanban.ui.elements-scenes]
            [portfolio.data :as data]
            [portfolio.ui :as portfolio]
            [replicant.dom :as r]))

:kanban.actions/keep
:kanban.pages.board-scenes/keep
:kanban.ui.card-scenes/keep
:kanban.ui.column-scenes/keep
:kanban.ui.elements-scenes/keep

(data/register-collection!
 :kanban.ui
 {:title "UI elements"
  :idx 0})

(data/register-collection!
 :kanban.pages
 {:title "Page scenes"
  :idx 1})

;; Setup

(def light-theme
  {:background/background-color "#fff"
   :background/document-class "light"
   :background/document-data {:theme "light"}})

(def dark-theme
  {:background/background-color "#1e2329"
   :background/document-class "dark"
   :background/document-data {:theme "dark"}})

(defn main []
  (r/set-dispatch! #(prn %2))

  (portfolio/start!
   {:config
    {:css-paths ["/styles.css"]

     :background/options
     [{:id :light
       :title "Light"
       :value light-theme}
      {:id :dark
       :title "Dark"
       :value dark-theme}]

     :canvas/layout {:kind :rows
                     :xs [light-theme
                          dark-theme]}}}))
