(ns bet.core
  (:gen-class))

(require '[clj-http.client :as client])
(require '[cheshire.core :as json])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Buscando resposta GET...")
  (def resp (client/get "https://betano.p.rapidapi.com/events" {:headers {:x-rapidapi-key "4a99664d09msh4338b9e97205a19p14bf36jsne665166aa327"
                                                                          :x-rapidapi-host "betano.p.rapidapi.com"}
                                                                :query-params {:tournamentId "38"}})
  )
  (println resp)
)