(ns tigger.main
  (:require [tigger.core :refer [listen]]
            [tigger.http :as http]
            [clojure.core.async :refer [<!!]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            ))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn trimReturnBeforeSubject [^CharSequence s]
  (loop [index -1]
    (let [len (.length s)
          ch (.charAt s (inc index))]
      (if (or (= ch \newline) (= ch \return))
        (recur (inc index))
        (.. s (subSequence (+ index 1) len) toString)))))

(defn splitSubjectBody [x]
  (let [body (:body x)
        lines (clojure.string/split-lines body)
        strSubject (nth (filter #(.startsWith % "Subject: ") lines) 0)]
    {:subject (subs strSubject (.length "Subject: "))
     :body (clojure.string/trim-newline (trimReturnBeforeSubject (subs body (+
                       (.indexOf body strSubject)
                       (.length strSubject)))))}
    ))

(defn sendSlackItemCB [status res]
  (timbre/info "Status:" status ", Response:" res))

(defn sendSlackPost [strUrl from subj body]
  (let [subject (str "*" subj "*")]
    (http/postItem strUrl
                   {:username "MailGhost"
                    :text (str from "\n" subject "\n```" body "\n```")
                    :icon_emoji ":ghost:"}
                   sendSlackItemCB)))

(defn -main []
 ; (print "env:" (:env config))
  (let [ch (listen 2500)
        strUrl (-> env :env :slack-webhook-url)]
    (if (nil? strUrl)
      (exit 1 (str "Please make profiles.clj with :slack-webhook-url param!")))
    (while true
      (let [objMsg (<!! ch)
            objSB (splitSubjectBody objMsg)]
        (println (pr-str objMsg))
        ;(println objMsg)
        (sendSlackPost strUrl (:from objMsg) (:subject objSB) (:body objSB))
        ))))
