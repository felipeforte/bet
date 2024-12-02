(ns bet.core
  (:gen-class)
  (:require [clj-http.client :as client]
           [cheshire.core :as json]
           [clojure.java.io :as io]))



(def disponiveis {:torneios #{325, 1562}})

(def api-key "99b5e27786msh32148b7670e235bp1802f3jsn8d40ad589bf5")

(def cache-tempo (* 3600 1000 6)) ;; Definindo 6h em milisegundos para tempo de renovação de cache da API

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
        sem-timestamp? (or (not (:timestamp json-data)) (not (number? (:timestamp json-data))))
        cache-expirado? (and (:timestamp json-data)
                             (> (count (keys json-data)) 1)
                             (> (- (System/currentTimeMillis) (:timestamp json-data)) cache-tempo))
        cota-excedida? (and (:error json-data)
                            (= (:error json-data) "Cota de API excedida. Use outra chave API."))]
    (cond
      cache-vazio? false
      sem-timestamp? false
      cache-expirado? false
      cota-excedida? false
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
  "Faz chamadas GET de forma programática para diferentes tipos de requisições"
  [req-tipo extra cache-arquivo & [filtro-func]]
  (try
    (let [resp (api-get req-tipo extra)
          body (assoc (json/parse-string (:body resp) true) :timestamp (System/currentTimeMillis))
          filtrado (if filtro-func
                     (if (= req-tipo "torneios")
                       (filtro-func body)    ;; For 'torneios', call with 'body' only
                       (filtro-func body extra))  ;; For others, call with 'body' and 'extra'
                     body)]
      (println (str "Buscando " req-tipo " via API externa..."))
      (spit cache-arquivo (json/generate-string filtrado true))
      filtrado)
    (catch clojure.lang.ExceptionInfo e
      (let [data (ex-data e)
            status (:status data)
            body-msg-str (:message (json/parse-string (:body data) true))
            error-message (cond
                            (and body-msg-str (.contains body-msg-str "You have exceeded"))
                            "Cota de API excedida. Use outra chave API."
                            (and body-msg-str (.contains body-msg-str "tournament exists but is not active at the moment"))
                            "Torneio existe, mas não está ativo no momento."
                            ;; Additional error messages specific to 'torneios' can be added here
                            :else
                            body-msg-str)]
        (if (or (= 400 status) (= 429 status))
          (do
            (println (str "Erro ao buscar " req-tipo ": " error-message))
            {:error error-message, (keyword req-tipo "-id") extra :status status}))))))



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

(defn filtrar-odds-outcomes
  "Filtra chaves irrelevantes dos outcomes e extrai 'outcomeName' e 'price'."
  [outcomes]
  (into {}
        (map (fn [[outcome-id outcome-data]]
               (let [{:keys [outcomeName bookmakers]} outcome-data
                     price (get-in bookmakers [:bestPrice :price])]
                 [outcome-id {:outcomeName outcomeName
                              :price       price}]))
             outcomes)))

(defn filtrar-odds-mercados
  "Filtra mercados pelo oddsType e remove besteira inútil"
  [markets]
  (let [tipos-validos #{"Over/Under" "3Way"}]
    (into {}
          (comp
           (filter (fn [[_ dados-mercados]]
                     (tipos-validos (:oddsType dados-mercados))))
           (map (fn [[market-id dados-mercados]]
                  (let [{:keys [marketName oddsType handicap outcomes]} dados-mercados
                        outcomes-filtrado (filtrar-odds-outcomes outcomes)]
                    [market-id {:marketName marketName
                                :oddsType   oddsType
                                :handicap   handicap
                                :outcomes   outcomes-filtrado}]))))
          markets)))

(defn filtrar-odds
  "Filtra chaves irrelevantes e retorna os dados organizados, incluindo apenas mercados com oddsType 'Over/Under' ou '3Way'"
  [body eventId]
  (let [{:keys [date participant1 participant2 eventStatus eventId tournamentId timestamp markets]} body
        mercados-filtrados (filtrar-odds-mercados markets)]
    {:date          date
     :participant1  participant1
     :participant2  participant2
     :eventStatus   eventStatus
     :eventId       eventId
     :tournamentId  tournamentId
     :timestamp     timestamp
     :markets       mercados-filtrados}))


(defn get-torneios
  "Retorna a lista de torneios de futebol, usando cache ou API"
  [sport]
  (check-dir-cache)
  (let [cache (processar-cache "torneios" sport)
        cache-arquivo (str "cache/tournaments-" sport ".json")]
    (if cache
      cache
      (try-catch-api "torneios" sport cache-arquivo filtrar-torneios))))


(defn get-eventos
  "Retorna a lista de eventos a partir de certo torneio, usando cache ou API"
  [tournamentId]
  (check-dir-cache)
  (let [cache (processar-cache "eventos" tournamentId)
        cache-arquivo (str "cache/events-" tournamentId ".json")]
    (if cache
      cache
      (try-catch-api "eventos" tournamentId cache-arquivo filtrar-eventos))))


(defn get-evento-odds
  "Retorna mercados e odds a partir de certo evento, usando cache ou API"
  [eventId]
  (check-dir-cache)
  (let [cache (processar-cache "odds" eventId)
        cache-arquivo (str "cache/odds-" eventId ".json")]
    (if cache
      cache
      (try-catch-api "odds" eventId cache-arquivo filtrar-odds))))
  


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; (println (get-torneios "soccer")))
  (println (get-evento-odds "id100032548215167")))
  