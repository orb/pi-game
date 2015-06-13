(defproject pi-game "0.1.0-SNAPSHOT"
  :description "The PI game"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [compojure "1.3.4"]
                 [hiccup "1.0.5"]
                 [ring-edn "0.1.0"]

                 [org.clojure/core.async "0.1.298.0-2a82a1-alpha"]

                 ;; for clojurescript
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.omcljs/om "0.8.8"]

                 [sablono "0.3.4"]
                 [cljs-ajax "0.3.12"]
                 [prismatic/dommy "1.1.0"]]

  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-ring "0.9.5"]]

  :ring {:handler pi-game.core/app
         :init    pi-game.startup/on-start
         :nrepl {:start? true}}

  :xhooks [leiningen.cljsbuild] ;; cljsbuild hook considered harmful

  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "public/js/app.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]})


