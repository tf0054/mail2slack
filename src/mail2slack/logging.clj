(ns mail2slack.logging
  (require
    [clojure.string :as str]
    [taoensso.timbre :as timbre]
    ))

(defn default-fmt-output-fn
  [{:keys [level throwable message timestamp hostname ns]}
       ;; Any extra appender-specific opts:
       & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
     ;; <timestamp> <hostname> <LEVEL> [<ns>] - <message> <throwable>
     (format "%s %s %s [%s] - %s%s"
       timestamp hostname (-> level name str/upper-case) ns (or message "")
       (or (timbre/stacktrace throwable "\n" (when nofonts? {})) "")
             ))

(defn simple-fmt-output-fn
  [{:keys [level throwable message timestamp hostname ns]}
   & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
  (format "%s %s [%s] - %s%s"
    (-> level name str/upper-case) timestamp ns (or message "")
    (or (timbre/stacktrace throwable "\n" (when nofonts? {})) "")))

(def simpler-logging-output-format {
                                     :fmt-output-fn #'simple-fmt-output-fn
                                     :timestamp-pattern "HH:mm:ss"
                                     })

(timbre/merge-config! simpler-logging-output-format)
