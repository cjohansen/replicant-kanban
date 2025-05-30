(ns kanban.query)

(defn take-until [f xs]
  (loop [res []
         xs (seq xs)]
    (cond
      (nil? xs) res
      (f (first xs)) (conj res (first xs))
      :else (recur (conj res (first xs)) (next xs)))))

(defn add-log-entry [log entry]
  (cond->> (cons entry log)
    (= :query.status/success (:query/status entry))
    (take-until #(= (:query/status %) :query.status/loading))))

(defn send-request [state now query]
  (update-in state [::log query] add-log-entry
             {:query/status :query.status/loading
              :query/user-time now}))

(defn ^{:indent 3} receive-response [state now query response]
  (update-in state [::log query] add-log-entry
             (cond-> {:query/status (if (:success? response)
                                      :query.status/success
                                      :query.status/error)
                      :query/user-time now}
               (:success? response)
               (assoc :query/result (:result response)))))

(defn get-log [state query]
  (get-in state [::log query]))

(defn get-latest-status [state query]
  (:query/status (first (get-log state query))))

(defn loading? [state query]
  (= :query.status/loading
     (get-latest-status state query)))

(defn available? [state query]
  (->> (get-log state query)
       (some (comp #{:query.status/success} :query/status))
       boolean))

(defn error? [state query]
  (= :query.status/error
     (get-latest-status state query)))

(defn get-result [state query]
  (->> (get-log state query)
       (keep :query/result)
       first))

(defn requested-at [state query]
  (->> (get-log state query)
       (drop-while #(not= :query.status/loading (:query/status %)))
       first
       :query/user-time))
