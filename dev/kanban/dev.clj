(ns kanban.dev
  (:require [clj-reload.core :as clj-reload]
            [kanban.server :as server]))

(def server (atom nil))

(defn start []
  (when-not @server
    (let [port 8088]
      (reset! server (server/start-server port))
      (println (str "Started server on http://localhost:" port)))))

(defn stop []
  (some-> @server server/stop-server))

(defn reset []
  (stop)
  (clj-reload/reload)
  ((requiring-resolve `start)))

(comment ;; s-:
  (start)
  (stop)
  (reset)
  (set! *print-namespace-maps* false)
  )
