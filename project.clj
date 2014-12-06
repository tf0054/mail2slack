(defproject tf0054/mail2slack "0.1.0"
  :description "SMTP to Slack.com"
  :dependencies [[org.clojure/clojure "1.6.0"]
		 [rodnaph/tigger "0.4.0"]
                 [http-kit "2.1.18"]
                 [com.taoensso/timbre "3.2.1"]
                 [org.clojure/data.json "0.2.4"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.subethamail/subethasmtp "3.1.7"]
                 [environ "1.0.0"]
                 [javax.mail/mail "1.4"]]
  :main mail2slack.main)

