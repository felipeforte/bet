(ns bet.db
  (:require [bet.core :as core]))

(def tipos-apostas [{:id 1 :nome "overunder" :descricao "Total de pontos/gols acima ou abaixo do valor definido"}
                    {:id 2 :nome "1X2" :descricao "Vitória do time 1, empate, ou vitória do time 2."}])

(def saldo (atom 0))

(def transacoes (atom []))

(defn registrar-transacao [tipo valor]
  (swap! transacoes conj {:tipo tipo :valor valor :horario (System/currentTimeMillis)}))

(defn validar-deposito [valor]
  (cond
    (not (number? valor))                   {:erro "O valor do depósito deve ser um número"}
    (or (zero? valor) (neg? valor))         {:erro "O valor precisa ser maior que zero."}
    :else                                   (swap! saldo + valor)
     ))

(defn atualizar-saldo [saldo-atom valor]
  (swap! saldo-atom + valor))

(defn processar-deposito [valor]
  (let [erro (validar-deposito valor)]
    (if erro
      erro
      {:mensagem "Depósito realizado com sucesso"
       :saldo-atual saldo})))

(defn validar-valor-aposta [valor]
  (if (or (not (number? valor)) (neg? valor)) ; Checa se n eh número ou negativo
        {:erro "O valor da aposta deve ser um número maior que zero"} 400 ; Se n for número ou negativo, devolve erro
        (let [novo-saldo (swap! saldo - valor)]
          (if (neg? novo-saldo) ; Se o novo saldo for negativo (valor maior que em conta)
            (do
              (swap! saldo + valor) ; Desfaz a subtração
              {:erro "Saldo insuficiente"} 400)
            (do
              (registrar-transacao "aposta" valor)
              )))))

(defn validar-odds-aposta [odds]
  )

(defn processar-aposta [valor odds evento]
  (let [erro (validar-valor-aposta valor)]
    (if erro
      erro
      {:mensagem "Aposta registrada com sucesso"
       :saldo-atual saldo})))


(defn apostar [valor odds evento]
    ;; (if (or (not (number? valor)) (neg? valor)) ; Checa se n eh número ou negativo
    ;;   (para-json {:erro "O valor da aposta deve ser um número maior que zero"} 400) ; Se n for número ou negativo, devolve erro
    ;;   (let [novo-saldo (swap! db/saldo - valor)]
    ;;     (if (neg? novo-saldo) ; Se o novo saldo for negativo (valor maior que em conta)
    ;;       (do
    ;;         (swap! db/saldo + valor) ; Desfaz a subtração
    ;;         (para-json {:erro "Saldo insuficiente"} 400))
    ;;       (do
    ;;         (registrar-transacao "aposta" valor)
    ;;         (para-json {:mensagem "Aposta registrada com sucesso"
    ;;                     :saldo-atual novo-saldo}))))))
  (println "VALOR:")
  (println valor)
  (println "ODDS:")
  (println odds)
  (println "EVENTO:")
  (println evento)
    )