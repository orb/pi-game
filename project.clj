(defproject pi-game "0.1.0-SNAPSHOT"
  :description "The PI game"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [ring-edn "0.1.0"]

                 ;; for clojurescript
                 [cljs-ajax "0.1.6"]
                 [prismatic/dommy "0.1.1"]]

  :plugins [[lein-cljsbuild "0.3.2"]
            [lein-immutant "1.0.0"]]

  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "public/js/app.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}

  :immutant {:nrepl-port 4242})
