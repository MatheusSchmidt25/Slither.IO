package br.unipar.frameworksweb.slitherunipar;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class GameState {
    private static GameState instance;

    Map<String, Object> state = new HashMap<>();
    private ConcurrentHashMap<String, Player> players;
    private ConcurrentHashMap<String, Score> score;

    private ConcurrentHashMap<String, Bot> bots;

    private GameState() {
        players = new ConcurrentHashMap<>();
        bots = new ConcurrentHashMap<>();
        score = new ConcurrentHashMap<>();
    }

    public static synchronized GameState getInstance() {
        if (instance == null) {
            instance = new GameState();
        }
        return instance;
    }

    public ConcurrentHashMap<String, Player> getPlayers() {
        return players;
    }

    public ConcurrentHashMap<String, Bot> getBots() {
        return bots;
    }
    public ConcurrentHashMap<String, Score>getScore(){
        return score;
    }
    public Map<String, Object> getState() {
        state.put("players", players.values());
        state.put("bots", bots.values());
        state.put("score",score.values());
        return state;
    }
}