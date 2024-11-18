(ns bet.core
  (:gen-class))

(require '[clj-http.client :as client]
         '[cheshire.core :as json]
         )

;; Função para processar esportes
(defn get-json-sports
  "Retorna JSON com lista de esportes disponíveis"
  []
  (println "Buscando resposta GET (sports)...")
  ;; (def resp (client/get "https://betano.p.rapidapi.com/sports" {:headers {:x-rapidapi-key "sua-chave-aqui"
  ;;                                                                           :x-rapidapi-host "betano.p.rapidapi.com"}}))
  ;; (spit "test/sports.json" (:body resp)) ; Salva a chamada da API pra evitar estourar o limite durante a fase de testes
  (let [dados (json/parse-string (slurp "test/sports.json") true)]
    (println "Esportes disponíveis:" dados)))

;; Função para processar torneios
(defn get-json-tournaments
  "Retorna JSON com lista de torneios de apostas"
  []
  (println "Buscando resposta GET (tournaments)...")
  ;; (def resp (client/get "https://betano.p.rapidapi.com/tournaments"
  ;;                       {:headers {:x-rapidapi-key "sua-chave-aqui"
  ;;                                  :x-rapidapi-host "betano.p.rapidapi.com"}
  ;;                        :query-params {:sport "soccer"}}))
  ;; (spit "test/tournaments.json" (:body resp)) ; Salva a resposta para evitar chamadas contínuas à API
  (let [dados (json/parse-string (slurp "test/tournaments.json") true)
        torneios (:tournaments dados)]
    (dorun (map (fn [torneio]
                  (println "Torneio ID:" (:id torneio) "Nome:" (:name torneio)))
                torneios))))

;; Função para processar eventos
(defn get-json-events
  "Retorna JSON com lista de eventos"
  []
  (println "Buscando resposta GET (events)...")
  ;; (def resp (client/get "https://betano.p.rapidapi.com/events"
  ;;                       {:headers {:x-rapidapi-key "sua-chave-aqui"
  ;;                                  :x-rapidapi-host "betano.p.rapidapi.com"}
  ;;                        :query-params {:tournamentId "38"}}))
  ;; (spit "test/events.json" (:body resp)) ; Salva a resposta para evitar chamadas contínuas à API
  (let [dados (json/parse-string (slurp "test/events.json") true)]
    (println "Eventos disponíveis:" dados)))

;; Função para processar tipos de odds
(defn get-json-oddstypes
  "Retorna JSON com lista de tipos de odds"
  []
  (println "Buscando resposta GET (oddstypes)...")
  ;; (def resp (client/get "https://betano.p.rapidapi.com/oddstypes"
  ;;                       {:headers {:x-rapidapi-key "sua-chave-aqui"
  ;;                                  :x-rapidapi-host "betano.p.rapidapi.com"}
  ;;                        :query-params {:sport "soccer"}}))
  ;; (spit "test/oddstypes.json" (:body resp)) ; Salva a resposta para evitar chamadas contínuas à API
  (let [dados (json/parse-string (slurp "test/oddstypes.json") true)]
    (println "Tipos de odds disponíveis:" dados)))


(defn acessar-dados
  "Acessa dados específicos do JSON"
  []
  (let [dados (json/parse-string (slurp "test/events.json") true)]
    (println "Estrutura dos dados:" dados)  ;; Verifica a estrutura do JSON parseado

    ;; Acessar o nome da categoria
    (println "Nome da categoria:" (:categoryName dados))

    ;; Acessar eventos
    (let [eventos (:events dados)]  ;; Acessando o mapa de eventos
      ;; Acessar o primeiro evento (id 0)
      (let [evento-0 (get eventos "0")]  ;; Acessa o evento com ID "0"
        (println "Primeiro evento:")
        (println "Data do evento:" (:date evento-0))
        (println "Participante 1:" (:participant1 evento-0))
        (println "Participante 2:" (:participant2 evento-0))
        (println "Status do evento:" (:eventStatus evento-0))
        (println "Hora do evento:" (:time evento-0))))))




;; Função principal
(defn -main
  "Executa as chamadas de teste"
  [& args]
  ;;(get-json-sports)
  ;;(get-json-tournaments)
  ;;(get-json-events)
  ;;(get-json-oddstypes)
  (acessar-dados))
