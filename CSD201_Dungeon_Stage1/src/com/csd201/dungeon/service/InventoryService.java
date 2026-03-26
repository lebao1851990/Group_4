package com.csd201.dungeon.service;

import com.csd201.dungeon.ds.MyLinkedList;
import com.csd201.dungeon.model.Item;

/**
 * ============================================================
 *  INVENTORY SERVICE — Dịch Vụ Quản Lý Túi Đồ Người Chơi
 * ============================================================
 *
 * Dùng MyLinkedList<Item> để lưu trữ danh sách vật phẩm
 * người chơi đã nhặt được trong Dungeon.
 *
 * Minh họa ứng dụng THỰC TẾ của cấu trúc Linked List:
 *   - Không cần biết trước số lượng item → kích thước động
 *   - Thêm item mới (add) ở cuối danh sách → O(n)
 *   - Hiển thị toàn bộ túi đồ (display) → duyệt O(n)
 *
 * Các Item có thể nhặt:
 *   "Kiếm sắt (+10 Dmg)"   — tăng sức tấn công (Phòng 1)
 *   "Bình HP (+50 Máu)"    — hồi máu ngay lập tức (Phòng 3)
 *   "Chìa khóa Cổng Rồng" — điều kiện mở cổng Boss (Phòng 4)
 *
 * Tương đương với state `inventory` trong App.jsx:
 *   { buffAtk: false, buffHp: false, millenniumKey: false }
 */
public class InventoryService {

    /**
     * Túi đồ người chơi — Linked List chứa các Item đã nhặt.
     * Dùng MyLinkedList tự cài để minh họa cấu trúc dữ liệu.
     */
    private MyLinkedList<Item> inventory = new MyLinkedList<>();

    /**
     * Thêm item mới vào cuối túi đồ.
     * Gọi từ GameController.checkRoomEvents() khi phát hiện item trong phòng.
     *
     * @param item  Vật phẩm vừa nhặt được
     */
    public void addItem(Item item) {
        inventory.add(item); // Thêm vào cuối Linked List
    }

    /**
     * In toàn bộ nội dung túi đồ ra console.
     * Gọi khi người chơi chọn "2. Mở xem túi đồ" trong menu.
     * Duyệt toàn bộ Linked List từ đầu đến cuối.
     */
    public void showInventory() {
        System.out.println("=== 🎒 TÚI ĐỒ CỦA BẠN ===");
        inventory.display(); // Gọi display() của MyLinkedList để in từng item
        System.out.println("==========================");
    }
}
