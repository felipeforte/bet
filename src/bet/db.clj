(ns bet.db)

(def tipos-apostas [{:id 1 :nome "Over/Under" :descricao "Aposte se o total de pontos/gols estará acima ou abaixo do valor definido."}
                    {:id 2 :nome "1X2 (Match Odds)" :descricao "Aposte no resultado final: vitória do time 1, empate, ou vitória do time 2."}])

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