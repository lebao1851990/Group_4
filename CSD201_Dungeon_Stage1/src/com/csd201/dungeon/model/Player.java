package com.csd201.dungeon.model;

public class Player {
    private int hp;

    public Player(int hp) {
        this.hp = hp;
    }

    public int getHp() { return hp; }

    public void heal(int amount) {
        if (amount < 0) return;
        hp += amount;
    }

    @Override
    public String toString() {
        return "Player{hp=" + hp + "}";
    }
}
