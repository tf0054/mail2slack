
# mail2slack

A smtp server which converts emails to [slack](http://slack.com/) msg via slack's webhook interface.
Made with [Tigger](https://github.com/rodnaph/tigger).

## Usage

The address for receiving emails are hard coded on main.clj.
Some parameters like the port number should be set by [environ](https://github.com/weavejester/environ)


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

The [lein-daemon](https://github.com/arohner/lein-daemon) is used for deamonize. The log can be seen on the project directory.
