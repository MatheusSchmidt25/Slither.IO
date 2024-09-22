// src/main/java/br/unipar/frameworksweb/slitherunipar/GameController.java
package br.unipar.frameworksweb.slitherunipar;

import com.fasterxml.jackson.databind.deser.std.ObjectArrayDeserializer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@EnableWebSocketMessageBroker
public class GameController {

    private final GameState gameState = GameState.getInstance();
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RequestMapping(value = "/game", method = RequestMethod.GET)
    public String game() {
        return "game";
    }

    @MessageMapping("/score")
    @SendTo("/topic/game")
    public  Map<String, Object> score(ScoreMessage message){
        String player = message.getPlayer();
        int score = message.getScore();
        Score newScore = new Score();
        newScore.setScore(score);
        newScore.setPlayer(player);

        gameState.getScore().put(player, newScore);

        messagingTemplate.convertAndSend("/topic/game", gameState.getState());

        return gameState.getState();

    }

    @MessageMapping("/connect")
    @SendTo("/topic/game")
    public Map<String, Object> connect(ConnectMessage message) {
        String username = message.getName();
        if (!gameState.getPlayers().containsKey(username)) {
            // Gerar posição aleatória para o jogador
            double startX = Math.random() * 400; // Posição X entre 0 e 400
            double startY = Math.random() * 400; // Posição Y entre 0 e 400

            // Converter posição para int
            int startXInt = (int) startX;
            int startYInt = (int) startY;

            Player player = new Player(username, new Position(startXInt, startYInt), 0.0) {
                @Override
                public Position move() {
                    return null;
                }
            };
            gameState.getPlayers().put(username, player);
            System.out.println("Player " + username + " connected");
        }

        return gameState.getState();
    }



    @PostConstruct
    public void initializeBots() {
        int spacing = 100; // Distância mínima entre os bots
        int rows = 5; // Número de linhas de bots
        int cols = 10; // Número de colunas de bots

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Ajuste o valor base conforme necessário para aumentar o container
                double startX = 1000 + j * spacing; // Posição X com mais espaço
                double startY = 1000 + i * spacing; // Posição Y com mais espaço

                // Converter posição para int
                int startXInt = (int) startX;
                int startYInt = (int) startY;

                Bot bot = new Bot("Bot" + (i * cols + j + 1), new Position(startXInt, startYInt), Math.random() * 360);
                gameState.getBots().put(bot.getName(), bot);
            }
        }
        // Start a thread to periodically update game state
        new Thread(this::updateGameState).start();
        new Thread(this::respawnBots).start();
    }

    private void respawnBots() {
        while (true) {
            try {
                Thread.sleep(20000);
                int spacing = 1000; // Distância mínima entre os bots
                int rows = 2; // Número de linhas de bots
                int cols = 5; // Número de colunas de bots

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // Ajuste o valor base conforme necessário para aumentar o container
                        double startX = 100 + j * spacing; // Posição X com mais espaço
                        double startY = 100 + i * spacing; // Posição Y com mais espaço

                        // Converter posição para int
                        int startXInt = (int) startX;
                        int startYInt = (int) startY;

                        Bot bot = new Bot("Bot" + (gameState.getBots().size() + 1), new Position(startXInt, startYInt), Math.random() * 360);
                        gameState.getBots().put(bot.getName(), bot);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }



    public void updateGameState() {

        while (true) {
            try {
                for (Bot bot : gameState.getBots().values()) {
                    Position newPosition = bot.move();
                    bot.setPosition(newPosition);
                    gameState.getBots().put(bot.getName(), bot);
                }

                messagingTemplate.convertAndSend("/topic/game", gameState.getState());

                Thread.sleep(500); // Update every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    // src/main/java/br/unipar/frameworksweb/slitherunipar/GameController.java
    private double calculateDistance(Position pos1, Position pos2) {
        int dx = pos1.getX() - pos2.getX();
        int dy = pos1.getY() - pos2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void checkCollisions(MoveMessage message, Player playerinfo) {
        final double COLLISION_MARGIN = 10.0;
        List<Player> entitiesToRemove = new ArrayList<>();


        Score newScore = new Score();
        int novoValor = message.getScore();
        newScore.setPlayer(playerinfo.getName());
        newScore.setScore(novoValor +1);
        for (Player player : new ArrayList<>(gameState.getPlayers().values())) {
            for (Bot bot : new ArrayList<>(gameState.getBots().values())) {
                if (calculateDistance(player.getPosition(), bot.getPosition()) <= COLLISION_MARGIN) {
                    player.addPoints(bot.getPoints());
                    entitiesToRemove.add(bot);
                    gameState.getScore().put(playerinfo.getName(), newScore);
                    System.out.println("Player " + player.getName() + " collided with bot " + bot.getName()+ "score" + newScore.getScore());
                }
            }
        }

        for (Player player1 : new ArrayList<>(gameState.getPlayers().values())) {
            for (Player player2 : new ArrayList<>(gameState.getPlayers().values())) {
                if (!player1.equals(player2) &&
                        calculateDistance(player1.getPosition(), player2.getPosition()) <= COLLISION_MARGIN) {

                    // Obtem as pontuações dos jogadores
                    int scorePlayer1 = gameState.getScore().get(player1.getName()).getScore();
                    int scorePlayer2 = gameState.getScore().get(player2.getName()).getScore();

                    if (scorePlayer1 > scorePlayer2) {
                        player1.addPoints(player2.getPoints());
                        entitiesToRemove.add(player2);
                        // Zera o score do player2 no gameState
                        gameState.getScore().get(player2.getName()).setScore(0);

                        System.out.println("Player " + player1.getName() + " collided with player " + player2.getName() + " and removed " + player2.getName() + " (score zerado)");
                    } else if (scorePlayer1 < scorePlayer2) {
                        player2.addPoints(player1.getPoints());
                        entitiesToRemove.add(player1);
                        // Zera o score do player1 no gameState
                        gameState.getScore().get(player1.getName()).setScore(0);
                        System.out.println("Player " + player2.getName() + " collided with player " + player1.getName() + " and removed " + player1.getName() + " (score zerado)");
                    }
                }
            }
        }

// Remoção das entidades
        for (Player entity : entitiesToRemove) {
            if (entity instanceof Bot) {
                gameState.getBots().remove(entity.getName());
                System.out.println("Bot " + entity.getName() + " removed");
            } else if (entity instanceof Player) {
                gameState.getPlayers().remove(entity.getName());
                System.out.println("Player " + entity.getName() + " removed");
            }
        }





    }

    @MessageMapping("/move")
    @SendTo("/topic/game")
    public Map<String, Object> movePlayer(MoveMessage message) {
        String username = message.getName();
        Position newPosition = message.getPosition();

        Player player = gameState.getPlayers().get(username);

        if (player != null) {
            player.setPosition(newPosition);
            gameState.getPlayers().put(username, player);
        }
        // Check for collisions
        checkCollisions(message, player);
        messagingTemplate.convertAndSend("/topic/game", gameState.getState());

        return gameState.getState();
    }
}