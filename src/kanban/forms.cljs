(ns kanban.forms)

(defn get-input-value [^js element]
  (cond
    (= "number" (.-type element))
    (when (not-empty (.-value element))
      (.-valueAsNumber element))

    (= "number" (aget (.-dataset element) "type"))
    (when (not-empty (.-value element))
      (parse-long (.-value element)))

    (= "boolean" (aget (.-dataset element) "type"))
    (= "true" (.-value element))

    (= "checkbox" (.-type element))
    (if (.hasAttribute element "value")
      (when (.-checked element)
        (.-value element))
      (.-checked element))

    (= "keyword" (aget (.-dataset element) "type"))
    (keyword (.-value element))

    :else
    (.-value element)))

(defn get-input-key [^js element]
  (when-let [k (some-> element .-name not-empty keyword)]
    (when (or (not= "checkbox" (.-type element))
              (.-checked element)
              (not (.hasAttribute element "value")))
      k)))

(defn gather-form-input-data [form-inputs]
  (some-> (into-array form-inputs)
          (.reduce
           (fn [res ^js el]
             (let [k (get-input-key el)]
               (cond-> res
                 k (assoc k (get-input-value el)))))
           {})))

(defn gather-form-data [^js form-el]
  (gather-form-input-data (.-elements form-el)))
