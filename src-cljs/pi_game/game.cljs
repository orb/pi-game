(ns pi-game.game
  (:require [ajax.core :as ajax]
            [dommy.core :as dommy])
  (:use-macros [dommy.macros :only [sel sel1 node deftemplate]]))

(defn log [e msg]
  (.log js/console msg e)
  e)

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
  (log (pr-str player) "PLAYER")
  (let [percent (* 100.0 (/ (:score player) total-points))]
    (-> (node [:div.progress-bar.progress-bar-success {:style (width-spec percent)}
               (str (:name player) " - " (:score player))])
        (make-color (:color player)))))

(defn update-game-state [response]
  (log (str response) "results")
  (-> (sel1 :.in-game)
      (dommy/remove-class! :hidden))

  (-> (sel1 :#current-digit)
      (dommy/set-text! (:current response)))

  (let [digits (map digit-box (:digits response) (:colors response))]
    (dommy/replace-contents! (sel1 :#digits) nil)
    (reduce dommy/append! (sel1 :#digits) digits)
    (dommy/append! (sel1 :#digits) (digit-box \_ 0 #_"X")))

  (let [total-points (reduce + (map :score  (:players response)))]
    (dommy/replace-contents! (sel1 :#scoreboard) nil)
    (doseq [player (:players response)]
      (dommy/append! (sel1 :#scoreboard)
                     (player-bar player total-points)))))

(defn update-state []
  (ajax/GET "/pi-game/state" {:handler update-game-state
                              :error-handler oh-noes}))

(defn pressed [e]
  (let [code (.-keyCode e)]
    (when (<= 48 code 57)
      (let [sel-elem (sel1 :#player-name)
            data {:digit (- code 48)
                  :user (dommy/text (aget (.-options sel-elem) (.-selectedIndex sel-elem)))
                  :position (js/parseInt (dommy/text (sel1 :#current-digit)))}]
        (ajax/POST "/pi-game/guess"
                   {:data data
                    :format :edn
                    :handler (fn [& args] (log digit "PRESSED"))
                    :error-handler oh-noes})))))

(defn init []
  (.log js/console "Why, hello there!")
  (js/setInterval update-state 1000)
  (-> (sel1 :body)
      (dommy/listen! :keypress pressed)))
