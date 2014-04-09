(ns pi-game.play
  (:require [pi-game.pi :as pi]))

(def game-cache
  (atom {}))

(defn cache-put [c k v]
  (swap! c assoc k v))

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
  (or (:game-state @game-cache)
      (initial-state)))


(defn user-correct-guess [state digit user player-color]
  (let [add-item
        (fn [item]
          (fn [items]
            (conj items item)))

        add-point-to
        (fn [user]
          (fn [player-state]
            (for [player player-state]
              (if (= user (:name player))
                (update-in player [:score] inc)
                player))))]
    (-> state
        (update-in [:current] inc)
        (update-in [:digits] (add-item digit))
        (update-in [:colors] (add-item player-color))
        (update-in [:players] (add-point-to user)))))

(defn do-reset []
  (cache-put game-cache :game-state (initial-state)))

(defn do-guess [guess]
  (let [state (current-state)
        digit (:digit guess)
        user (:user guess)
        player-color (:color (first (filter #(= user (:name %)) (:players state))))]

    (if (= digit (pi/nth-digit (:current state)))
      (cache-put game-cache
                 :game-state
                 (user-correct-guess state digit user player-color))
      state)))

(defn handle-guess [guess]
  (if (:reset guess)
    (do-reset)
    (do-guess guess)))



