(ns tigger.http
  (:require [clojure.data.json :as json]
            [org.httpkit.client :as http]
            [taoensso.timbre :as timbre]))

(defn postItem [strBaseUrl objContent func]
  ; http://shenfeng.me/async-clojure-http-client.html
  ; http://www.markhneedham.com/blog/2013/09/26/clojure-writing-json-to-a-filereading-json-from-a-file/
  (let [options {:headers {"Content-Type" "application/json"}
                 :keepalive 3000
                 :form-params {"payload" (json/write-str objContent)}
                 }]
    (println (:body options))
    (http/post strBaseUrl options
              ;asynchronous with callback
               (fn [{:keys [status error body headers]}]
                 (if error
                   (timbre/debugf "Failed(%s), exception is %s" status error)
                   (func status body)))
               )))
