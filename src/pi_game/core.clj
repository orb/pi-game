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
    (page/include-css "http://fonts.googleapis.com/css?family=Press+Start+2P&subset=latin")]
   [:body body]
   (page/include-js "/pi-game/resources/js/jquery-1.10.2.min.js")
   (page/include-js "/pi-game/resources/js/bootstrap.min.js")
   (page/include-js "/pi-game/resources/js/app.js")
   (element/javascript-tag "pi_game.game.init();")))

(defn game-app []
  (html
   [:div.container
    [:div.navbar {:style "font-family: 'Press Start 2P', cursive;"}
     [:p.navbar-text "SHALL WE PLAY A GAME?"]]

    [:div.in-game.well.hidden
     [:h2 "Searching for digit " [:span#current-digit "--"]]
     [:div#digits]
     [:div "&nbsp;"]
     [:div#scoreboard.progress]]]))



(def tmp-state
  (atom {:current 3
         :digits
         [3 \. 1 4]
         :colors
         [0 0  1 1]

         :players [{:name "Bob"  :color 1 :score 7}
                   {:name "Sam"  :color 2 :score 15}
                   {:name "Anna" :color 3 :score 12}]}))


(defn game-state []
  (println "GET /state")
  (edn @tmp-state))

(defn user-guess [req]
  (let [params (:params req)
        digit (:digit params)]
    (swap! tmp-state
           (fn [state]
             (println "GOT" digit "expecting" (pi/nth-digit (:current state)))
             (if (= digit (pi/nth-digit (:current state)))
               (-> state
                   (update-in [:current] inc)
                   (update-in [:digits] #(conj % (:digit params)))
                   (update-in [:colors] #(conj % 3)))
               state))))
  #_(println "!" @tmp-state)
  (edn :ok))

(defroutes app-routes
  (GET "/" [] (game-app))
  (GET "/state" [] (game-state))
  (POST "/guess" [] user-guess)
  (compojure.route/files "/resources"))

(def app
  (-> app-routes
      ring-edn/wrap-edn-params))
