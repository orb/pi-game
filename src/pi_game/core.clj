(ns pi-game.core
  (:use [compojure.core])
  (:require [compojure.route]
            [hiccup.core :as h]
            [hiccup.page :as page]
            [hiccup.element :as element]
            [immutant.messaging :as msg]
            [ring.middleware.edn :as ring-edn]
            [pi-game.pi :as pi]
            [pi-game.play :as play]))

(defn edn [data]
  {:body (pr-str data)
   :headers {"Content-Type" "application/edn;charset=UTF-8"}})

(defn html [& body]
  (page/html5 {:lang "en"}
   [:head
    [:title "PI Game"]
    (page/include-css "/pi-game/resources/css/bootstrap.min.css")
    (page/include-css "/pi-game/resources/css/game.css")
    (page/include-css "http://fonts.googleapis.com/css?family=Press+Start+2P&subset=latin,greek")]
   [:body body]
   (page/include-js "/pi-game/resources/js/jquery-1.10.2.min.js")
   (page/include-js "/pi-game/resources/js/bootstrap.min.js")
   (page/include-js "/pi-game/resources/js/app.js")
   (element/javascript-tag "pi_game.game.init();")))

(defn player-select []
  [:select#player-name.form-control {:style "width:200px;"}
   (let [players (map :name (:players (play/current-state)))
         random-name (rand-nth players)]
     (for [name players]
       (if (= random-name name)
         [:option {:selected 1} name]
         [:option name])))])

(defn game-app []
  (html
   [:div.navbar
    [:span
     {:style "font-family: 'Press Start 2P', cursive;"}

     [:div.navbar-brand "\u03C0"]
     [:ul.nav.navbar-nav
      [:li [:a "SHALL"]]
      [:li [:a "WE"]]
      [:li [:a "PLAY"]]
      [:li [:a "A"]]
      [:li [:a "GAME?"]]]]

    [:form.navbar-form.pull-right (player-select)]]

   [:div.container
    [:div.in-game.well.hidden
     [:h2 "Looking for digit #" [:span#current-digit "--"]]

     [:div#digits]
     [:div "&nbsp;"]
     [:div#scoreboard.progress]]]))


(defn game-state []
  (edn (play/current-state)))

(defn user-guess [req]
  (println "! keypress from"  (:user (:params req)))
  (msg/publish "/queue/guesses" (:params req))
  (edn :ok))

(defroutes app-routes
  (GET "/" [] (game-app))
  (GET "/state" [] (game-state))
  (POST "/guess" [] user-guess)
  (compojure.route/files "/resources"))

(def app
  (-> app-routes
      ring-edn/wrap-edn-params))



