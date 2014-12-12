
# mail2slack

A smtp server which converts emails to slack msg via slack's webhook interface.
Made with [Tigger](https://github.com/rodnaph/tigger).

## Usage

The address for receiving emails are hard coded on main.clj.
Some parameters like the port number should be set by [environ](https://github.com/weavejester/environ)


```
nohup lein run
```

