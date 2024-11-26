// Exemplo básico para conectar com a API
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
