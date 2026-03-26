package com.csd201.dungeon.model;

/**
 * ============================================================
 *  ITEM — Vật Phẩm (Loot) Trong Dungeon
 * ============================================================
 *
 * Mỗi Item có ID và tên. Người chơi có thể nhặt Item khi vào phòng,
 * Item sẽ được thêm vào Túi Đồ (InventoryService dùng Linked List).
 *
 * Các Item trong game:
 *   ID 1 → "Kiếm sắt (+10 Dmg)"     — Phòng 1 (tăng sức công)
 *   ID 2 → "Bình HP (+50 Máu)"       — Phòng 3 (hồi HP ngay lập tức)
 *   ID 3 → "Chìa khóa Cổng Rồng"     — Phòng 4 (mở cổng Boss)
 *
 * Tương đương với các Phụ Trợ trong giao diện React:
 *   buffAtk (P2) / buffHp (P3) / millenniumKey (P4)
 */
public class Item {

    /** ID duy nhất của item. */
    private final int id;

    /** Tên hiển thị (mô tả tác dụng). */
    private final String name;

    /**
     * Tạo một Item mới.
     * Gọi từ GameController.initGame() để rải item vào các phòng.
     */
    public Item(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    /** Trả về ID của item. */
    public int getId() { return id; }

    /** Trả về tên/mô tả của item. */
    public String getName() { return name; }

    /** Hiển thị item dạng "ID - Tên" (dùng khi in túi đồ). */
    @Override
    public String toString() {
        return id + " - " + name;
    }

    /**
     * So sánh 2 Item theo ID.
     * Dùng khi remove() trong MyLinkedList để tìm đúng item cần xóa.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item item = (Item) obj;
        return id == item.id;
    }
}
