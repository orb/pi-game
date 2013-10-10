(ns pi-game.startup
  (require [pi-game.play :as play]
           [clojure.core.async :as async
            :refer [<! >! timeout chan alt! go]]))

(defonce guesses (chan 100)) ;; maybe dropping buffer?
(defonce stop (chan))

(defn publish [msg]
  (println "GUESS" msg)
  (go (>! guesses msg)))

(defn on-start []
  (go (loop []
        (println "WAITING FOR GUESS")
        (let [[val chan] (alts! [guesses stop])]
          (when (= chan guesses)
            (println "GUESS" val)
            (play/handle-guess val)
            (recur))))
      (println "Done...")))



