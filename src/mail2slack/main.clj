(ns mail2slack.main
  (:require [tigger.core :refer [listen]]
            [clojure.core.async :refer [<!!]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [mail2slack.http :as http])
  (:use [clojure.pprint])
  (:import [java.util Properties]
           [java.io ByteArrayInputStream]
           [javax.mail Session]
           [javax.mail.internet MimeMessage MimeUtility]
           [javax.mail.internet InternetAddress]))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn substring? [sub st]
  (not= (.indexOf st sub) -1))

(defn sendSlackPostCB [x]
  (timbre/info "Status:" (:status x) ", Response:" (:body x) ", From:" (:from x)))

(defn sendSlackPost [strUrl date from subj body]
  (let [subject (str "*" subj "*")]
    (http/postItem strUrl
                   {:username "mail2slack"
                    :icon_emoji ":ghost:"
                    :text (str "From:" from "  (" date ")\n" subject "\n```" body "\n```")}
                   sendSlackPostCB
                   {:from from})))

; from contrib?
(defn as-properties [m]
  (let [p (Properties.)]
    (doseq [[k v] m]
      (.setProperty p (str k) (str v))) p))

; ???
(def session
  (Session/getDefaultInstance
    (as-properties [["mail.store.protocol" "imaps"]])))

(defn- get-to [m]
  (map str
    (.getRecipients m javax.mail.Message$RecipientType/TO)))

; MimeMessage
(defn- get-message [x]
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
    (read-multi (.getContent msg))
    msg))

(defn -main []
  ;(print "env:" (:env env))
  (let [ch (listen (-> env :env :mail2slack-port))
        strUrl (-> env :env :slack-webhook-url)]
    (if (nil? strUrl)
      (exit 1 (str "Please make profiles.clj with :slack-webhook-url param!")))
    ;
    (timbre/info "Starting listening on:" (-> env :env :mail2slack-port))
    (while true
      (let [objMsg (<!! ch)
            objMimeMsg (get-message (:body objMsg))
            strFrom (MimeUtility/decodeText
                     (InternetAddress/toString (.getFrom objMimeMsg)))
            strRcpts (get-to objMimeMsg)]
        (if (not (every? false? (map #(or
                                       (substring? "support@" %)
                                       (substring? "tf0054@" %)) strRcpts)))
          (if (multipart? objMimeMsg)
            (sendSlackPost strUrl
                           (.getSentDate objMimeMsg)
                           strFrom
                           (.getSubject objMimeMsg)
                           (apply str (filter (fn [c] (not= c \return))
                                              (.getContent (nth (message-parts objMimeMsg) 0)))) )
            (sendSlackPost strUrl
                           (.getSentDate objMimeMsg)
                           strFrom
                           (.getSubject objMimeMsg)
                           (apply str (filter (fn [c] (not= c \return))
                                              (.getContent objMimeMsg))) ))
          (timbre/info "Invalid Rcpts:" (clojure.string/join "," strRcpts)) )))))