package com.csd201.dungeon.ds;

import com.csd201.dungeon.model.Monster; // BSTNode lưu Monster làm dữ liệu khóa

/**
 * ============================================================
 *  BSTNODE — Nút của Cây Nhị Phân Tìm Kiếm (Binary Search Tree)
 * ============================================================
 *
 * Mỗi BSTNode lưu một Monster và hai con trỏ (left, right) tạo
 * thành cấu trúc PHÂN NHÁNH theo ID của Monster:
 *
 *           [Monster 104]
 *           /            \
 *    [Monster 102]   [Monster 106]
 *       /
 *  [Monster 101]
 *
 * QUY TẮC sắp xếp BST:
 *   - ID < node cha  → đặt vào NHÁNH TRÁI  (left)
 *   - ID > node cha  → đặt vào NHÁNH PHẢI (right)
 *   → Tìm kiếm Monster theo ID chỉ mất O(log n) thay vì O(n)
 *
 * Dùng bởi: MonsterBST.java
 */
public class BSTNode {

    /** Dữ liệu Monster được lưu tại nút này (HP, ATK, tên, ...). */
    public Monster data;

    /** Con trỏ đến nút con TRÁI — chứa Monster có ID nhỏ hơn. */
    public BSTNode left;

    /** Con trỏ đến nút con PHẢI — chứa Monster có ID lớn hơn. */
    public BSTNode right;

    /**
     * Tạo một nút BST mới chứa Monster chỉ định.
     * Hai nhánh left và right ban đầu đều là null (lá cây).
     */
    public BSTNode(Monster data) {
        this.data  = data;
        this.left  = null; // Chưa có nhánh trái
        this.right = null; // Chưa có nhánh phải
    }
}
