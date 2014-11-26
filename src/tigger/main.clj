(ns tigger.main
  (:require [tigger.core :refer [listen]]
            [tigger.http :as http]
            [clojure.core.async :refer [<!!]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]])
  (:use [clojure.pprint])
  (:import [java.util Properties]
           [java.io ByteArrayInputStream]
           [javax.mail Session]
           [javax.mail.internet MimeMessage MimeUtility]
           [javax.mail.internet InternetAddress]))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn sendSlackItemCB [status res]
  (timbre/info "Status:" status ", Response:" res))

(defn sendSlackPost [strUrl date from subj body]
  (let [subject (str "*" subj "*")]
    (http/postItem strUrl
                   {:username "MailGhost"
                    :text (str "From:" from "  (" date ")\n" subject "\n```" body "\n```")
                    :icon_emoji ":ghost:"}
                   sendSlackItemCB)))

; from contrib?
(defn as-properties [m]
  (let [p (Properties.)]
    (doseq [[k v] m]
      (.setProperty p (str k) (str v))) p))

; ???
(def session
    (Session/getDefaultInstance
    (as-properties [["mail.store.protocol" "imaps"]])))

; MimeMessage
(defn get-message [x]
    ;(bean
     (MimeMessage. session (ByteArrayInputStream. (.getBytes x))))

; https://github.com/owainlewis/clojure-mail/blob/master/src/clojure_mail/message.clj
(defn- multipart? [m]
  (.startsWith (.getContentType m) "multipart"))

(defn- read-multi [mime-multi-part]
  (let [count (.getCount mime-multi-part)]
    (for [part (map #(.getBodyPart mime-multi-part %) (range count))]
      (if (multipart? part)
        (.getContent part)
        part))))

(defn- message-parts [^javax.mail.internet.MimeMultipart msg]
  (if (multipart? msg)
    (read-multi (.getContent msg))))

(defn -main []
  ;(print "env:" (:env env))
  (let [ch (listen 2500)
        strUrl (-> env :env :slack-webhook-url)]
    (if (nil? strUrl)
      (exit 1 (str "Please make profiles.clj with :slack-webhook-url param!")))
    (while true
      (let [objMsg (<!! ch)
            objMimeMsg (get-message (:body objMsg))]
        (if (multipart? objMimeMsg)
          (sendSlackPost strUrl
                         (.getSentDate objMimeMsg)
                         (MimeUtility/decodeText
                          (InternetAddress/toString (.getFrom objMimeMsg)))
                         (.getSubject objMimeMsg)
                         (apply str (filter (fn [c] (not= c \return))
                          (.getContent (nth (message-parts objMimeMsg) 0)))) )
          (sendSlackPost strUrl
                         (.getSentDate objMimeMsg)
                         (MimeUtility/decodeText
                          (InternetAddress/toString (.getFrom objMimeMsg)))
                         (.getSubject objMimeMsg)
                         (apply str (filter (fn [c] (not= c \return))
                          (.getContent objMimeMsg))) )
        )))))
