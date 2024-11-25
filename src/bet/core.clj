(ns bet.core
  (:gen-class))

(require '[clj-http.client :as client]
         '[cheshire.core :as json])

(def disponiveis {:torneios #{325, 1562}})

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
  ;;       json-string (:body resp)
  ;;       body (json/parse-string json-string)]
  ;;      (spit "test/tournaments-soccer.json" json-string)
  ;;   )

  ;; Comentar em produção
  (let [body (json/parse-string (slurp "test/tournaments-soccer.json") true)]
    (filtrar-brasil body))
  )

(defn get-events [tournamentId]
  (if (not (contains? (:torneios disponiveis) tournamentId))
    {:erro "Torneio não disponível."}

    ;; Descomentar em produção
    ;; (let [resp (client/get "https://betano.p.rapidapi.com/events" {:headers {:x-rapidapi-key api-key
    ;;                                                                          :x-rapidapi-host "betano.p.rapidapi.com"}
    ;;                                                                :query-params {:tournamentId tournamentId}})
    ;;       json-string (:body resp)
    ;;       body (json/parse-string json-string true)]
    ;;   (spit (str "test/" tournamentId "-events.json") json-string)
    ;; Comentar em produção
    (let [body (json/parse-string (slurp (str "test/" tournamentId "-events.json")) true)]
      (:events body))
  )
  )

(defn get-event-odds [eventId]
  (let [resp (client/get "https://betano.p.rapidapi.com/odds_betano" {:headers {:x-rapidapi-key api-key
                                                                                :x-rapidapi-host "betano.p.rapidapi.com"}
                                                                      :query-params {:eventId eventId
                                                                                     :oddsFormat "decimal"
                                                                                     :raw "false"}})
        json-string (:body resp)
        body (json/parse-string json-string)]
    (spit (str "test/" eventId "-odds.json") body)))

(defn get-tournaments-basketball
  "Retorna JSON com lista de torneios de basquete"
  []
  (println "Buscando torneios via API externo...")
  ; Descomentar em produção
  ;; (let [resp (client/get "https://betano.p.rapidapi.com/tournaments" {:headers {:x-rapidapi-key api-key
  ;;                                                                               :x-rapidapi-host "betano.p.rapidapi.com"}
  ;;                                                                     :query-params {:sport "basketball"}})
  ;;       json-string (:body resp)
  ;;         body (json/parse-string json-string))
  (let [body (json/parse-string (slurp "test/tournaments-basketball.json") true)]
    (filtrar-brasil body))
  )


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (get-events 325))
  )