(ns pi-game.game
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [sel sel1 node deftemplate]])
  (:require [ajax.core :as ajax]
            [cljs.core.async :refer [<! chan put! sliding-buffer]]
            [dommy.core :as dommy]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(defn pressed [e]
  (let [code (.-keyCode e)]
    (cond
     (<= 48 code 57)
     (let [sel-elem (sel1 :#player-name)
            data {:digit (- code 48)
                  :user (dommy/text (aget (.-options sel-elem) (.-selectedIndex sel-elem)))
                  :position (js/parseInt (dommy/text (sel1 :#current-digit)))}]
       (ajax/POST "/guess"
                  {:data data
                   :format :edn
                   :handler (fn [& args] (println (- code 48) "PRESSED"))
                   ;; :error-handler oh-noes
                   }))

     (== code 126)
     (ajax/POST "/reset"))))

;; ----------------------------------------

(defonce game-state (atom {:playing false}))

(defn oh-noes [response]
  (.log js/console "something bad happened: " (:status response) " " (:status-text response)))

(defn progress-bar [bar owner]
  (reify
    om/IRender
    (render [_]
      (let [width-style (str (:percent bar) "%")
            color-class (str "color" (:color bar))]

        (html [:div.progress
               [:div.progress-bar.progresss-bar-success {:class (str "color" (:color bar))
                                                         :style {:width width-style}}]])))))

(defn scorebar-view [player owner]
  (reify om/IRender
    (render [_]
      (let [total-points (om/get-shared owner :total-points)
            percent (* 100 (/ (:score player) total-points))]
        (html [:div
               [:div.div.col-2 (:name player)
                [:div.span.badge.pull-right (:score player)]]
               (om/build progress-bar {:color (:color player)
                                       :percent percent})])))))

(defn scoreboard-view [players owner]
  (reify
    om/IRender
    (render [_]
      (html [:div [:h2 "Scoreboard"]
               (let [total-points (reduce max 1 (map :score players))]
                 [:div.scoreboard.well
                  (om/build-all scorebar-view players
                                {:shared {:total-points total-points}})])]))))


(defn digit-box [digit color]
  [:span.digit {:class (str "color" color)} digit])

(defn game-view [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [handle-new-state
            (fn [resp]
              (om/set-state! owner :in-game true)
              (om/update! app :game-state resp))

            ping-server
            (fn []
              (ajax/GET "/state" {:handler handle-new-state :error-handler oh-noes}))]

        (let [timer-id (js/setInterval ping-server 1000)]
          (om/set-state! owner :timer-id timer-id))))

    om/IWillUnmount
    (will-unmount [_]
      (let [timer-id (om/get-state owner :timer-id)]
        (when timer-id
          (js/clearInterval timer-id))))

    om/IRenderState
    (render-state [_ {:keys [in-game]}]
      (if in-game
        (html [:div.container
               [:h2 "Looking for digit #"
                [:span.current-digit (get-in app [:game-state :current])]]
               [:div.digits.well
                (map digit-box
                     (get-in app [:game-state :digits])
                     (get-in app [:game-state :colors]))
                (digit-box \_ 0)]
               (om/build scoreboard-view (get-in app [:game-state :players]))])
        (html [:h2 "Waiting for game"])))))

(defn init []
  (om/root game-view game-state
           {:target  (.getElementById js/document "game")})

  (-> (sel1 :body)
      (dommy/listen! :keypress pressed)))
