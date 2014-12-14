
# mail2slack

A smtp server which converts emails to [slack](http://slack.com/) msg via it's webhook interface.
Made with [Tigger](https://github.com/rodnaph/tigger).

## Usage

You can start mail2slack as a daemon via `lein daemon start mail2slack`.

```
[root@ip-xxx mail2slack]# lein daemon start mail2slack
WARNING: You're currently running as root; probably by accident.
Press control-C to abort or Enter to continue as root.
Set LEIN_ROOT to disable this warning.

Retrieving lein-daemon/lein-daemon/0.5.4/lein-daemon-0.5.4.pom from clojars
Retrieving lein-daemon/lein-daemon/0.5.4/lein-daemon-0.5.4.jar from clojars
pid not present, starting
waiting for pid file to appear at mail2slack.pid
mail2slack started
[root@ip-xxx mail2slack]#
```

Some parameters like the port number should be set by [environ](https://github.com/weavejester/environ).
It is deprecated, but You can define requisite params in **.lein-env** placed at the project directory.

```
[root@ip-xxx mail2slack]# cat .lein-env
{:env{
:mail2slack-port 2500
:slack-webhook-url "https://hooks.slack.com/services/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"
}}
[root@ip-xxx mail2slack]#
```

The [lein-daemon](https://github.com/arohner/lein-daemon) is used for deamonize. So you can find log file and pid file at the project directory.

So far addresses for receiving emails are hard coded on [main.clj](https://github.com/tf0054/mail2slack/blob/master/src/mail2slack/main.clj).
