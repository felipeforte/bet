(ns bet.db)

(def saldo (atom 0))

(defn validar-deposito [valor]
  (cond
    (not (number? valor))                   {:erro "O valor do depósito deve ser um número"}
    (or (zero? valor) (neg? valor))         {:erro "O valor precisa ser maior que zero."}
    :else                                   nil
     ))

(defn atualizar-saldo [saldo-atom valor]
  (swap! saldo-atom + valor))

(defn depositar [valor]
  (let [erro (validar-deposito valor)]
    (if erro
      erro
      {:success true
          :novo-saldo (atualizar-saldo saldo valor)})))