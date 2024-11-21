(ns bet.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [bet.db :as db]))

(defn como-json [conteudo & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string conteudo)})

(defn processar-json-body [request]
  (try
    (json/parse-string (slurp (:body request)) true)
    (catch Exception e
      {:error "Invalid JSON"})))



(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/saldo" [] (como-json {:saldo @db/saldo}))
  (POST "/depositar" request
    (let [body (processar-json-body request)
          valor (:valor body)
          resultado (db/depositar valor)]
      (if (:erro resultado)
        (como-json {:erro (:erro resultado)} 400)
        (como-json {:mensagem "Depósito realizado com sucesso!"
                    :novo-saldo (:novo-saldo resultado)}))))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
