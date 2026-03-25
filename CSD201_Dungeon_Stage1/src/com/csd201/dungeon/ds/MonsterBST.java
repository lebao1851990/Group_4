/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.csd201.dungeon.ds;

import com.csd201.dungeon.model.Monster;

public class MonsterBST {
    private BSTNode root;

    public void insert(Monster m) {
        if (m == null)
            return;
        root = insertRec(root, m);
    }

    private BSTNode insertRec(BSTNode node, Monster m) {
        if (node == null)
            return new BSTNode(m);
        if (m.getId() < node.data.getId())
            node.left = insertRec(node.left, m);
        else if (m.getId() > node.data.getId())
            node.right = insertRec(node.right, m);
        return node;
    }

    public Monster search(int id) {
        return searchRec(root, id);
    }

    private Monster searchRec(BSTNode node, int id) {
        if (node == null || node.data == null)
            return null; // Điều kiện dừng đệ quy và bọc lỗi Null
        if (id == node.data.getId())
            return node.data;
        if (id < node.data.getId())
            return searchRec(node.left, id); // Gọi đệ quy nhánh trái
        return searchRec(node.right, id); // Gọi đệ quy nhánh phải
    }

    // --- Phương thức Delete Node (để ôn thi Practical Exam) ---
    public void delete(int id) {
        root = deleteRec(root, id);
    }

    private BSTNode deleteRec(BSTNode root, int id) {
        if (root == null || root.data == null)
            return root;

        // B1: Gọi đệ quy tìm Node cần xóa
        if (id < root.data.getId()) {
            root.left = deleteRec(root.left, id);
        } else if (id > root.data.getId()) {
            root.right = deleteRec(root.right, id);
        } else {
            // B2: Đã tìm thấy Node cần xóa (root hiện tại)

            // TH1 & TH2: Có 0 hoặc 1 node con
            if (root.left == null)
                return root.right;
            else if (root.right == null)
                return root.left;

            // TH3: Có 2 node con -> Tìm Node nhỏ nhất bên nhánh phải để thế mạng
            root.data = minValue(root.right);

            // Xóa node thế mạng ở nhánh phải
            root.right = deleteRec(root.right, root.data.getId());
        }
        return root;
    }

    // Hàm phụ tìm giá trị nhỏ nhất của cây (đi mãi sang trái)
    private Monster minValue(BSTNode root) {
        Monster minv = root.data;
        while (root.left != null) {
            minv = root.left.data;
            root = root.left;
        }
        return minv;
    }
}
