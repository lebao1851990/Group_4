package com.csd201.dungeon.service;

import com.csd201.dungeon.ds.MyLinkedList;
import com.csd201.dungeon.model.Item;

public class InventoryService {
    private MyLinkedList<Item> inventory = new MyLinkedList<>();

    public void addItem(Item item) {
        inventory.add(item);
    }

    public void showInventory() {
        System.out.println("Inventory:");
        inventory.display();
    }
}
