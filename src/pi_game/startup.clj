(ns pi-game.startup
  (require [pi-game.play :as play]
           [clojure.core.async :as async
            :refer [<! >! <!! >!! timeout chan alt! alts! alts!! go go-loop close!]]))

(defonce guesses (chan 100)) ;; maybe dropping buffer?
(defonce stop (chan))

(defn publish [msg]
  (go (>! guesses msg)))

(defn on-start []
  (go-loop []
   (let [[val chan] (alts! [guesses stop])]
     (when (= chan guesses)
       (play/handle-guess val)
       (recur)))))


