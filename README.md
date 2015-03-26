# clarin-cleanup

A tool to clean Clarin data to assign correct language to text entries and do some garbage cleaning.

## Usage

There are a few scripts to get the data and setup dependencies:

* bin/download - will get EU parliament ontology data in turtle format saving it to resources/ttls 
* bin/lang-install - *language-detection* lib does not have Maven artifacts, install it manually

Once in REPL (for those not into Clojure, you can easily install [Leiningen](http://leiningen.org/) to get the REPL)

```
(use 'clarin-cleanup.core)

;convert one file
(fix-ttl "resources/ttls/Slovenian.ttl")

;convert the whole dir
(fix-all "resources/ttls")

```

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
