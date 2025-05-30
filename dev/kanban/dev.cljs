(ns kanban.dev
  (:require [dataspex.core :as dataspex]
            [kanban.actions :as actions]
            [kanban.core :as kanban]
            [kanban.sample-data :as sample-data]))

(defonce store
  (atom
   {:columns sample-data/columns
    :tags sample-data/tags}))

(defonce el (js/document.getElementById "app"))

(dataspex/inspect "Store" store)

(defn ^:export main []
  (kanban/boot store el)

  ;; Use sample data in the client
  (swap! store assoc :tasks sample-data/tasks)

  ;; Fetch data from the server
  ;; (actions/handle-actions store nil [[:actions/query {:query/kind :queries/tasks}]])

  ;; Stream data from the server
  ;; (actions/handle-actions store nil [[:actions/subscribe-query {:query/kind :queries/tasks}]])

  (swap! store assoc :system/started-at (js/Date.)))

(defn ^:dev/after-load refresh []
  (swap! store assoc :system/refreshed-at (js/Date.)))

(comment
  (set! *print-namespace-maps* false)
  )
