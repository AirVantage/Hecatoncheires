(defproject hecatoncheires "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [;; Chestnut ---------------------------------------------------
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89" :scope "provided"]
                 [com.cognitect/transit-clj "0.8.285"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.2.0"]
                 [bk/ring-gzip "0.1.1"]
                 [ring.middleware.logger "0.5.0"]
                 [compojure "1.5.0"]
                 [environ "1.0.3"]
                 [http-kit "2.1.19"]
                 ;; Server -----------------------------------------------------
                 [amazonica "0.3.85" :exclusions [cheshire]]
                 [aws-arn "1.0.2"]
                 [tentacles "0.5.1" :exclusions [cheshire
                                                 clj-http]]
                 [cheshire "5.7.0"]
                 [clj-http "2.3.0"]
                 [com.cognitect/transit-clj "0.8.297"]
                 [com.datomic/datomic-free "0.9.5554"
                  :exclusions [org.slf4j/slf4j-nop
                               org.slf4j/log4j-over-slf4j
                               com.google.guava/guava]]
                 [org.clojure/core.async "0.2.395"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.google.guava/guava "19.0"]
                 ;; Client -----------------------------------------------------
                 [org.omcljs/om "1.0.0-alpha48"]
                 [compassus "1.0.0-alpha2"]
                 [sablono "0.7.7"]
                 [cljs-http "0.1.42"]
                 [prismatic/dommy "1.1.0"]
                 [cljsjs/jquery "2.2.4-0"]
                 [cljsjs/bootstrap "3.3.6-1"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-environ "1.0.3"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]

  :test-paths ["test/clj" "test/cljc"]

  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/js"]

  :uberjar-name "hecatoncheires.jar"

  ;; Use `lein run` if you just want to start a HTTP server, without figwheel
  :main hecatoncheires.core

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (run) and
  ;; (browser-repl) live.
  :repl-options {:init-ns user}

  :cljsbuild {:builds
              [{:id "app"
                :source-paths ["src/cljs" "src/cljc"]

                :figwheel true
                ;; Alternatively, you can configure a function to run every time figwheel reloads.
                ;; :figwheel {:on-jsload "hecatoncheires.core/on-figwheel-reload"}

                :compiler {:main hecatoncheires.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/hecatoncheires.js"
                           :output-dir "resources/public/js/compiled/out"
                           :parallel-build true
                           :source-map-timestamp true}}

               {:id "test"
                :source-paths ["src/cljs" "test/cljs" "src/cljc" "test/cljc"]
                :compiler {:output-to "resources/public/js/compiled/testable.js"
                           :main hecatoncheires.test-runner
                           :parallel-build true
                           :optimizations :none}}

               {:id "min"
                :source-paths ["src/cljs" "src/cljc"]
                :jar true
                :compiler {:main hecatoncheires.core
                           :output-to "resources/public/js/compiled/hecatoncheires.js"
                           :output-dir "target"
                           :externs ["src/js/externs.js"]
                           :closure-defines {goog.DEBUG false}
                           :parallel-build true
                           :source-map-timestamp true
                           :optimizations :advanced
                           :pretty-print false}}]}

  ;; When running figwheel from nREPL, figwheel will read this configuration
  ;; stanza, but it will read it without passing through leiningen's profile
  ;; merging. So don't put a :figwheel section under the :dev profile, it will
  ;; not be picked up, instead configure figwheel here on the top level.

  :figwheel {;; :http-server-root "public"       ;; serve static assets from resources/public/
             ;; :server-port 3449                ;; default
             ;; :server-ip "127.0.0.1"           ;; default
             :css-dirs ["resources/public/css"]  ;; watch and update CSS

             ;; Instead of booting a separate server on its own port, we embed
             ;; the server ring handler inside figwheel's http-kit server, so
             ;; assets and API endpoints can all be accessed on the same host
             ;; and port. If you prefer a separate server process then take this
             ;; out and start the server with `lein run`.
             ;; :ring-handler user/http-handler

             ;; Start an nREPL server into the running figwheel process. We
             ;; don't do this, instead we do the opposite, running figwheel from
             ;; an nREPL process, see
             ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
             ;; :nrepl-port 7888

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             :server-logfile "log/figwheel.log"}

  :doo {:build "test"}

  :profiles {:dev
             {:dependencies [[figwheel "0.5.4-4"]
                             [figwheel-sidecar "0.5.4-4"]
                             [com.cemerick/piggieback "0.2.1"]
                             [org.clojure/tools.nrepl "0.2.12"]]

              :plugins [[lein-figwheel "0.5.4-4"]
                        [lein-doo "0.1.6"]]

              :source-paths ["dev"]
              :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

             :uberjar
             {:source-paths ^:replace ["src/clj" "src/cljc"]
              :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
              :hooks []
              :omit-source true
              :aot :all}})
