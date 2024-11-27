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

document.addEventListener('DOMContentLoaded', () => {
    const esporteRadios = document.querySelectorAll('input[name="esporte"]');
    const torneioSelect = document.getElementById('torneio');
    const eventosSelect = document.getElementById('eventos-lista');
    const torneiosDiv = document.querySelector('.torneios');
    const eventosDiv = document.querySelector('.eventos');
    const mercadosDiv = document.querySelector('.mercados');
    
    // Definir a div de erro fora das funções
    const errorMessageSpan = document.createElement('span');  // Div for error message

    // Function to fetch tournaments based on selected sport
    const fetchTournaments = async (selectedSport) => {
        try {
            const response = await fetch(`http://localhost:3000/torneios?esporte=${selectedSport}`);
            if (!response.ok) {
                throw new Error("Failed to fetch tournaments");
            }

            const data = await response.json();

            // Clear existing options in the tournament dropdown
            torneioSelect.innerHTML = '<option value="" disabled selected>Selecione o torneio</option>';

            // Populate the tournament dropdown
            Object.values(data).forEach((tournament) => {
                const option = document.createElement("option");
                option.value = tournament.tournamentId;
                option.textContent = `${tournament.name} (${tournament.categoryName})`;
                torneioSelect.appendChild(option);
            });

            torneiosDiv.classList.remove('hidden');
        } catch (error) {
            console.error("Error fetching tournaments:", error);
            alert("Erro ao carregar torneios. Tente novamente.");
        }
    };

    // Function to fetch events based on selected tournament
    const fetchEvents = async (tournamentId) => {
        try {
            const response = await fetch(`http://localhost:3000/eventos?tournamentId=${tournamentId}`);
            const data = await response.json();

            // Check if there's an error in the response
            if (data.error) {
                // Show error message next to the torneioSelect dropdown
                errorMessageSpan.textContent = data.error;
                errorMessageSpan.style.color = 'red';
                torneioSelect.parentNode.appendChild(errorMessageSpan);  // Add the error message div after the select
                eventosDiv.classList.add('hidden');  // Hide events div if there's an error
                return;  // Exit the function if there's an error
            }

            // If no error, clear the error message (if any)
            errorMessageSpan.textContent = '';
            errorMessageSpan.style.color = ''; // Reset error styling

            // Clear existing options in the event dropdown
            eventosSelect.innerHTML = '<option value="" disabled selected>Escolha um evento</option>';

            // Populate the events dropdown
            Object.values(data).forEach((event) => {
                const option = document.createElement("option");
                option.value = event.eventId;
                option.textContent = `${event.participant1} vs ${event.participant2} - ${event.date}`;
                eventosSelect.appendChild(option);
            });

            // Show the eventos container
            eventosDiv.classList.remove('hidden');
        } catch (error) {
            console.error("Error fetching events:", error);
            alert("Erro ao carregar eventos. Tente novamente.");
        }
    };

    // Event listener for changing sport
    esporteRadios.forEach(radio => {
        radio.addEventListener('change', () => {
            if (radio.checked) {
                const selectedSport = document.querySelector('input[name="esporte"]:checked').value;
                fetchTournaments(selectedSport);
            }
        });
    });

    // Event listener for changing tournament
    torneioSelect.addEventListener('change', () => {
        const tournamentId = torneioSelect.value;
        if (tournamentId) {
            fetchEvents(tournamentId);  // Fetch events based on selected tournament
        }
    });

    // Event listener for changing event
    eventosSelect.addEventListener('change', () => {
        if (eventosSelect.value) {
            mercadosDiv.classList.remove('hidden');
        }
    });
});




