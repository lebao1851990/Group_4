/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.com.csd201.dungeon.ds;

/**
 *
 * @author ASUS
 */
import com.csd201.dungeon.ds.MyLinkedList;
import com.csd201.dungeon.ds.MyLinkedList;
import com.csd201.dungeon.model.Item;

public class LinkedListManualTest {

    public static void main(String[] args) {

        System.out.println("=== LINKED LIST MANUAL TEST ===");

        MyLinkedList<Item> list = new MyLinkedList<>();

        // Test remove empty (no NullPointerException)
        System.out.println("\nTest remove on empty list:");
        Item removed = list.remove(x -> x.getId() == 1);
        System.out.println("Removed = " + removed); // null

        // Add items
        System.out.println("\nTest add items:");
        list.add(new Item(1, "Potion"));
        list.add(new Item(2, "Key"));
        list.add(new Item(3, "Sword"));

        list.display();

        // Search item
        System.out.println("\nTest search id=2:");
        Item found = list.search(x -> x.getId() == 2);
        System.out.println("Found = " + found);

        // Remove head
        System.out.println("\nTest remove head id=1:");
        removed = list.remove(x -> x.getId() == 1);
        System.out.println("Removed = " + removed);

        list.display();

        // Remove tail
        System.out.println("\nTest remove tail id=3:");
        removed = list.remove(x -> x.getId() == 3);
        System.out.println("Removed = " + removed);

        list.display();

        System.out.println("\n✅ LINKED LIST TEST DONE (NO NPE)");
    }
}
