document.addEventListener('DOMContentLoaded', () => {
    const esporteRadios = document.querySelectorAll('input[name="esporte"]');
    const esporteDiv = document.querySelector('.esporte');
    const torneioSelect = document.getElementById('torneio');
    const eventosSelect = document.getElementById('eventos-lista');
    const torneiosDiv = document.querySelector('.torneios');
    const eventosDiv = document.querySelector('.eventos');
    const mercadosDiv = document.querySelector('.mercados');

    /**
     * Exibe ou oculta o indicador de carregamento em um contêiner.
     * @param {HTMLElement} container - O contêiner onde o indicador será exibido.
     * @param {boolean} isLoading - Se `true`, exibe o indicador; caso contrário, oculta.
     */
    const setLoadingState = (container, isLoading) => {
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
    };

    /**
     * Exibe ou oculta o indicador de carregamento em um contêiner.
     * @param {HTMLElement} container - O contêiner onde o indicador será exibido.
     * @param {boolean} isError - Se `true`, exibe o indicador; caso contrário, oculta.
     * @param {String} msg - msg de erro 
     */
    const setErrorState = (container, isError, msg) => {
        let errorSpan = container.querySelector('.error-indicator');
    
        if (!errorSpan) {
            errorSpan = document.createElement('span');
            errorSpan.classList.add('error-indicator');
            errorSpan.style.color = 'red';
            container.appendChild(errorSpan);
        }
    
        // Update the error message only if 'msg' is provided
        if (typeof msg !== 'undefined') {
            errorSpan.textContent = msg;
        }
    
        errorSpan.style.display = isError ? 'inline' : 'none';
    };
    

    /**
     * Busca torneios para o esporte selecionado.
     * @param {string} selectedSport - O esporte selecionado.
     */
    const fetchTournaments = async (selectedSport) => {
        setErrorState(esporteDiv,false);
        setLoadingState(esporteDiv, true);
        torneiosDiv.classList.add('hidden');
        eventosDiv.classList.add('hidden');
        mercadosDiv.classList.add('hidden');

        try {
            const response = await fetch(`http://localhost:3000/torneios?esporte=${selectedSport}`);
            if (!response.ok) throw new Error('Erro ao buscar torneios.');

            const data = await response.json();
            torneioSelect.innerHTML = '<option value="" disabled selected>Selecione o torneio</option>';

            if (data.error) {
                setLoadingState(esporteDiv, false);
                setErrorState(esporteDiv,true,data.error)
                torneiosDiv.classList.add('hidden');
                eventosDiv.classList.add('hidden');
                mercadosDiv.classList.add('hidden');
                return;
            }

            setErrorState(torneiosDiv,false);

            Object.values(data).forEach((tournament) => {
                const option = document.createElement('option');
                option.value = tournament.tournamentId;
                option.textContent = `${tournament.name} (${tournament.categoryName})`;
                torneioSelect.appendChild(option);
            });

            setLoadingState(esporteDiv, false);
            torneiosDiv.classList.remove('hidden');

        } catch (error) {
            console.error(error);
            alert('Erro ao carregar torneios. Tente novamente.');
        } finally {
            setLoadingState(esporteDiv, false);
        }
    };

    /**
     * Busca eventos para o torneio selecionado.
     * @param {string} tournamentId - O ID do torneio selecionado.
     */
    const fetchEvents = async (tournamentId) => {
        setErrorState(torneiosDiv,false);
        setLoadingState(torneiosDiv, true);mercadosDiv
        eventosDiv.classList.add('hidden');
        mercadosDiv.classList.add('hidden');

        try {
            const response = await fetch(`http://localhost:3000/eventos?tournamentId=${tournamentId}`); 
            if (!response.ok) throw new Error('Erro ao buscar eventos.');

            const data = await response.json();
            eventosSelect.innerHTML = '<option value="" disabled selected>Escolha um evento</option>';

            if (data.error) {
                setLoadingState(torneiosDiv, false);
                setErrorState(torneiosDiv,true,data.error)
                eventosDiv.classList.add('hidden');
                mercadosDiv.classList.add('hidden');
                return;
            }

            setErrorState(torneiosDiv,false);

            Object.values(data).forEach((event) => {
                const option = document.createElement('option');
                option.value = event.eventId;
                option.textContent = `${event.participant1} vs ${event.participant2} - ${event.date}`;
                eventosSelect.appendChild(option);
            });

            eventosDiv.classList.remove('hidden');
        } catch (error) {
            console.error(error);
            alert('Erro ao carregar eventos. Tente novamente.');
        } finally {
            setLoadingState(torneiosDiv, false);
        }
    };

    /**
     * Busca mercados para o evento selecionado.
     * @param {string} eventId - O ID do evento selecionado.
     */
    const fetchMarkets = async (eventId) => {
        setErrorState(eventosDiv,false);
        setLoadingState(eventosDiv, true);
        mercadosDiv.classList.add('hidden');

        try {
            const response = await fetch(`http://localhost:3000/odds?eventId=${eventId}`);
            if (!response.ok) throw new Error('Erro ao buscar mercados.');

            const data = await response.json();
            if (!data || Object.keys(data).length === 0) {
                alert('Nenhum mercado disponível para este evento.');
                return;
            }

            if (data.error) {
                setLoadingState(eventosDiv, false);
                setErrorState(eventosDiv,true,data.error)
                mercadosDiv.classList.add('hidden');
                return;
            }

            setErrorState(eventosDiv,false);

            // Adiciona mercados ao DOM
            Object.values(data.markets).forEach((market) => {
                const fieldset = document.createElement('fieldset');
                const legend = document.createElement('legend');
                legend.textContent = `${market.marketName} (${market.handicap})`;
                fieldset.appendChild(legend);

                Object.values(market.outcomes).forEach((outcome) => {
                    const label = document.createElement('label');
                    const input = document.createElement('input');
                    input.type = 'radio';
                    input.name = `mercado-${market.marketName}`;
                    input.value = outcome.outcomeName.toLowerCase().replace(/\s+/g, '-');
                    label.appendChild(input);
                    label.appendChild(document.createTextNode(`${outcome.outcomeName} (${outcome.price})`));
                    fieldset.appendChild(label);
                });

                mercadosDiv.appendChild(fieldset);
            });

            mercadosDiv.classList.remove('hidden');
        } catch (error) {
            console.error(error);
            alert('Erro ao carregar mercados. Tente novamente.');
        } finally {
            setLoadingState(eventosDiv, false);
        }
    };

    // Event listeners
    esporteRadios.forEach((radio) => {
        radio.addEventListener('change', () => {
            if (radio.checked) {
                const selectedSport = document.querySelector('input[name="esporte"]:checked').value;
                fetchTournaments(selectedSport);
            }
        });
    });

    torneioSelect.addEventListener('change', () => {
        const tournamentId = torneioSelect.value;
        if (tournamentId) {
            fetchEvents(tournamentId);
        }
    });

    eventosSelect.addEventListener('change', () => {
        const eventId = eventosSelect.value;
        if (eventId) {
            fetchMarkets(eventId);
        }
    });
});

document.querySelectorAll('.btn-validar').forEach(button => {
    button.addEventListener('click', () => {
        const tipoAposta = button.previousElementSibling.querySelector('#aposta-tipo').value;
        const saldoElemento = document.getElementById('saldo-usuario');
        const saldoAtual = parseFloat(saldoElemento.textContent);

        if (saldoAtual >= 10) { // Simulação de aposta
            fetch('http://localhost:3000/validar-aposta', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ tipoAposta })
            })
            .then(response => response.json())
            .then(data => {
                alert('Aposta validada com sucesso!');
                saldoElemento.textContent = (saldoAtual - 10).toFixed(2);
            })
            .catch(error => console.error('Erro ao validar aposta:', error));
        } else {
            alert('Saldo insuficiente para apostar.');
        }
    });
});

document.getElementById('btn-depositar').addEventListener('click', function() {
    var depositoCampo = document.getElementById('deposito-campo');
    var valor = parseFloat(depositoCampo.value);

    if (isNaN(valor) || valor <= 0) {
        alert('Por favor, insira um valor válido para depósito.');
        return;
    }

    fetch('http://localhost:3000/depositar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ valor: valor })
    })
    .then(response => response.json())
    .then(data => {
        atualizarSaldo();
    })
    .catch(error => {
        console.error('Erro ao depositar:', error);
    });
});

function atualizarSaldo() {
    fetch('http://localhost:3000/saldo')
    .then(response => response.json())
    .then(data => {
        var saldo = parseFloat(data.saldo);
        var saldoFormatado = saldo.toFixed(2).replace('.', ',');
        document.getElementById('saldo-usuario').textContent = saldoFormatado;
    })
    .catch(error => {
        console.error('Erro ao obter o saldo:', error);
    });
}

// Atualiza o saldo ao carregar a página
atualizarSaldo();

// Reseta todos os campos
window.addEventListener('load', () => {
    const radios = document.querySelectorAll('input[type="radio"]');
    radios.forEach(radio => {
        radio.checked = false;
    });
});