/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.com.csd201.dungeon.ds;

/**
 *
 * @author ASUS
 */
import com.csd201.dungeon.ds.MonsterBST;
import com.csd201.dungeon.ds.MonsterBST;
import com.csd201.dungeon.model.Monster;

public class BSTManualTest {

    public static void main(String[] args) {

        System.out.println("=== BST MANUAL TEST ===");

        MonsterBST bst = new MonsterBST();

        bst.insert(new Monster(5, "Goblin", 40, 8));
        bst.insert(new Monster(15, "Orc", 60, 12));
        bst.insert(new Monster(2, "Slime", 20, 3));

        System.out.println("\nSearch monster id=5:");
        Monster m = bst.search(5);
        System.out.println(m);

        System.out.println("\nSearch monster id=99:");
        Monster m2 = bst.search(99);
        if (m2 == null) {
            System.out.println("Monster not found! (PASS)");
        } else {
            System.out.println(m2);
        }

        System.out.println("\n✅ BST TEST DONE");
    }
}