/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.csd201.dungeon.ds;

import com.csd201.dungeon.model.Monster;

public class MonsterBST {
    private BSTNode root;

    public void insert(Monster m) {
        root = insertRec(root, m);
    }

    private BSTNode insertRec(BSTNode node, Monster m) {
        if (node == null) return new BSTNode(m);
        if (m.getId() < node.data.getId())
            node.left = insertRec(node.left, m);
        else if (m.getId() > node.data.getId())
            node.right = insertRec(node.right, m);
        return node;
    }

    public Monster search(int id) {
        BSTNode cur = root;
        while (cur != null) {
            if (id == cur.data.getId()) return cur.data;
            if (id < cur.data.getId()) cur = cur.left;
            else cur = cur.right;
        }
        return null;
    }
}
