(ns pi-game.play
  (:require [immutant.messaging :as msg]
            [immutant.cache :as cache]
            [pi-game.pi :as pi]))

(def game-cache
  (cache/create "game" :persist true))


(def default-players ["Persephone"
                      "Iris"
                      "Glaucus"
                      "Artemis"
                      "Morpheus"
                      "Erebus"])

(defn initial-state []
  {:current 3
   :digits  [3 \. 1 4]
   :colors  [0 0  0 0]
   :players (map (fn [name color]
                   {:name name :color color :score 0})
                 default-players (rest (range)))})

(defn current-state []
  (or (:game-state game-cache)
      (initial-state)))

(defn handle-guess [guess]
  (if (:reset guess)
    (cache/put game-cache :game-state (initial-state))
    (let [state (current-state)
          digit (:digit guess)
          user (:user guess)
          player-color (:color (first (filter #(= user (:name %)) (:players state))))

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

      (println "GOT" digit "expecting" (pi/nth-digit (:current state)))
      (if (= digit (pi/nth-digit (:current state)))
        (cache/put game-cache
                   :game-state
                   (-> state
                       (update-in [:current] inc)
                       (update-in [:digits] (add-item digit))
                       (update-in [:colors] (add-item player-color))
                       (update-in [:players] (add-point-to user))))
        state))))



