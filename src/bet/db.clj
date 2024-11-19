(ns bet.db)

(def apostas (atom []))
(def eventos (atom {}))

(defn registrar-aposta [aposta]
  (let [nova-aposta (assoc aposta :id (inc (count @apostas)) :status "pendente")]
    (swap! apostas conj nova-aposta)
    nova-aposta))

(defn listar-apostas []
  @apostas)

(defn limpar-apostas []
  (reset! apostas []))

(defn atualizar-status-aposta [id status]
  (swap! apostas
         (fn [aps]
           (map (fn [a]
                  (if (= (:id a) id)
                    (assoc a :status status)
                    a))
                aps))))

(defn obter-aposta-por-id [id]
  (first (filter #(= (:id %) id) @apostas)))

;; Funções para gerenciar eventos
(defn carregar-eventos [dados]
  (reset! eventos dados))

(defn listar-eventos []
  (get @eventos "events"))

(defn obter-evento-por-id [id]
  (get-in @eventos ["events" id]))