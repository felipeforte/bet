(ns bet.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [bet.db :as db]
            [bet.core :as core]))

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
        valor (:valor body)
        odds (:odds body)
        evento (:evento body)
        resultado (db/processar-aposta valor odds evento)]
    (if (:erro resultado)
      (para-json {:erro (:erro resultado)} 400)
      (para-json {:mensagem "Aposta realizada com sucesso!"
                  :saldo-atual @db/saldo}))))


(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/saldo" [] (para-json {:saldo @db/saldo}))
  (POST "/depositar" requisicao
    (depositar requisicao))
  (POST "/apostar" requisicao
    (apostar requisicao))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
