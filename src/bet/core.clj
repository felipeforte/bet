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

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (get-json-sports)
  )