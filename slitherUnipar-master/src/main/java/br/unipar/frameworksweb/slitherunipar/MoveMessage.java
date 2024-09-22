package br.unipar.frameworksweb.slitherunipar;

public class MoveMessage {
    private String name;
    private Position position;

    private int score;

    public MoveMessage() {
    }

    public MoveMessage(String name, Position position) {
        this.name = name;
        this.position = position;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "MoveMessage{" +
                "name='" + name + '\'' +
                ", position=" + position.toString() + ", score="+score+
                '}';
    }
}