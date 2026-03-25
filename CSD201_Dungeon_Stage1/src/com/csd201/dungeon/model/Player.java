package com.csd201.dungeon.model;

public class Player {
    private int hp;
    private int attack;

    public Player(int hp, int attack) {
        this.hp = hp;
        this.attack = attack;
    }

    public int gettingHp() { return hp; } // temp renaming avoiding conflict below, wait, I will just replace the whole body
    public int getHp() { return hp; }
    public int getAttack() { return attack; }

    public void heal(int amount) {
        if (amount < 0) return;
        hp += amount;
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public String toString() {
        return "Player{hp=" + hp + ", attack=" + attack + "}";
    }
}
