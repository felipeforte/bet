(ns bet.core
  (:gen-class))

(require '[clj-http.client :as client]
         '[cheshire.core :as json])

(def api-key "c9cb38318cmsh9607b632446376bp113300jsnc5cf5f7e8aca")
 

(defn get-json-events
  "Retorna JSON com lista de eventos"
  []
  (println "Buscando resposta GET (events)...") ; Comentado para evitar chamadas à API
  ;; (def resp (client/get "https://betano.p.rapidapi.com/events"
  ;;              {:headers {"x-rapidapi-key" api-key
  ;;                         "x-rapidapi-host" "betano.p.rapidapi.com"}
  ;;               :query-params {:tournamentId "91"}}))
  ;; (spit "test/events.json" (:body resp)) ; Salva a resposta para evitar estourar o limite da API
  (println (json/parse-string (slurp "test/events.json") true))
  )

(defn get-json-oddstypes
  "Retorna JSON com lista de tipos de odds"
  []
  (println "Buscando resposta GET (oddstypes)...") ; Comentado para evitar chamadas à API
  ;; (def resp (client/get "https://betano.p.rapidapi.com/oddstypes"
  ;;                       {:headers {:x-rapidapi-key api-key
  ;;                                  :x-rapidapi-host "betano.p.rapidapi.com"}
  ;;                        :query-params {:sport "soccer"}}))
  ;; (spit "test/oddstypes.json" (:body resp)) ; Salva a resposta para evitar estourar o limite da API
  (println (json/parse-string (slurp "test/oddstypes.json") true))
  )


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (get-json-events)
  (get-json-oddstypes)
  )