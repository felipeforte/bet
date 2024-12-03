(ns bet.db
  (:require [bet.core :as core]))

(def saldo (atom 0))

(def transacoes (atom []))

(defn registrar-transacao [tipo valor detalhes]
  (swap! transacoes conj {:tipo     tipo
                          :valor    valor
                          :detalhes detalhes})
  (println transacoes))


(defn validar-deposito [valor]
  (cond
    (not (number? valor))             {:erro "O valor do depósito deve ser um número"}
    (or (zero? valor) (neg? valor))   {:erro "O valor precisa ser maior que zero."}
    :else                             nil))

(defn processar-deposito [valor]
  (let [erro (validar-deposito valor)]
    (if erro
      erro
      (do
        (swap! saldo + valor)
        (registrar-transacao "depósito" valor {})
        {:mensagem "Depósito realizado com sucesso"
         :saldo-atual @saldo}))))

(defn validar-valor-aposta [valor]
  (cond
    (not (number? valor)) {:erro "O valor da aposta deve ser um número."}
    (<= valor 0)          {:erro "O valor da aposta deve ser maior que zero."}
    (> valor @saldo)      {:erro "Saldo insuficiente."}
    :else                 nil))

(defn validar-odds [odds]
  (if (and (map? odds)
           (:marketName odds)
           (:oddsType odds)
           (:outcome odds)
           (:price (:outcome odds)))
    nil
    {:erro "Odds inválidas."}))

(defn validar-evento [evento]
  (if (and (map? evento)
           (:eventId evento)
           (:participant1 evento)
           (:participant2 evento))
    nil
    {:erro "Evento inválido."}))

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