(ns lib-compojure.core)

(defn message [success pmessage data & [detailMessage ptype]]
  (let [pmessage (or pmessage "")
        data (or data {})
        detailMessage (or detailMessage "")
        ptype (or ptype (if success :success :error))]
    {:success success
     :message pmessage
     :data data 
     :detailMessage detailMessage
     :type ptype}))

(defn success-message [pmessage & [data detailMessage]]
  (message true pmessage data detailMessage))

(defn failture-message [pmessage & [data detailMessage]]
  (message false pmessage data detailMessage))

(def error-message failture-message)

(defn info-message [pmessage & [data detailMessage]]
  (message true pmessage data detailMessage :info))
