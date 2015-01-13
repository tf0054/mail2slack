(ns mail2slack.main
  (:use [clojure.pprint])
  (:require [clojure.string :as string]
            [clj-time.core :as jodat]
            [tigger.core :refer [listen]]
            [clojure.core.async :refer [<!!]]
            [environ.core :refer [env]]
            [clj-time.coerce :as jodac]
            [taoensso.timbre :as timbre]
            [mail2slack.http :as http]
            [mail2slack.logging])
  (:import [java.util Date]
           [org.joda.time.format DateTimeFormat]
           [java.util Properties]
           [java.io ByteArrayInputStream]
           [javax.mail Session]
           [javax.mail.internet MimeMessage MimeUtility]
           [javax.mail.internet InternetAddress]))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn substring? [sub st]
  (not= (.indexOf st sub) -1))

(defn date2joda [date] (if (instance? Date date) (jodac/from-date date) date))

(defn sendSlackPostCB [x]
  (timbre/info "Status:" (:status x) ", Response:" (:body x) ", From:" (:from x)))

(defn sendSlackPost [strUrl date from subj body]
  (let [subject (str "*" subj "*")
        jstDate (jodat/to-time-zone (date2joda date)
                                    (jodat/time-zone-for-id "Asia/Tokyo"))]
    (http/postItem strUrl
                   {:username "mail2slack"
                    :icon_emoji ":ghost:"
                    :text (str "From:" from "  ("
                               (.toString jstDate (DateTimeFormat/fullDateTime))
                               ")\n" subject "\n```" body "\n```")}
                   sendSlackPostCB
                   {:from from})))

(defn- get-from [m]
  (let [strFrom (InternetAddress/toString (.getFrom m))]
    (if (not (string/blank? strFrom))
      (MimeUtility/decodeText strFrom)
      (timbre/info "No from address cound be gotten:" (.getFrom m))))) ;timbre/info returns nil

(defn- get-tos [m]
  (map str
    (.getRecipients m javax.mail.Message$RecipientType/TO)))

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
(defn- get-message [x]
  (MimeMessage. session (ByteArrayInputStream. (.getBytes x))))

; https://github.com/owainlewis/clojure-mail/blob/master/src/clojure_mail/message.clj
(defn- multipart? [m]
  (.startsWith (.getContentType m) "multipart"))

(defn- read-multi [mime-multi-part]
  (let [count (try (.getCount mime-multi-part)
                (catch Exception e
                  (timbre/warn "Catched exception:" (.getMessage e))))]
    (for [part (map #(.getBodyPart mime-multi-part %) (range count))]
      (if (multipart? part)
        (.getContent part)
        part))))

(defn- message-parts [^javax.mail.internet.MimeMultipart msg]
  (if (multipart? msg)
    (read-multi (.getContent msg))
    msg))

(defn -main []
  ; checking
  (if (nil? (env :env))
    (exit 1 (str "Please make profiles.clj with :slack-webhook-url param!")))
  ;
  (let [ch (listen (-> env :env :mail2slack-port))
        strUrl (-> env :env :slack-webhook-url)]
    (timbre/info "Starting listening on:" (-> env :env :mail2slack-port))
    (.addShutdownHook ; needs "lein trampoline run" why?
     (Runtime/getRuntime)
     (Thread. #(timbre/info "Shutdowning successfully.")))
    ;
    (while true
      (let [objMsg (<!! ch)
            objMimeMsg (get-message (:body objMsg))
            strFrom (get-from objMimeMsg)
            strRcpts (get-tos objMimeMsg)]
        ;
        (if (nil? strFrom) ; is this needed?
          (timbre/info "Invalid From: nil"))
        (if (every? false? (map #(or
                                  (substring? "support@" %)
                                  (substring? "tf0054@" %)) strRcpts))
          (timbre/info "Invalid Rcpts:" (clojure.string/join "," strRcpts)))
        ;
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
                                            (.getContent objMimeMsg))) )) ))))

