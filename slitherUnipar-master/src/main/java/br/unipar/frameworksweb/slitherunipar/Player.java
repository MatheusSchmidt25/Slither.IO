// src/main/java/br/unipar/frameworksweb/slitherunipar/Player.java
package br.unipar.frameworksweb.slitherunipar;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Player {
    private String name;
    private Position position;
    private double angle;
    private int points;
    private boolean isDead; // Novo atributo para indicar se o jogador está morto

    public Player(String name, Position position, double angle) {
        this.name = name;
        this.position = position;
        this.angle = angle;
        this.points = 0;
        this.isDead = false; // Inicialmente, todos os jogadores começam vivos
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void die() {
        this.isDead = true; // Define o jogador como morto
    }

    public abstract Position move();
}
