document.addEventListener("DOMContentLoaded", function () {
    const loginContainer = document.getElementById('login-container');
    const gameContainer = document.getElementById('game-container');
    const restartContainer = document.getElementById('restart-container');
    const connectButton = document.getElementById('connect-button');
    const playerNameInput = document.getElementById('player-name');
    const gameArea = document.getElementById('game-area');
    const restartButton = document.getElementById('restart-button');
    const killCountElement = document.getElementById('kill-count');
    const playerColors = {};
    const botColors = {};
    const score_table = document.getElementById("score_table");
    let loggedInPlayer = null;
    const playerSpeed = 10;
    let killCount = 0;

    const socket = new SockJS('http://localhost:8080/game', {
        headers: {
            'ngrok-skip-browser-warning': 'true'
        }
    });
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/game', function (response) {
            const data = JSON.parse(response.body);
            updateGameState(data, gameArea, loggedInPlayer, playerColors, botColors);
            if (loggedInPlayer) {
                killCountElement.innerText = data.score.find(elem => elem.player.trim() === loggedInPlayer.name).score;
                killCount = data.score.find(elem => elem.player.trim() === loggedInPlayer.name).score;
            }
            const sortedScores = data.score.filter(jogador => jogador.score !== 0)
            .sort((a, b) => b.score - a.score);
            score_table.innerHTML = '';
            sortedScores.forEach(jogador => {
                if (jogador.score != 0) {
                    const jogadorElemento = document.createElement('div');
                    jogadorElemento.textContent = `Nome: ${jogador.player}, Pontos: ${jogador.score}`;
                    score_table.appendChild(jogadorElemento);
                }
            });
        });
    });

    connectButton.addEventListener('click', function () {
        const playerName = playerNameInput.value.trim();

        if (playerName) {
            stompClient.send("/app/connect", {}, JSON.stringify({ name: playerName }));
            loggedInPlayer = { name: playerName, position: { x: 0, y: 0 } };
            loginContainer.style.display = 'none';
            gameContainer.style.display = 'block';
            restartContainer.style.display = 'none';
        } else {
            alert('Please enter a player name.');
        }
    });

    restartButton.addEventListener('click', function () {
        // Redefinindo o estado para mostrar a tela de login novamente
        loginContainer.style.display = 'block'; // Exibe a tela de login
        gameContainer.style.display = 'none'; // Oculta o contêiner do jogo
        restartContainer.style.display = 'none'; // Oculta a tela de reinício
        loggedInPlayer = null; // Reseta o jogador logado
    });

    document.addEventListener('keydown', function (event) {
        if (!loggedInPlayer) return;

        switch (event.key) {
            case 'ArrowUp':
                loggedInPlayer.position.y -= playerSpeed;
                break;
            case 'ArrowDown':
                loggedInPlayer.position.y += playerSpeed;
                break;
            case 'ArrowLeft':
                loggedInPlayer.position.x -= playerSpeed;
                break;
            case 'ArrowRight':
                loggedInPlayer.position.x += playerSpeed;
                break;
        }

        updatePlayerPosition(loggedInPlayer);

        if (loggedInPlayer && stompClient) {
            var move = {
                name: loggedInPlayer.name,
                position: loggedInPlayer.position,
                score: killCount
            };

            stompClient.send("/app/move", {}, JSON.stringify(move));
        }
    });

    function showRestartScreen() {
        gameContainer.style.display = 'none'; // Oculta o contêiner do jogo
        restartContainer.style.display = 'flex'; // Exibe a tela de reinício
    }

    function onPlayerDeath(player) {
        if (player.name === loggedInPlayer.name) {
            // Se o jogador que morreu é o logado, exibe a tela de reinício
            showRestartScreen();
        } else {
            // Se não, apenas exibe uma mensagem ou atualiza o estado
            console.log(`Player ${player.name} morreu.`);
        }
    }

    function updatePlayerPosition(player) {
        const playerElement = document.querySelector(`[data-player-name="${player.name}"]`);
        if (playerElement) {
            playerElement.style.left = `${player.position.x}px`;
            playerElement.style.top = `${player.position.y}px`;
        }
    }

    function updateGameState(data, gameArea, loggedInPlayer, playerColors, botColors) {
        gameArea.innerHTML = '';

        data.players.forEach(player => {
            const playerElement = document.createElement('div');
            playerElement.textContent = `${player.name}`;
            playerElement.classList.add('player');
            playerElement.setAttribute('data-player-name', player.name);
            playerElement.style.left = `${player.position.x}px`;
            playerElement.style.top = `${player.position.y}px`;
            playerElement.style.backgroundColor = getOrCreateColor(player.name, playerColors);
            gameArea.appendChild(playerElement);

            if (player.name === loggedInPlayer.name) {
                loggedInPlayer.position = player.position;
                if (player.dead) {
                    onPlayerDeath(player); // Chama a função de morte se o jogador estiver morto
                }
            }
        });

        data.bots.forEach(bot => {
            const botElement = document.createElement('div');
            botElement.textContent = `${bot.name}`;
            botElement.classList.add('bot');
            botElement.setAttribute('data-bot-name', bot.name);
            botElement.style.left = `${bot.position.x}px`;
            botElement.style.top = `${bot.position.y}px`;
            botElement.style.backgroundColor = getOrCreateColor(bot.name, botColors);
            gameArea.appendChild(botElement);
        });
    }

    function getOrCreateColor(id, colorStorage) {
        if (!colorStorage[id]) {
            colorStorage[id] = generateRandomColor();
        }
        return colorStorage[id];
    }

    function generateRandomColor() {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    }
});
