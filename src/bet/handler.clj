(ns bet.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [bet.db :as db]
            [bet.core :as core]
            [ring.util.response :refer [response content-type]]))

(defn para-json [conteudo & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string conteudo)})


(defn retorna-json-body [requisicao]
  (try
    (json/parse-string (slurp (:body requisicao)) true)
    (catch clojure.lang.ExceptionInfo e
      {:erro "Formato de JSON inválido", :status 400})))

(defn depositar [requisicao]
  (let [body (retorna-json-body requisicao)
        valor (:valor body)
        resultado (db/processar-deposito valor)]
    (if (:erro resultado)
      (para-json {:erro (:erro resultado)} 400)
      (para-json {:mensagem "Depósito realizado com sucesso!"
                  :saldo-atual @db/saldo}))))

(defn apostar [requisicao]
  (let [body (retorna-json-body requisicao)
        valor (:valor (:valor body))
        odds (:odds body)
        evento (:evento body)
        resultado (db/processar-aposta valor odds evento)]
    (if (:erro resultado)
      (para-json {:erro (:erro resultado)} 400)
      (para-json {:mensagem "Aposta realizada com sucesso!"
                  :saldo-atual @db/saldo}))))


(defroutes app-routes
  (GET "/" []
    (-> (slurp "src/web/index.html")
        (response)
        (content-type "text/html")))
  
  (GET "/styles.css" []
    (-> (slurp "src/web/styles.css")
        (response)
        (content-type "text/css")))
  
  (GET "/script.js" []
    (-> (slurp "src/web/script.js")
        (response)
        (content-type "application/javascript")))
  
  (GET "/torneios" [esporte]
    (cond
          (= esporte "futebol") (para-json (core/get-torneios "soccer")) ;; Chama função para "futebol"
          (= esporte "basquete") (para-json (core/get-torneios "basketball")) ;; Chama função para "basquete"
          :else (para-json {:erro "Esporte inválido. Use 'futebol' ou 'basquete'."})))
  
  (GET "/eventos" [tournamentId]
    (cond
      (nil? tournamentId)
      (para-json {:erro "Parâmetro 'tournamentId' é necessário."})
      :else
      (para-json (core/get-eventos tournamentId))))
  
  (GET "/odds" [eventId]
    (cond
      (nil? eventId)
      (para-json {:erro "Parâmetro 'eventId' é necessário."})
      :else
      (para-json (core/get-evento-odds eventId))))
  
  (GET "/transacoes" [] (para-json (db/get-transacoes))) 
  (GET "/saldo" [] (para-json {:saldo @db/saldo}))
  
  (POST "/depositar" requisicao
    (depositar requisicao))
  (POST "/apostar" requisicao
    (apostar requisicao))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
