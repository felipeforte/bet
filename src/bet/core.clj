(ns bet.core
  (:gen-class))

(require '[clj-http.client :as client]
         '[cheshire.core :as json])

(defn get-json-sports
  "Retorna JSON com lista de esportes disponíveis"
  []
  ;; (println "Buscando resposta GET...")
  ;; (def resp (client/get "https://betano.p.rapidapi.com/sports" {:headers {:x-rapidapi-key "4a99664d09msh4338b9e97205a19p14bf36jsne665166aa327"
  ;;                                                                           :x-rapidapi-host "betano.p.rapidapi.com"}}))
  ;; (spit "test/sports" (:body resp)) ; Salva a chamada da API pra evitar estourar o limite durante a fase de testes
  ;; (json/parse-string (:body resp) true)  
  (println (json/parse-string (slurp "test/sports") true))
  )

(defn get-json-tournaments
  "Retorna JSON com lista de torneios de apostas"
  []
   (println "Buscando resposta GET...") ; Comentado para evitar chamadas à API
   (def resp (client/get "https://betano.p.rapidapi.com/tournaments"
               {:headers {"x-rapidapi-key" "7e74fa9991msh6adec433f460cb7p1f3802jsn94929cbbe680"
                          "x-rapidapi-host" "betano.p.rapidapi.com"}
                :query-params {"sport" "soccer"}}))
   (spit "test/tournaments.json" (:body resp)) ; Salva a resposta para evitar chamadas contínuas à API
  (println (json/parse-string (slurp "test/tournaments.json") true)))
 

(defn get-json-events
  "Retorna JSON com lista de eventos"
  []
  ;; (println "Buscando resposta GET...") ; Comentado para evitar chamadas à API
  ;; (def resp (client/get "https://betano.p.rapidapi.com/events"
  ;;              {:headers {"x-rapidapi-key" "7e74fa9991msh6adec433f460cb7p1f3802jsn94929cbbe680"
  ;;                         "x-rapidapi-host" "betano.p.rapidapi.com"}
  ;;               :query-params {"tournament" "some-tournament-id"}}))
  ;; (spit "test/events.json" (:body resp)) ; Salva a resposta para evitar estourar o limite da API
  (println (json/parse-string (slurp "test/events.json") true)))

(defn get-json-oddstypes
  "Retorna JSON com lista de tipos de odds"
  []
  ;; (println "Buscando resposta GET...") ; Comentado para evitar chamadas à API
  ;; (def resp (client/get "https://betano.p.rapidapi.com/oddstypes"
  ;;              {:headers {"x-rapidapi-key" "7e74fa9991msh6adec433f460cb7p1f3802jsn94929cbbe680"
  ;;                         "x-rapidapi-host" "betano.p.rapidapi.com"}}))
  ;; (spit "test/oddstypes.json" (:body resp)) ; Salva a resposta para evitar estourar o limite da API
  (println (json/parse-string (slurp "test/oddstypes.json") true)))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (get-json-sports)
  (get-json-tournaments)
  (get-json-events)
  (get-json-oddstypes)
  )