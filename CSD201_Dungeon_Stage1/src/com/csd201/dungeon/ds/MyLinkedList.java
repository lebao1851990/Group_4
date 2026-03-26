package com.csd201.dungeon.ds;

/**
 * ============================================================
 *  MY LINKED LIST — Danh Sách Liên Kết Đơn (Singly Linked List)
 * ============================================================
 *
 * Cấu trúc dữ liệu tuyến tính, các phần tử được liên kết với nhau
 * qua con trỏ next. Khác ArrayList, không cần mảng cố định kích thước.
 *
 * Sơ đồ chuỗi: head → [A|→] → [B|→] → [C|null]
 *
 * TRONG DỰ ÁN NÀY dùng cho 2 mục đích:
 *   1. Graph.adj[]  → mỗi đỉnh (phòng) giữ một LinkedList<Integer>
 *      chứa ID các đỉnh kề (phòng có thể đi tiếp).
 *   2. InventoryService → lưu danh sách Item người chơi nhặt được.
 *
 * Generic <T>: cùng một class dùng được cho cả Integer lẫn Item.
 */
public class MyLinkedList<T> {

    /** Đầu danh sách — con trỏ vào nút đầu tiên. null nếu danh sách rỗng. */
    private Node<T> head;

    /** Khởi tạo danh sách rỗng (head = null). */
    public MyLinkedList() {
        head = null;
    }

    /**
     * Trả về nút đầu tiên — dùng để duyệt toàn bộ danh sách từ ngoài.
     * Ví dụ: Graph.getNeighbors(u) trả về LinkedList, rồi duyệt
     *         từng cur = cur.next để kiểm tra các phòng kề.
     */
    public Node<T> getHead() {
        return head;
    }

    /**
     * Thêm phần tử MỚI vào CUỐI danh sách (O(n) — phải duyệt hết).
     * Gọi khi: Graph.addEdge() thêm đỉnh kề, hoặc nhặt item mới.
     */
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            // Danh sách rỗng → nút mới chính là đầu danh sách
            head = newNode;
        } else {
            // Duyệt đến nút cuối cùng rồi nối thêm
            Node<T> cur = head;
            while (cur.next != null) {
                cur = cur.next;
            }
            cur.next = newNode;
        }
    }

    /**
     * Tìm kiếm phần tử theo giá trị. Trả về phần tử nếu tìm thấy,
     * null nếu không có. Dùng equals() để so sánh.
     * Độ phức tạp: O(n) — phải duyệt tuần tự.
     */
    public T search(T target) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data.equals(target)) {
                return cur.data; // Tìm thấy → trả về ngay
            }
            cur = cur.next;
        }
        return null; // Không tìm thấy → trả về null
    }

    /**
     * Xóa phần tử đầu tiên khớp với target. Trả về phần tử đã xóa,
     * hoặc null nếu không tìm thấy.
     * Cập nhật con trỏ để bỏ qua nút bị xóa.
     */
    public T remove(T target) {
        if (head == null) return null; // Danh sách rỗng

        // Trường hợp đặc biệt: xóa chính node đầu tiên
        if (head.data.equals(target)) {
            T data = head.data;
            head = head.next; // Dịch chuyển head sang node tiếp theo
            return data;
        }

        // Duyệt tìm node vừa đứng trước target để ghép nối lại
        Node<T> prev = head;
        Node<T> cur  = head.next;
        while (cur != null) {
            if (cur.data.equals(target)) {
                prev.next = cur.next; // Bỏ qua cur (xóa khỏi chuỗi)
                return cur.data;
            }
            prev = cur;
            cur  = cur.next;
        }
        return null; // Không tìm thấy target
    }

    /**
     * In ra tất cả phần tử trong danh sách (dùng System.out).
     * Gọi từ InventoryService.showInventory() để hiển thị túi đồ.
     */
    public void display() {
        Node<T> cur = head;
        while (cur != null) {
            System.out.println(cur.data); // In giá trị của từng nút
            cur = cur.next;
        }
    }
}
