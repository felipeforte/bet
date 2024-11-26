(ns bet.db
  (:require [bet.core :as core]))

(def saldo (atom 0))

(def transacoes (atom []))

(defn registrar-transacao [tipo valor]
  
  )

(defn validar-deposito [valor]
  (cond
    (not (number? valor))                   {:erro "O valor do depósito deve ser um número"}
    (or (zero? valor) (neg? valor))         {:erro "O valor precisa ser maior que zero."}
    :else                                   (swap! saldo + valor)
     ))

(defn processar-deposito [valor]
  (let [erro (validar-deposito valor)]
    (if erro
      erro
      {:mensagem "Depósito realizado com sucesso"
       :saldo-atual saldo})))

(defn validar-valor-aposta [valor]
  (cond
    (not (number? valor)) {:erro "O valor da aposta deve ser um número."}
    (<= valor 0)          {:erro "O valor da aposta deve ser maior que zero."}
    (> valor @saldo)      {:erro "Saldo insuficiente."}
    :else                 nil))


(defn validar-odds [odds])

(defn validar-evento [evento])


(defn processar-aposta [valor odds evento]
  (let [erro-valor (validar-valor-aposta valor)
        erro-odds (validar-odds odds)
        erro-evento (validar-evento evento)]
    (cond
      erro-valor erro-valor
      erro-odds erro-odds
      erro-evento erro-evento
      :else (do (swap! saldo - valor)
              {:mensagem "Aposta registrada com sucesso"
             :saldo-atual @saldo}))))



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