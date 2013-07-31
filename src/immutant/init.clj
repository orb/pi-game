(ns immutant.init
  (:require [pi-game.core :as pi-game]
            [immutant.messaging :as messaging]
            [immutant.repl :as repl]
            [immutant.web :as web]
            [immutant.util :as util]))

;;(defonce nrepl (repl/start-nrepl 4242))
(web/start "/" pi-game/app)



;; Messaging allows for starting (and stopping) destinations (queues & topics)
;; and listening for messages on a destination.

; (messaging/start "/queue/a-queue")
; (messaging/listen "/queue/a-queue" #(println "received: " %))

