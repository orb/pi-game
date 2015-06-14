(defproject pi-game "0.1.0-SNAPSHOT"
  :description "The PI game"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [compojure "1.3.4"]
                 [hiccup "1.0.5"]
                 [ring-edn "0.1.0"]

                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 ;; for clojurescript
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.omcljs/om "0.8.8"]

                 [sablono "0.3.4"]
                 [cljs-ajax "0.3.12"]
                 [prismatic/dommy "1.1.0"]

                 ^:replace [org.clojure/tools.nrepl "0.2.10"]]

  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-ring "0.9.5" :exclusions [[cider/cider-nrepl]]]
            [lein-figwheel "0.3.3" :exclusions [[org.codehaus.plexus/plexus-utils]
                                                [org.clojure/clojure]
                                                [cider/cider-nrepl]]]]

  :ring {:handler pi-game.core/app
         :init    pi-game.startup/on-start
         :nrepl {:start? true}}

  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :figwheel true
                        :compiler {:main "pi-game.game"
                                   :asset-path "js/out"
                                   :output-to "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :pretty-print true
                                   :cache-analysis true
                                   :source-map-timestamp true
                                   :source-map true}}]})


