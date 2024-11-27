(ns bet.core
  (:gen-class)
  (:require [clj-http.client :as client]
           [cheshire.core :as json]
           [clojure.java.io :as io]))



(def disponiveis {:torneios #{325, 1562}})

(def api-key "c9cb38318cmsh9607b632446376bp113300jsnc5cf5f7e8aca")

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
        json-data (json/parse-string json-string true)]
    (if (or (empty? json-data) (= json-data {}))
      false ;; Se o cache estiver vazio ou for um mapa vazio, retorna false
      (if (and (:timestamp json-data) (> (count (keys json-data)) 1))
        (let [timestamp (:timestamp json-data)
              now (System/currentTimeMillis)
              diff-tempo (- now timestamp)]
          (if (< diff-tempo (* 3600 1000))
            (do
              (println "Usando cache disponível...")
              json-data) ;; Cache válido, retorna o conteúdo
            false)) ;; Cache expirado, retorna false
        false)))) ;; Se não tiver timestamp, retorna false

(defn api-get
  "Faz uma chamada GET para a API pra deixar o código limpo"
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



(defn filtrar-torneios [body]
  (into {} (filter (fn [[_ v]] (= (:categoryName v) "Brazil")) body)))

(defn filtrar-eventos [body tournamentId]
  (into {} ;; Filtra as chaves irrelevantes e organiza os dados
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
  [esporte]
  (check-dir-cache)
  (let [cache (processar-cache "torneios" esporte)]
    (if cache
      (filtrar-torneios cache) ;; Cache válido, usa os dados
      (let [resp (api-get "torneios" esporte) ;; Caso não tenha cache válido, faz a chamada à API
            body (assoc (json/parse-string (:body resp) true) :timestamp (System/currentTimeMillis))]
        (println "Buscando torneios via API externa...")
        (spit (str "cache/tournaments-" esporte ".json") (json/generate-string body true)) ;; Salva em arquivo com timestamp
        (filtrar-torneios body)))))

(defn get-eventos
  "Retorna a lista de eventos a partir de certo torneio, usando cache ou API"
  [tournamentId]
  (check-dir-cache)
  (let [cache (processar-cache "eventos" tournamentId)]
    (if cache
      (filtrar-eventos cache tournamentId) ;; Cache válido, usa os dados
      (let [resp (api-get "eventos" tournamentId) ;; Caso não tenha cache válido, faz a chamada à API
            body (assoc (json/parse-string (:body resp) true) :timestamp (System/currentTimeMillis))]
        (println "Buscando eventos via API externa...")
        (spit (str "cache/events-" tournamentId ".json") (json/generate-string body true)) ;; Salva em arquivo com timestamp
        (filtrar-eventos body tournamentId)))))

(defn get-evento-odds 
  "Retorna mercados e odds a partir de certo evento, usando cache ou API"
  [eventId]
  (check-dir-cache)
  (let [cache (processar-cache "odds" eventId)]
    (if cache
      cache ;; Cache válido, usa os dados
      (let [resp (api-get "odds" eventId) ;; Caso não tenha cache válido, faz a chamada à API
            body (assoc (json/parse-string (:body resp) true) :timestamp (System/currentTimeMillis))]
        (println "Buscando odds via API externa...")
        (spit (str "cache/odds-" eventId ".json") (json/generate-string body true)) ;; Salva em arquivo com timestamp
        body))))
  


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; (println (get-event-odds "id100032548215167"))
  (println (get-evento-odds "id100039048424611")))
  