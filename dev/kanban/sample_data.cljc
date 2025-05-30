(ns kanban.sample-data)

(def tasks
  [{:task/id #uuid "d8a1fa84-5e33-4ea9-a68b-3fbd6f365731"
    :task/status :status/open
    :task/title "Add keyboard shortcuts for board navigation"
    :task/tags [:tags/feature :tags/accessibility]
    :task/priority :priority/medium
    :task/created-at #inst "2025-04-28T10:15:00.000Z"
    :task/description "Allow users to navigate between columns and cards using arrow keys and hotkeys."}

   {:task/id #uuid "fe61a91c-2262-4f7f-96cc-6cb59bdf730c"
    :task/status :status/open
    :task/title "Support custom tags with colors"
    :task/tags [:tags/feature :tags/ui]
    :task/priority :priority/low
    :task/created-at #inst "2025-05-01T08:30:00.000Z"
    :task/description "Let users define custom tags and assign them a color for visual filtering."}

   {:task/id #uuid "b117de9c-9e4d-4060-b066-83a3cfb5e7b5"
    :task/status :status/in-progress
    :task/title "Add markdown support in descriptions"
    :task/tags [:tags/feature :tags/editor]
    :task/priority :priority/high
    :task/created-at #inst "2025-04-30T14:00:00.000Z"
    :task/description "Enable markdown formatting (bold, italics, links, code) in the issue description field."}

   {:task/id #uuid "21b35698-d679-48a1-a441-2f6c3a190f8f"
    :task/status :status/open
    :task/title "Show card age indicators"
    :task/tags [:tags/feature :tags/ui]
    :task/priority :priority/medium
    :task/created-at #inst "2025-04-27T11:45:00.000Z"
    :task/description "Display a subtle age indicator on each card based on its creation date."}

   {:task/id #uuid "eb5b3cb1-f9b7-4ed4-a5e2-4531c3d7a5c7"
    :task/status :status/closed
    :task/title "Add mobile-friendly layout"
    :task/tags [:tags/feature :tags/responsive-design]
    :task/priority :priority/high
    :task/created-at #inst "2025-04-20T09:10:00.000Z"
    :task/changed-status-at #inst "2025-04-22T09:10:00.000Z"
    :task/description "Ensure the board is usable on smaller screens with responsive column stacking."}

   {:task/id #uuid "0d4a5bb5-78bb-4a79-9ed3-14d23b0b3fa4"
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
