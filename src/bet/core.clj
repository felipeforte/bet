(ns bet.core
  (:gen-class)
  (:require [clj-http.client :as client]
           [cheshire.core :as json]
           [clojure.java.io :as io]))



(def disponiveis {:torneios #{325, 1562}})

(def api-key "8a88e6eaa1msh937464703c269abp186cbajsn7b39931b7d41")

(defn check-dir-cache []
  (println "Checando diretório cache")
  (let [dir (java.io.File. "cache/")]
    (when-not (.exists dir)
      (println "Diretório cache inexistente, criando...")
      (.mkdirs dir))))

(defn get-cache [tipo extra]
  "Obtém o conteúdo do cache, se disponível, ou retorna {}"
  (let [arquivo (cond
                  (= tipo "torneios") (str "cache/tournaments-" extra ".json")
                  (= tipo "eventos") (str "cache/events-" extra ".json")
                  (= tipo "odds") (str "cache/odds-" extra ".json"))]
    (if (.exists (io/file arquivo))
      (slurp arquivo)
      "{}")))


(defn processar-cache
  "Processa o cache: verifica validade e retorna o conteúdo JSON ou false"
  [tipo extra]
  (let [json-string (get-cache tipo extra)
        json-data (json/parse-string json-string true)
        cache-vazio? (or (empty? json-data) (= json-data {}))
        sem-timestamp? (not (:timestamp json-data))
        cache-expirado? (and (:timestamp json-data)
                             (> (count (keys json-data)) 1)
                             (> (- (System/currentTimeMillis) (:timestamp json-data))
                                (* 3600 1000)))
        cota-excedida? (and (:error json-data)
                            (= (:error json-data) "Cota de API excedida. Use outra chave API."))]
    (cond
      cache-vazio? false ;; Cache vazio ou inválido
      sem-timestamp? false ;; Cache sem timestamp
      cache-expirado? false ;; Cache expirado
      cota-excedida? false ;; Cota da API excedida
      :else
      (do
        (println "Usando cache disponível...")
        json-data))))


(defn api-get
  "Faz chamadas GET de forma programática para torneios (pra deixar o código mais limpo)"
  [req-tipo extra]  
  (let [header-base {:headers {:x-rapidapi-key api-key
                               :x-rapidapi-host "betano.p.rapidapi.com"}}
        link-base "https://betano.p.rapidapi.com/"]
    (cond
      (= req-tipo "torneios")
      (client/get (str link-base "tournaments")
                  (assoc header-base :query-params {:sport extra}))

      (= req-tipo "eventos")
      (client/get (str link-base "events")
                  (assoc header-base :query-params {:tournamentId extra}))

      (= req-tipo "odds")
      (client/get (str link-base "odds_betano")
                  (assoc header-base :query-params {:eventId extra
                                                    :oddsFormat "decimal"
                                                    :raw "false"})))))

(defn try-catch-api
  "Faz chamadas GET de forma programática para eventos e odds (pra deixar o código mais limpo)"
  [req-tipo extra processar-func cache-arquivo]
  (try
    (let [resp (api-get req-tipo extra)  ;; Chama a API com o tipo e parâmetros
          body (assoc (json/parse-string (:body resp) true) :timestamp (System/currentTimeMillis))
          filtrado (processar-func body extra)]  ;; Processa os dados da resposta
      (println (str "Buscando " req-tipo " via API externa..."))
      (spit cache-arquivo (json/generate-string filtrado true))  ;; Salva os dados em cache
      filtrado)  ;; Retorna os dados processados
    (catch clojure.lang.ExceptionInfo e
      (let [data (ex-data e)
            status (:status data)
            body-msg-str (:message (json/parse-string (:body data) true))
            error-message (cond
                            (and body-msg-str (.contains body-msg-str "You have exceeded"))
                            "Cota de API excedida. Use outra chave API."
            
                            (and body-msg-str (.contains body-msg-str "tournament exists but is not active at the moment"))
                            "Torneio existe, mas não está ativo no momento."
            
                            (and body-msg-str (.contains body-msg-str "Event is already finished."))
                            "Evento já finalizado."
            
                            :else
                            body-msg-str)]
        (if (or (= 400 status) (= 429 status))
          (do
            (println (str "Erro ao buscar " req-tipo ": " error-message))
            (let [erro {:error error-message, (keyword req-tipo "-id") extra}]
              (spit cache-arquivo (json/generate-string (assoc erro :timestamp (System/currentTimeMillis)) true))
              erro)))))))  ;; Retorna o erro tratado



(defn filtrar-torneios
  "Filtra torneios brasileiros"
  [body]
  (into {} (filter (fn [[_ v]] (= (:categoryName v) "Brazil")) body)))

(defn filtrar-eventos
  "Filtra chaves irrelevantes e retorna os dados organizados"
  [body tournamentId]
  (into {}
        (map (fn [[k {:keys [date participant1 participant2 eventStatus eventId]}]]
               [k {:date          date
                   :participant1  participant1
                   :participant2  participant2
                   :eventStatus   eventStatus
                   :eventId       eventId
                   :tournamentId  tournamentId}]))
        (:events body)))

(defn get-torneios
  "Retorna a lista de torneios de futebol, usando cache ou API"
  [sport]
  (check-dir-cache)
  (let [cache (processar-cache "torneios" sport)]
    (if cache
      cache
      (let [resp (api-get "torneios" sport) ;; Caso não tenha cache válido, faz a chamada à API
            body (assoc (json/parse-string (:body resp) true) :timestamp (System/currentTimeMillis))]
        (println "Buscando torneios via API externa...")
        (spit (str "cache/tournaments-" sport ".json") (json/generate-string (filtrar-torneios body) true)) ;; Salva em arquivo com timestamp
        ))))

(defn get-eventos
  "Retorna a lista de eventos a partir de certo torneio, usando cache ou API"
  [tournamentId]
  (check-dir-cache)
  (let [cache (processar-cache "eventos" tournamentId)
        cache-arquivo (str "cache/events-" tournamentId ".json")]
    (if cache
      cache
      (try-catch-api "eventos" tournamentId filtrar-eventos cache-arquivo))))


(defn get-evento-odds
  "Retorna mercados e odds a partir de certo evento, usando cache ou API"
  [eventId]
  (check-dir-cache)
  (let [cache (processar-cache "odds" eventId)
        cache-arquivo (str "cache/odds-" eventId ".json")]
    (if cache
      cache
      (try-catch-api "odds" eventId identity cache-arquivo))))
  


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; (println (get-event-odds "id100032548215167"))
  (println (get-evento-odds "id100039048424615")))
  