/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.csd201.dungeon.model;

/**
 *
 * @author ASUS
 */
public class Room {
    private final int id;
    private final String name;

    private Monster monster;
    private Item item;

    public Room(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public Monster getMonster() { return monster; }
    public void setMonster(Monster monster) { this.monster = monster; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    @Override
    public String toString() {
        return "Room " + id + " - " + name;
    }
}