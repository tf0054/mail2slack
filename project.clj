(defproject tf0054/mail2slack "0.1.0"
  :description "SMTP to Slack.com"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [rodnaph/tigger "0.4.0"]
                 [http-kit "2.1.18"]
                 [com.taoensso/timbre "3.3.1"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [clj-time "0.9.0"]
                 [environ "1.0.0"]
                 [javax.mail/mail "1.4"]]
  :daemon {:mail2slack {:ns mail2slack.main
                        :pidfile "mail2slack.pid"}}
  :plugins [[lein-daemon "0.5.4"]]
  :main mail2slack.main)

