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

    // Make the main function async to handle the fetch request
    const fetchTournaments = async (selectedSport) => {
        try {
            const response = await fetch(`http://localhost:3000/torneios?esporte=${selectedSport}`);
            if (!response.ok) {
                throw new Error("Falha na resposta do servidor");
            }

            const data = await response.json();

            // Limpar opções, pra não sobrepor nas mudanças
            torneioSelect.innerHTML = '<option value="" disabled selected>Selecione o torneio</option>';

            // Colocar elementos em dropdown
            Object.values(data).forEach((tournament) => {
                const option = document.createElement("option");
                option.value = tournament.tournamentId;
                option.textContent = `${tournament.name} (${tournament.categoryName})`;
                torneioSelect.appendChild(option);
            });

            // Mostra o container de elementos
            torneiosDiv.classList.remove('hidden');
        } catch (error) {
            console.error("Falha em requisitar dados de torneios:", error);
            alert("Erro ao carregar torneios. Tente novamente.");
        }
    };

    // Event listener for changing sports
    esporteRadios.forEach(radio => {
        radio.addEventListener('change', () => {
            if (radio.checked) {
                const selectedSport = document.querySelector('input[name="esporte"]:checked').value;
                fetchTournaments(selectedSport);
            }
        });
    });

    // Event listener for selecting a tournament
    torneioSelect.addEventListener('change', () => {
        if (torneioSelect.value) {
            eventosDiv.classList.remove('hidden');
        }
    });

    // Event listener for selecting an event
    eventosSelect.addEventListener('change', () => {
        if (eventosSelect.value) {
            mercadosDiv.classList.remove('hidden');
        }
    });
});


document.querySelectorAll('input[name="esporte"]').forEach((radio) => {
    radio.addEventListener("change", async () => {
        const selectedSport = document.querySelector('input[name="esporte"]:checked').value;
        const torneioDropdown = document.getElementById("torneio");
        const torneioContainer = document.querySelector(".torneios");

        // Clear existing options
        torneioDropdown.innerHTML = '<option value="" disabled selected>Selecione o torneio</option>';

        try {
            // Fetch tournaments from the API
            const response = await fetch(`http://localhost:3000/torneios?esporte=${selectedSport}`);
            if (!response.ok) {
                throw new Error("Failed to fetch tournaments");
            }

            const data = await response.json();

            // Populate the dropdown
            Object.values(data).forEach((tournament) => {
                const option = document.createElement("option");
                option.value = tournament.tournamentId;
                option.textContent = `${tournament.name} (${tournament.categoryName})`;
                torneioDropdown.appendChild(option);
            });

            // Make the dropdown visible
            torneioContainer.classList.remove("hidden");
        } catch (error) {
            console.error("Error fetching tournaments:", error);
            alert("Erro ao carregar torneios. Tente novamente.");
        }
    });
});



