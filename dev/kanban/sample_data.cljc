(ns kanban.sample-data
  (:require [kanban.id :as id]))

(def tasks
  [{:task/id (id/random-id)
    :task/status :status/open
    :task/title "Add keyboard shortcuts for board navigation"
    :task/tags [:tags/feature :tags/accessibility]
    :task/priority :priority/medium
    :task/created-at #inst "2025-04-28T10:15:00.000Z"
    :task/description "Allow users to navigate between columns and cards using arrow keys and hotkeys."}

   {:task/id (id/random-id)
    :task/status :status/open
    :task/title "Support custom tags with colors"
    :task/tags [:tags/feature :tags/ui]
    :task/priority :priority/low
    :task/created-at #inst "2025-05-01T08:30:00.000Z"
    :task/description "Let users define custom tags and assign them a color for visual filtering."}

   {:task/id (id/random-id)
    :task/status :status/in-progress
    :task/title "Add markdown support in descriptions"
    :task/tags [:tags/feature :tags/editor]
    :task/priority :priority/high
    :task/created-at #inst "2025-04-30T14:00:00.000Z"
    :task/description "Enable markdown formatting (bold, italics, links, code) in the issue description field."}

   {:task/id (id/random-id)
    :task/status :status/open
    :task/title "Show card age indicators"
    :task/tags [:tags/feature :tags/ui]
    :task/priority :priority/medium
    :task/created-at #inst "2025-04-27T11:45:00.000Z"
    :task/description "Display a subtle age indicator on each card based on its creation date."}

   {:task/id (id/random-id)
    :task/status :status/closed
    :task/title "Add mobile-friendly layout"
    :task/tags [:tags/feature :tags/responsive-design]
    :task/priority :priority/high
    :task/created-at #inst "2025-04-20T09:10:00.000Z"
    :task/changed-status-at #inst "2025-04-22T09:10:00.000Z"
    :task/description "Ensure the board is usable on smaller screens with responsive column stacking."}

   {:task/id (id/random-id)
    :task/status :status/closed
    :task/title "Implement zoom-in view for cards"
    :task/tags [:tags/feature :tags/ui]
    :task/priority :priority/high
    :task/created-at #inst "2025-05-02T12:20:00.000Z"
    :task/changed-status-at #inst "2025-04-25T09:10:00.000Z"
    :task/description "Clicking a card opens a detailed view showing all metadata, tags, and the full description."}])

(def columns
  [{:column/status :status/open
    :column/title "Todo"
    :column/add-new? true}
   {:column/status :status/in-progress
    :column/title "WIP"
    :column/limit 2}
   {:column/status :status/closed
    :column/sort-by :task/changed-status-at
    :column/sort-order :desc
    :column/title "Done"}])

(def tags
  [{:tag/ident :tags/feature
    :tag/style :primary}
   {:tag/ident :tags/accessibility
    :tag/style :secondary}
   {:tag/ident :tags/ui
    :tag/style :accent}
   {:tag/ident :tags/editor
    :tag/style :info}
   {:tag/ident :tags/responsive-design
    :tag/style :success}])
