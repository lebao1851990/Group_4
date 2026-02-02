/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.csd201.dungeon.model;

/**
 *
 * @author ASUS
 */
public class Monster {
    private final int id;
    private final String name;
    private int hp;
    private final int attack;

    public Monster(int id, String name, int hp, int attack) {
        this.id = id;
        this.name = name;
        this.hp = hp;
        this.attack = attack;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getAttack() { return attack; }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public String toString() {
        return "Monster{" + id + "-" + name + ", hp=" + hp + ", atk=" + attack + "}";
    }
}