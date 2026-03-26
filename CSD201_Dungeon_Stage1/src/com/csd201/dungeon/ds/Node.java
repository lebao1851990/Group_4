package com.csd201.dungeon.ds;

/**
 * ============================================================
 *  NODE — Nút đơn của cấu trúc Danh Sách Liên Kết (Linked List)
 * ============================================================
 *
 * Mỗi Node giữ một GIÁ TRỊ (data) và một CON TRỎ (next) trỏ đến
 * Node kế tiếp trong chuỗi. Khi next == null → đây là Node cuối cùng.
 *
 *  [ data | next ] → [ data | next ] → [ data | null ]
 *    Node 1            Node 2             Node 3 (cuối)
 *
 * Sử dụng generic <T> để Node có thể chứa bất kỳ kiểu dữ liệu nào:
 *   - Node<Integer>  → lưu ID phòng trong Graph (danh sách kề)
 *   - Node<Item>     → lưu đồ vật trong Túi Đồ (InventoryService)
 */
public class Node<T> {

    /** Giá trị được lưu tại nút này (ví dụ: ID phòng, hoặc Item). */
    public T data;

    /** Con trỏ đến Node tiếp theo; null nếu đây là Node cuối danh sách. */
    public Node<T> next;

    /**
     * Khởi tạo một Node mới với giá trị cho trước.
     * Con trỏ next mặc định là null (chưa liên kết).
     */
    public Node(T data) {
        this.data = data;
        this.next = null; // Chưa có node tiếp theo
    }
}
