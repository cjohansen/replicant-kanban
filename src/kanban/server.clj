(ns kanban.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.core.protocols :as protocols]
            [ring.middleware.params]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :as response])
  (:import (java.net URLDecoder)
           (java.nio.charset StandardCharsets)))

(def db
  (atom
   {:tasks
    [{:task/id #uuid "98f1dcf4-c539-4f14-9c62-2a4a2c408d4b"
      :task/status :status/open
      :task/title "Add dark mode toggle"
      :task/tags [:tags/feature :tags/theme]
      :task/priority :priority/medium
      :task/created-at #inst "2025-04-30T09:00:00.000Z"
      :task/description "Introduce a toggle to switch between light and dark themes for the board interface."}

     {:task/id #uuid "2b9d9dc0-5d99-4ae5-b2b1-993c2c41d676"
      :task/status :status/open
      :task/title "Auto-archive old closed tasks"
      :task/tags [:tags/feature :tags/automation]
      :task/priority :priority/low
      :task/created-at #inst "2025-05-01T10:15:00.000Z"
      :task/description "Automatically archive tasks that have been in the closed column for more than 30 days."}

     {:task/id #uuid "b4e68c57-6fc4-4e87-b0c7-c5d7d80e0c47"
      :task/status :status/open
      :task/title "Add quick-add form for new cards"
      :task/tags [:tags/feature :tags/ui]
      :task/priority :priority/high
      :task/created-at #inst "2025-04-29T13:00:00.000Z"
      :task/description "Implement a small form at the top of the board for quickly creating new tasks."}

     {:task/id #uuid "3de54672-d270-421e-a2e3-f16fef1d3dc1"
      :task/status :status/open
      :task/title "Keyboard shortcut for moving cards"
      :task/tags [:tags/feature :tags/accessibility]
      :task/priority :priority/medium
      :task/created-at #inst "2025-05-03T14:45:00.000Z"
      :task/description "Allow cards to be moved left or right between columns using keyboard shortcuts."}

     {:task/id #uuid "ce6a5b1e-62f4-4a90-b899-bbca097da1a1"
      :task/status :status/open
      :task/title "Export board to JSON"
      :task/tags [:tags/feature :tags/data]
      :task/priority :priority/low
      :task/created-at #inst "2025-04-26T08:00:00.000Z"
      :task/changed-status-at #inst "2025-04-27T16:00:00.000Z"
      :task/description "Provide a way to export the current board and task data as a JSON file for backup or import."}

     {:task/id #uuid "f68deba2-4169-4dfc-b204-d88887b0e6cf"
      :task/status :status/open
      :task/title "Animated column transitions"
      :task/tags [:tags/ui :tags/animation]
      :task/priority :priority/medium
      :task/created-at #inst "2025-04-23T15:25:00.000Z"
      :task/changed-status-at #inst "2025-04-25T10:30:00.000Z"
      :task/description "Smoothly animate column changes and card movements for improved visual feedback."}]
    }))

(defn get-query-results [state query]
  (case (:query/kind query)
    :queries/tasks
    {:success? true
     :result (:tasks state)}

    {:error "Unknown query type"
     :query query}))

(defn query [req]
  (if-let [query (try
                   (read-string (slurp (:body req)))
                   (catch Exception e
                     (println "Failed to parse query body")
                     (prn e)))]
    (try
      (get-query-results @db query)
      (catch Exception e
        (println "Failed to handle query")
        (prn e)
        {:error "Failed while executing query"}))
    {:error "Unparsable query"}))

(defn ->new-task [task]
  (merge
   {:task/id (random-uuid)
    :task/created-at (java.util.Date.)
    :task/status :status/open
    :task/priority :priority/medium}
   (select-keys task [:task/id :task/created-at :task/title :task/status
                      :task/description :task/priority :task/tags])))

(defn create-task [task]
  (if (:task/title task)
    (do
      (swap! db update :tasks conj (->new-task task))
      {:success? true})
    {:success? false}))

(defn set-task-status [{:task/keys [id status]}]
  (swap! db update :tasks (fn [tasks]
                            (for [task tasks]
                              (cond-> task
                                (= id (:task/id task))
                                (assoc :task/status status)))))
  {:success? true})

(defn handle-command [req]
  (if-let [command (try
                   (read-string (slurp (:body req)))
                   (catch Exception e
                     (println "Failed to parse command body")
                     (prn e)))]
    (try
      (case (:command/kind command)
        :commands/create-task
        (create-task (:command/data command))

        :commands/set-task-status
        (set-task-status (:command/data command))

        {:error "Unknown command type"
         :command command})
      (catch Exception e
        (println "Failed to handle command")
        (prn e)
        {:error "Failed while handling command"}))
    {:error "Unparsable command"}))

(defn write-event [id out data]
  (try
    (.write out (-> (str "data: " (pr-str data) "\n\n")
                    (.getBytes StandardCharsets/UTF_8)))
    (.flush out)
    (catch java.io.IOException _
      (remove-watch db id))))

(defn stream-queries [respond query]
  (respond
   {:status 200
    :headers {"Content-Type" "text/event-stream"
              "Cache-Control" "no-cache"
              "Connection" "keep-alive"}
    :body (reify
            protocols/StreamableResponseBody
            (write-body-to-stream [_ _ out]
              (let [id (random-uuid)]
                (add-watch
                 db id
                 (fn [_ _ _ new-state]
                   (write-event id out (get-query-results new-state query))))
                (write-event id out (get-query-results @db query)))))}))

(defn handler [{:keys [uri] :as req} respond raise]
  (try
    (cond
      (= "/" uri)
      (respond (response/resource-response "/index.html" {:root "public"}))

      (= "/query" uri)
      (respond
       {:status 200
        :headers {"content-type" "application/edn"}
        :body (pr-str (query req))})

      (= "/query/subscribe" uri)
      (stream-queries respond (read-string (URLDecoder/decode (get (:params req) "query"))))

      (= "/command" uri)
      (respond
       {:status 200
        :headers {"content-type" "application/edn"}
        :body (pr-str (handle-command req))})

      :else
      (respond
       {:status 404
        :headers {"content-type" "text/html"}
        :body "<h1>Page not found</h1>"}))
    (catch Exception e
      (raise e))))

(defn start-server [port]
  (jetty/run-jetty
   (-> #'handler
       (wrap-resource "public")
       (ring.middleware.params/wrap-params))
   {:port port
    :join? false
    :async? true}))

(defn stop-server [server]
  (.stop server))

(comment

  (def server (start-server 8088))
  (stop-server server)

)
