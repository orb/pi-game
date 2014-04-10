(ns pi-game.game
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [sel sel1 node deftemplate]])
  (:require [ajax.core :as ajax]
            [dommy.core :as dommy]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [<! chan put! sliding-buffer]]))

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

(defn scorebar-view [player owner]
  (reify om/IRenderState
    (render-state [_ state]
      (let [total-points (om/get-shared owner :total-points)
            percent (* 100 (/ (:score player) total-points))
            player-color (:color player)]
        (dom/div nil
                 (dom/div #js {:className "div col-2"}
                          (:name player)
                          (dom/div #js {:className "span badge pull-right"}
                                   (:score player)))
                 (dom/div #js {:className "progress"}
                          (dom/div #js {:className (str "progress-bar progress-bar-succes color" player-color)
                                        :style #js {:width (str percent "%")}})))))))

(defn scoreboard-view [players owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
               (dom/h2 nil "Scoreboard")
               (let [total-points (reduce max 1 (map :score players))]
                 (apply dom/div #js {:className "scoreboard well"}
                        (om/build-all scorebar-view players {:shared {:total-points total-points}})))))))



(defn game-view [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [handle-response
            (fn [resp]
              (om/set-state! owner :in-game true)
              (om/transact! app :game-state (constantly resp)))

            ping-server
            (fn []
              (ajax/GET "/state" {:handler handle-response :error-handler oh-noes}))]
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
        (dom/div #js {:className "container"}
                 (dom/h2 nil
                         "Looking for digit #"
                         (dom/span #js {:className "current-digit"}
                                   (get-in app [:game-state :current])))
                 (apply dom/div #js {:className "digits well"}
                        (mapv #(dom/span #js {:className (str "digit color" %2)} %1)
                              (conj (get-in app [:game-state :digits]) \_)
                              (conj (get-in app [:game-state :colors]) 0)))
                 (om/build scoreboard-view (get-in app [:game-state :players])))

        (dom/h2 nil "Waiting...")))))

(defn init []
  (.log js/console "Why, hello there!")
  (om/root game-view game-state
           {:target  (.getElementById js/document "game")
            :init-state {:monkey "balls"}})

  (-> (sel1 :body)
      (dommy/listen! :keypress pressed)))
