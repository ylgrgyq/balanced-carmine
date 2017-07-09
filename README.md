# carmine-with-hash-key

In some cases you need several redis servers to share your load. If you can use redis cluster, go for it. It's your best choice.
But the real world is harsh. Maybe you can't use redis cluster dut to some reason and you don't want to sandwich any proxy
 between redis servers and your client. You just want a simple redis client side to distribute client request to several
 redis servers. In this scenario, carmine-with-hash-key client is right for you.

## Usage

```
[carmine-with-hash-key "0.1.0-RC1"]
```

1. require carmine-with-hash-key and carmine:

```
(ns my-app
  (:require [taoensso.carmine :as car]
            [carmine-with-hash-key.core :as bc]))
```

The only difference compares with carmine is that we will use carmine-with-hash-key.core/wcar to replace taoensso.carmine/wcar.

2. configure redis specs group. We will dispatch redis client's requests to these redis servers later:

```
(def specs [{:host "127.0.0.1" :port 6379}
            {:host "127.0.0.1" :port 6380}
            {:host "127.0.0.1" :port 6381}])
```

3. To use carmine in a clean way, we need to define wcar*, just as you did when using taoensso.carmine:
```
(defmacro wcar*
    [& body]
    `(bc/wcar {:pool pool
               :specs-group specs}
              ~@body))
```

4. Let wcar* leads your redis commands with any hash key. carmine-with-hash-key client will use this hash key to figure out which
redis server to send requests to. carmine-with-hash-key client insures that redis commands with the same hash key will send to the
 same redis server.
```
(wcar* "I'm a hash key"
       (car/set "a" 100)
       (car/get "a"))
```

Of course, you can use wcar* without hash key, then carmine-with-hash-key client will send request to the first redis server in specs-group.
```
(wcar* (car/set "a" 100)
       (car/get "a"))
```

If you need to use :as-pipeline, please add :as-pipeline after hash key:
```
(wcar* "I'm a hash key"
       :as-pipeline
       (car/set "a" 100)
       (car/get "a"))
```

## License

Copyright © 2017 ylgrgyq

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
