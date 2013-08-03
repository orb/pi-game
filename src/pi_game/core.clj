(ns pi-game.core
  (:use [compojure.core])

  (:require [compojure.route]
            [hiccup.core :as h]
            [hiccup.page :as page]
            [hiccup.element :as element]
            [immutant.messaging :as msg]
            [ring.middleware.edn :as ring-edn]

            [pi-game.pi :as pi]))

(def default-players ["Genevieve"
                      "Herman"
                      "Imogene"
                      "Percival"
                      "Thurston"])

(def tmp-state
  (atom {:current 3
         :digits  [3 \. 1 4]
         :colors  [0 0  0 0]
         :players (map (fn [name color]
                         {:name name :color color :score 1})
                       default-players (rest (range)))}))



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

(defn select-random [options]
  (seq (let [options-v (into [] options)]
         (update-in options-v [(rand-int (count options-v))]
                    #(do (println "!!" % (class %)) (conj % {:selected 1}))))))

(defn player-select []
  [:select#player-name.form-control {:style "width:200px;"}
   (let [random-name (rand-nth default-players)]
     (for [name default-players]
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

    [:form.navbar-form.pull-right
     (player-select)]]

   [:div.container
    [:div.in-game.well.hidden
     [:h2 "Looking for digit #" [:span#current-digit "--"]]

     [:div#digits]
     [:div "&nbsp;"]
     [:div#scoreboard.progress]]]))


(defn game-state []
  (edn @tmp-state))

(defn user-guess [req]
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

(defn process-guess [guess]
   (let [digit (:digit guess)
         user (:user guess)
         player-color (:color (first (filter #(= user (:name %)) (:players @tmp-state))))

         add-point-to
         (fn [user]
           (fn [player-state]
             (for [player player-state]
               (if (= user (:name player))
                 (update-in player [:score] inc)
                 player))))

         add-item
         (fn [item]
           (fn [items]
             (conj items item)))]

     (swap! tmp-state
            (fn [state]
              (println "GOT" digit "expecting" (pi/nth-digit (:current state)))
              (if (= digit (pi/nth-digit (:current state)))
                (-> state
                    (update-in [:current] inc)
                    (update-in [:digits] (add-item digit))
                    (update-in [:colors] (add-item player-color))
                    (update-in [:players] (add-point-to user)))

                state)))))
(defn handle-guess [message]
  (process-guess message))

