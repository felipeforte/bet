(ns bet.core
  (:gen-class))

(require '[clj-http.client :as client]
         '[cheshire.core :as json])

(def api-key "c9cb38318cmsh9607b632446376bp113300jsnc5cf5f7e8aca")

(defn filtrar-brasil [body]
  (into {} (filter (fn [[_ v]] (= (:categoryName v) "Brazil")) body)))

(defn get-tournaments-soccer
  "Retorna JSON com lista de torneios de futebol"
  []
  (println "Buscando torneios via API externo...")
  ; Descomentar em produção
  ;; (let [resp (client/get "https://betano.p.rapidapi.com/tournaments" {:headers {:x-rapidapi-key api-key
  ;;                                                                               :x-rapidapi-host "betano.p.rapidapi.com"}
  ;;                                                                     :query-params {:sport "soccer"}})
  ;;       body (:body resp)]
  ;;      (spit "test/tournaments-soccer.json" body)
  ;;   )
  (let [body (json/parse-string (slurp "test/tournaments-soccer.json") true)]
    (filtrar-brasil body))
  )

(defn get-events [tournamentId]
  (let [resp (client/get "https://betano.p.rapidapi.com/events" {:headers {:x-rapidapi-key api-key
                                                                           :x-rapidapi-host "betano.p.rapidapi.com"}
                                                                 :query-params {:tournamentId tournamentId}})
          body (:body resp)]
         (spit (str "test/" tournamentId "-events.json") body)
      )
  )

(defn get-event-odds [eventId]
  (let [resp (client/get "https://betano.p.rapidapi.com/odds_betano" {:headers {:x-rapidapi-key "c9cb38318cmsh9607b632446376bp113300jsnc5cf5f7e8aca"
                                                                                :x-rapidapi-host "betano.p.rapidapi.com"}
                                                                      :query-params {:eventId eventId
                                                                                     :oddsFormat "decimal"
                                                                                     :raw "false"}})
        body (:body resp)]
    (spit (str "test/" eventId "-odds.json") body)))

(defn get-tournaments-basketball
  "Retorna JSON com lista de torneios de basquete"
  []
  (println "Buscando torneios via API externo...")
  ; Descomentar em produção
  ;; (let [resp (client/get "https://betano.p.rapidapi.com/tournaments" {:headers {:x-rapidapi-key api-key
  ;;                                                                               :x-rapidapi-host "betano.p.rapidapi.com"}
  ;;                                                                     :query-params {:sport "basketball"}})
  ;;       body (:body resp)]
  ;;   (spit "test/tournaments-basketball.json" body))
  (let [body (json/parse-string (slurp "test/tournaments-basketball.json") true)]
    (filtrar-brasil body))
  )


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (get-tournaments-soccer))
  (println (get-tournaments-basketball))
  )