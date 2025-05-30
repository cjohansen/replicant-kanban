(ns kanban.http
  (:require [cljs.reader :as reader]))

(defn make-http-request [store path send receive handle-actions payload & [{:keys [on-success]}]]
  (swap! store send (js/Date.) payload)
  (-> (js/fetch path #js {:method "POST"
                          :body (pr-str payload)})
      (.then #(.text %))
      (.then reader/read-string)
      (.then (fn [res]
               (swap! store receive (js/Date.) payload res)
               (when on-success
                 (handle-actions on-success))))
      (.catch #(swap! store receive (js/Date.) payload {:error (.-message %)}))))

(defn connect-event-source [store path param send receive payload]
  (swap! store send (js/Date.) payload)
  (let [event-source (js/EventSource.
                      (str path "?" param "=" (js/encodeURIComponent (pr-str payload))))]
    (.addEventListener
     event-source "message"
     (fn [e]
       (swap! store receive (js/Date.) payload (reader/read-string (.-data e)))))))
