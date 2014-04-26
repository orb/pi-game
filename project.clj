(defproject pi-game "0.1.0-SNAPSHOT"
  :description "The PI game"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [ring-edn "0.1.0"]

                 [org.clojure/core.async "0.1.298.0-2a82a1-alpha"]
                 [org.clojure/clojurescript "0.0-2202"]

                 ;; for clojurescript
                 [om "0.6.2"]
                 [sablono "0.2.16"]
                 [cljs-ajax "0.1.6"]
                 [prismatic/dommy "0.1.1"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.7"]
            [lein-gorilla "0.2.0"]]

  :ring {:handler pi-game.core/app
         :init    pi-game.startup/on-start
         :nrepl {:start? true}}

  :xhooks [leiningen.cljsbuild] ;; cljsbuild hook considered harmful

  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "public/js/app.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]})
