(defproject clarin-cleanup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles  {:dev {:dependencies  [[midje "1.6.3"]]}}
  :dependencies [[org.clojars.quoll/turtle "0.2.2"]
                 [org.apache.commons/commons-lang3 "3.3.2"]
                 [langdetect "20140303" ]
                 [net.arnx/jsonic "1.2.9"]
                 [org.apache.tika/tika-core "1.7"]
                 [org.clojure/clojure "1.6.0"]])
