(ns pi-game.core
  (:use [compojure.core])
  (:require [hiccup.core :as h]
            [hiccup.page :as page]
            [hiccup.element :as element]
            [compojure.route]
            [ring.middleware.edn :as ring-edn]
            [pi-game.pi :as pi]))

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
(def default-players ["Genevieve"
                      "Herman"
                      "Imogene"
                      "Percival"
                      "Thurston"])

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

    [:form.navbar-form.pull-right
     [:select#player-name.form-control
      {:style "width:200px;"}
      (map (fn [name] [:option name])
           default-players)]]]

   [:div.container
    [:div.in-game.well.hidden
     [:h2 "Searching for digit " [:span#current-digit "--"]]

     [:div#digits]
     [:div "&nbsp;"]
     [:div#scoreboard.progress]]]))



(def tmp-state
  (atom {:current 3
         :digits  [3 \. 1 4]
         :colors  [0 0  0 0]
         :players (map (fn [name color]
                         {:name name :color color :score 1})
                       default-players (rest (range)))}))


(defn game-state []
  (println "GET /state")
  (edn @tmp-state))

(defn user-guess [req]
  (let [params (:params req)
        digit (:digit params)
        user (:user params)
        color (:color (first (filter #(= user (:name %)) (:players @tmp-state))))

        _ (println "!" user "!" color)

        add-point-to
        (fn [user player-state]
          (into []
                (for [player player-state]
                  (if (= user (:name player))
                    (update-in player [:score] inc)
                    player))))]

    (swap! tmp-state
           (fn [state]
             (println "GOT" digit "expecting" (pi/nth-digit (:current state)))
             (if (= digit (pi/nth-digit (:current state)))
               (-> state
                   (update-in [:current] inc)
                   (update-in [:digits] #(conj % (:digit params)))
                   (update-in [:colors] #(conj % color))
                   (update-in [:players] (partial add-point-to user)))

               state))))
  (edn :ok))

(defroutes app-routes
  (GET "/" [] (game-app))
  (GET "/state" [] (game-state))
  (POST "/guess" [] user-guess)
  (compojure.route/files "/resources"))

(def app
  (-> app-routes
      ring-edn/wrap-edn-params))
