package com.csd201.dungeon;

import com.csd201.dungeon.model.Item;
import com.csd201.dungeon.service.InventoryService;

public class Main {
    public static void main(String[] args) {
        InventoryService inv = new InventoryService();

        inv.addItem(new Item(1, "Potion"));
        inv.addItem(new Item(2, "Key"));

        inv.showInventory();
    }
}
