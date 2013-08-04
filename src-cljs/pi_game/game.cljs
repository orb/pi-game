(ns pi-game.game
  (:require [ajax.core :as ajax]
            [dommy.core :as dommy])
  (:use-macros [dommy.macros :only [sel sel1 node deftemplate]]))

(defn log [arg & args]
  (.log js/console arg args)
  (first args))

(defn oh-noes [response]
  (.log js/console "something bad happened: " (:status response) " " (:status-text response)))

(defn make-color [elem color-num]
  (dommy/add-class! elem (str "color" color-num)))

(defn digit-box [digit color]
  (-> (node [:span.digit digit])
      (make-color color)))

(defn width-spec [percent]
  (str "width: " (int percent) "%;"))

(defn player-bar [player total-points]
  (let [percent (* 100 (/ (:score player) total-points))
        player-color (:color player)]
    (node [:div (-> (node [:div.col-2 (:name player) [:span.badge.pull-right (:score player)]])
                    identity
                    #_(make-color player-color))
           [:div.progress
            (-> (node [:div.progress-bar.progress-bar-success {:style (width-spec percent)}])
                (make-color player-color))]])))

(defn update-game-state [response]
  #_(log (str response) "results")
  (-> (sel1 :.in-game)
      (dommy/remove-class! :hidden))

  (-> (sel1 :#current-digit)
      (dommy/set-text! (:current response)))

  (let [digits (map digit-box (:digits response) (:colors response))]
    (dommy/replace-contents! (sel1 :#digits) nil)
    (reduce dommy/append! (sel1 :#digits) digits)
    (dommy/append! (sel1 :#digits) (digit-box \_ 0 #_"X")))

  (let [total-points (reduce max 1 (map :score  (:players response)))]
    (dommy/replace-contents! (sel1 :#scoreboard) nil)
    (doseq [player (:players response)]
      (dommy/append! (sel1 :#scoreboard)
                     (player-bar player total-points)))))

(defn update-state []
  (ajax/GET "/pi-game/state" {:handler update-game-state
                              :error-handler oh-noes}))

(defn pressed [e]
  (let [code (.-keyCode e)]
    (cond
     (<= 48 code 57)
     (let [sel-elem (sel1 :#player-name)
            data {:digit (- code 48)
                  :user (dommy/text (aget (.-options sel-elem) (.-selectedIndex sel-elem)))
                  :position (js/parseInt (dommy/text (sel1 :#current-digit)))}]
       (ajax/POST "/pi-game/guess"
                  {:data data
                   :format :edn
                   :handler (fn [& args] (log (- code 48) "PRESSED"))
                   :error-handler oh-noes}))

     (== code 126)
     (ajax/POST "/pi-game/reset"))))

(defn init []
  (.log js/console "Why, hello there!")
  (js/setInterval update-state 1000)
  (-> (sel1 :body)
      (dommy/listen! :keypress pressed)))
