(ns immutant.init
  (:require [pi-game.core :as pi-game]
            [pi-game.play :as play]

            [immutant.messaging :as msg]
            [immutant.repl :as repl]
            [immutant.web :as web]
            [immutant.util :as util]
            [immutant.daemons :as daemon]))

;;(defonce nrepl (repl/start-nrepl 4242))

(web/start "/" pi-game/app)
(msg/start "/queue/guesses")

(defonce listener (atom nil))
(defn start-listener []
  (swap! listener
         (fn [listener]
           (println "! starting listener - was " listener)
           (msg/listen "/queue/guesses" play/handle-guess))))

(defn stop-listener []
  (swap! listener
         (fn [listener]
           (println "! stopping listener - was " listener)
           (when listener
             (msg/unlisten listener))
           nil)))

(daemon/daemonize "guess-listener"
                  start-listener
                  stop-listener
                  :singleton true)


