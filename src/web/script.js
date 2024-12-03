const apiService = {
    async getTorneios(selectedSport) {
        const response = await fetch(`http://localhost:3000/torneios?esporte=${selectedSport}`);
        if (!response.ok) throw new Error('Erro ao buscar torneios.');
        const data = await response.json();
        if (data.error) throw new Error(data.error);
        return data;
    },

    async getEventos(tournamentId) {
        const response = await fetch(`http://localhost:3000/eventos?tournamentId=${tournamentId}`);
        if (!response.ok) throw new Error('Erro ao buscar eventos.');
        const data = await response.json();
        if (data.error) throw new Error(data.error);
        return data;
    },

    async getMercados(eventId) {
        const response = await fetch(`http://localhost:3000/odds?eventId=${eventId}`);
        if (!response.ok) throw new Error('Erro ao buscar mercados.');
        const data = await response.json();
        if (data.error) throw new Error(data.error);
        return data;
    },

    async postAposta(payload) {
        const response = await fetch('http://localhost:3000/apostar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });
        console.log(JSON.stringify(payload));
        if (!response.ok) throw new Error('Erro ao realizar a aposta.');
        return response.json();
    },

    async postDeposito(valor) {
        const response = await fetch('http://localhost:3000/depositar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ valor }),
        });
        if (!response.ok) throw new Error('Erro ao depositar.');
        return response.json();
    },

    async getSaldo() {
        const response = await fetch('http://localhost:3000/saldo');
        if (!response.ok) throw new Error('Erro ao obter o saldo.');
        return response.json();
    },

    async validateBet(tipoAposta) {
        const response = await fetch('http://localhost:3000/validar-aposta', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ tipoAposta }),
        });
        if (!response.ok) throw new Error('Erro ao validar aposta.');
        return response.json();
    },
};

const uiHandlers = {
    setCarregando(container, isLoading) {
        let loadingSpan = container.querySelector('.loading-indicator');
        if (!loadingSpan) {
        loadingSpan = document.createElement('span');
        loadingSpan.classList.add('loading-indicator');
        loadingSpan.textContent = 'Carregando...';
        loadingSpan.style.marginLeft = '10px';
        loadingSpan.style.color = '#888';
        container.appendChild(loadingSpan);
        }
        loadingSpan.style.display = isLoading ? 'inline' : 'none';
    },

    setErro(container, isError, msg) {
        let errorSpan = container.querySelector('.error-indicator');
        if (!errorSpan) {
        errorSpan = document.createElement('span');
        errorSpan.classList.add('error-indicator');
        errorSpan.style.color = 'red';
        container.appendChild(errorSpan);
        }
        if (typeof msg !== 'undefined') {
        errorSpan.textContent = msg;
        }
        errorSpan.style.display = isError ? 'inline' : 'none';
    },

    popularTorneios(tournaments, torneioSelect) {
        torneioSelect.innerHTML = '<option value="" disabled selected>Selecione o torneio</option>';
        tournaments.forEach((tournament) => {
            const option = document.createElement('option');
            option.value = tournament.tournamentId;
            option.textContent = `${tournament.name} (${tournament.categoryName})`;
            torneioSelect.appendChild(option);
        });
    },

    populateEventos(events, eventosSelect) {
        eventosSelect.innerHTML = '<option value="" disabled selected>Escolha um evento</option>';
        events.forEach((event) => {
            const option = document.createElement('option');
            option.value = event.eventId;
            option.textContent = `${event.participant1} vs ${event.participant2} - ${event.date}`;
            eventosSelect.appendChild(option);
        });
    },

    showBetForm(marketData, mercadosDiv, eventoData, processaFormularioAposta) {
    // Limpa o conteúdo anterior
    mercadosDiv.innerHTML = '';

    // Cria o formulário
    const form = document.createElement('form');
    form.id = 'bet-form';

    // Label e select para categoria
    const labelCategory = document.createElement('label');
    labelCategory.setAttribute('for', 'bet-category');
    labelCategory.textContent = 'Selecione um Mercado:';

    const betCategorySelect = document.createElement('select');
    betCategorySelect.id = 'bet-category';
    betCategorySelect.name = 'bet-category';

    // Opção padrão para categoria
    const defaultCategoryOption = document.createElement('option');
    defaultCategoryOption.value = '';
    defaultCategoryOption.disabled = true;
    defaultCategoryOption.selected = true;
    defaultCategoryOption.textContent = 'Selecione um mercado';
    betCategorySelect.appendChild(defaultCategoryOption);

    // Mapeamento dos mercados
    const marketsMap = {};
    const marketsDataMap = {};

    // Popula os mercados
    const marketsArray = Object.values(marketData.markets);
    marketsArray.forEach((market) => {
        const marketKey = market.marketName + '|' + market.handicap;
        const option = document.createElement('option');
        option.value = marketKey;
        option.textContent = `${market.marketName} (${market.handicap || 'N/A'})`;
        betCategorySelect.appendChild(option);
        marketsMap[marketKey] = market.outcomes;
        marketsDataMap[marketKey] = market;
    });

    // Label e select para opções
    const labelOption = document.createElement('label');
    labelOption.setAttribute('for', 'bet-option');
    labelOption.textContent = 'Selecione uma Opção:';

    const betOptionSelect = document.createElement('select');
    betOptionSelect.id = 'bet-option';
    betOptionSelect.name = 'bet-option';

    // Opção padrão para resultado
    const defaultOptionOption = document.createElement('option');
    defaultOptionOption.value = '';
    defaultOptionOption.disabled = true;
    defaultOptionOption.selected = true;
    defaultOptionOption.textContent = 'Selecione uma opção';
    betOptionSelect.appendChild(defaultOptionOption);

    // Campo para valor da aposta
    const labelAmount = document.createElement('label');
    labelAmount.setAttribute('for', 'bet-amount');
    labelAmount.textContent = 'Valor da Aposta:';

    const betAmountInput = document.createElement('input');
    betAmountInput.type = 'number';
    betAmountInput.id = 'bet-amount';
    betAmountInput.name = 'bet-amount';
    betAmountInput.min = '1';
    betAmountInput.step = '0.01';

    // Botão de submissão
    const submitButton = document.createElement('button');
    submitButton.type = 'submit';
    submitButton.textContent = 'Fazer Aposta';

    // Monta o formulário
    form.appendChild(labelCategory);
    form.appendChild(betCategorySelect);
    form.appendChild(document.createElement('br'));
    form.appendChild(labelOption);
    form.appendChild(betOptionSelect);
    form.appendChild(document.createElement('br'));
    form.appendChild(labelAmount);
    form.appendChild(betAmountInput);
    form.appendChild(document.createElement('br'));
    form.appendChild(submitButton);

    mercadosDiv.appendChild(form);

    // Listener para mudança de mercado
    betCategorySelect.addEventListener('change', () => {
        const selectedMarketKey = betCategorySelect.value;
        betOptionSelect.innerHTML = '';
        betOptionSelect.appendChild(defaultOptionOption.cloneNode(true));

        const outcomes = marketsMap[selectedMarketKey];
        if (outcomes) {
            Object.entries(outcomes).forEach(([outcomeId, outcome]) => {
            const option = document.createElement('option');
            option.value = outcomeId;
            option.textContent = `${outcome.outcomeName} (${outcome.price})`;
            betOptionSelect.appendChild(option);
            });
        }
    });

    // Listener para submissão do formulário
    form.addEventListener('submit', (event) => {
        event.preventDefault();
        processaFormularioAposta(form, eventoData, marketsDataMap);
    });
    },

    atualizarElementoSaldo(saldo) {
        const saldoElemento = document.getElementById('saldo-usuario');
        const saldoFormatado = saldo.toFixed(2).replace('.', ',');
        saldoElemento.textContent = saldoFormatado;
    },
};

document.addEventListener('DOMContentLoaded', () => {
    // Atualiza o saldo ao carregar a página
    atualizarSaldo();
    
    // Referências aos elementos do DOM
    const esporteRadios = document.querySelectorAll('input[name="esporte"]');
    const esporteDiv = document.querySelector('.esporte');
    const torneioSelect = document.getElementById('torneio');
    const eventosSelect = document.getElementById('eventos-lista');
    const torneiosDiv = document.querySelector('.torneios');
    const eventosDiv = document.querySelector('.eventos');
    const mercadosDiv = document.querySelector('.mercados');

    

    // Reseta todos os campos ao carregar a página
    window.addEventListener('load', () => {
        const radios = document.querySelectorAll('input[type="radio"]');
        radios.forEach((radio) => {
            radio.checked = false;
        });
    });

    // Event listeners
    esporteRadios.forEach((radio) => {
        radio.addEventListener('change', () => {
        if (radio.checked) {
            const selectedSport = radio.value;
            handleSportChange(selectedSport);
        }
        });
    });

    torneioSelect.addEventListener('change', () => {
        const tournamentId = torneioSelect.value;
        if (tournamentId) {
            handleTournamentChange(tournamentId);
        }
    });

    eventosSelect.addEventListener('change', () => {
        const eventId = eventosSelect.value;
        if (eventId) {
            processaEventosUi(eventId);
        }
    });

    document.getElementById('btn-depositar').addEventListener('click', async () => {
        const depositoCampo = document.getElementById('deposito-campo');
        const valor = parseFloat(depositoCampo.value);

        if (isNaN(valor) || valor <= 0) {
            alert('Por favor, insira um valor válido para depósito.');
            return;
        }

        try {
            await apiService.postDeposito(valor);
            await atualizarSaldo();
        } catch (error) {
            console.error('Erro ao depositar:', error);
            alert('Erro ao depositar. Tente novamente.');
        }
    });

    document.querySelectorAll('.btn-validar').forEach((button) => {
        button.addEventListener('click', async () => {
            const tipoAposta = button.previousElementSibling.querySelector('#aposta-tipo').value;
            try {
                await apiService.validateBet(tipoAposta);
                alert('Aposta validada com sucesso!');
            } catch (error) {
                console.error('Erro ao validar aposta:', error);
                alert('Erro ao validar aposta.');
            }
        });
    });

    // Funções principais
    async function handleSportChange(selectedSport) {
        uiHandlers.setErro(esporteDiv, false);
        uiHandlers.setCarregando(esporteDiv, true);
        torneiosDiv.classList.add('hidden');
        eventosDiv.classList.add('hidden');
        mercadosDiv.classList.add('hidden');

        try {
            const tournaments = await apiService.getTorneios(selectedSport);
            uiHandlers.popularTorneios(Object.values(tournaments), torneioSelect);
            torneiosDiv.classList.remove('hidden');
        } catch (error) {
            console.error(error);
            uiHandlers.setErro(esporteDiv, true, error.message);
        } finally {
            uiHandlers.setCarregando(esporteDiv, false);
    }
    }

    async function handleTournamentChange(tournamentId) {
        uiHandlers.setErro(torneiosDiv, false);
        uiHandlers.setCarregando(torneiosDiv, true);
        eventosDiv.classList.add('hidden');
        mercadosDiv.classList.add('hidden');

        try {
            const events = await apiService.getEventos(tournamentId);
            uiHandlers.populateEventos(Object.values(events), eventosSelect);
            eventosDiv.classList.remove('hidden');
        } catch (error) {
            console.error(error);
            uiHandlers.setErro(torneiosDiv, true, error.message);
        } finally {
            uiHandlers.setCarregando(torneiosDiv, false);
        }
    }

    async function processaEventosUi(eventId) {
        uiHandlers.setErro(eventosDiv, false);
        uiHandlers.setCarregando(eventosDiv, true);
        mercadosDiv.classList.add('hidden');

        try {
            const marketData = await apiService.getMercados(eventId);

        if (!marketData || Object.keys(marketData).length === 0) {
            alert('Nenhum mercado disponível para este evento.');
            return;
        }

        // Dados do evento
        const eventoData = {
            date: marketData.date,
            participant1: marketData.participant1,
            participant2: marketData.participant2,
            eventStatus: marketData.eventStatus,
            eventId: marketData.eventId,
            tournamentId: marketData.tournamentId,
        };

        uiHandlers.showBetForm(marketData, mercadosDiv, eventoData, processaFormularioAposta);
        mercadosDiv.classList.remove('hidden');
        } catch (error) {
            console.error(error);
            uiHandlers.setErro(eventosDiv, true, error.message);
        } finally {
            uiHandlers.setCarregando(eventosDiv, false);
        }
    }

    async function processaFormularioAposta(form, eventoData, marketsDataMap) {
        const betCategorySelect = form.querySelector('#bet-category');
        const betOptionSelect = form.querySelector('#bet-option');
        const betAmountInput = form.querySelector('#bet-amount');

        const selectedMarketKey = betCategorySelect.value;
        const selectedOutcomeId = betOptionSelect.value;
        const betAmount = parseFloat(betAmountInput.value);

        if (!selectedMarketKey || !selectedOutcomeId || isNaN(betAmount) || betAmount <= 0) {
            alert('Por favor, selecione um mercado, uma opção e insira um valor válido para a aposta.');
            return;
        }

        // Recupera os dados do mercado e resultado selecionados
        const selectedMarket = marketsDataMap[selectedMarketKey];
        const selectedOutcome = selectedMarket.outcomes[selectedOutcomeId];

        // Prepara o payload
        const payload = {
        evento: eventoData,
        odds: {
            marketName: selectedMarket.marketName,
            oddsType: selectedMarket.oddsType,
            handicap: selectedMarket.handicap,
            outcome: {
                outcomeName: selectedOutcome.outcomeName,
                price: selectedOutcome.price,
            },
        },
        valor: {
            valor: betAmount,
            },
        };

        try {
            await apiService.postAposta(payload);
            // Atualiza o saldo após a aposta ser processada
            await atualizarSaldo();
            alert('Aposta realizada com sucesso!');
            form.reset();
        } catch (error) {
            console.error('Erro ao realizar a aposta:', error);
            alert('Erro ao realizar a aposta. Tente novamente.');
        }
    }

    async function atualizarSaldo() {
        try {
            console.log("Chamada função saldo atualizado")
            const data = await apiService.getSaldo();
            const saldo = parseFloat(data.saldo);
            uiHandlers.atualizarElementoSaldo(saldo);
        } catch (error) {
            console.error('Erro ao obter o saldo:', error);
            alert('Erro ao atualizar saldo.');
        }
    }
});
