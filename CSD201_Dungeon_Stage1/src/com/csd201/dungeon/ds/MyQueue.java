package com.csd201.dungeon.ds;

/**
 * ============================================================
 * MY QUEUE — Hàng Đợi (Queue / FIFO: First In, First Out)
 * ============================================================
 *
 * Phần tử vào TRƯỚC thì ra TRƯỚC — giống như hàng xếp vé.
 *
 * Sơ đồ:
 * enqueue(A) → [A]
 * enqueue(B) → [A, B]
 * dequeue() → trả về A, còn lại [B]
 *
 * TRONG DỰ ÁN NÀY:
 * Dùng trong Graph.bfsShortestPath() — thuật toán BFS (Breadth-First Search)
 * cần Queue để lan rộng thăm các đỉnh theo từng "lớp" (vòng tròn),
 * đảm bảo đường đi tìm được là NGẮN NHẤT (trong đồ thị không trọng số).
 *
 * Bước BFS:
 * 1. Đưa đỉnh bắt đầu vào Queue → enqueue(start)
 * 2. Lặp: lấy đỉnh đầu ra → dequeue() → thêm tất cả đỉnh kề chưa thăm
 * 3. Tiếp tục đến khi Queue rỗng hoặc tìm thấy đích
 *
 * Cài bằng Linked List (head = front, tail = back) để enqueue/dequeue O(1).
 */
public class MyQueue<T> {

    /** Đầu hàng đợi — nơi lấy phần tử ra (dequeue). */
    private Node<T> head;

    /** Cuối hàng đợi — nơi thêm phần tử vào (enqueue). */
    private Node<T> tail;

    /** Khởi tạo hàng đợi rỗng. */
    public MyQueue() {
        head = null;
        tail = null;
    }

    /** Kiểm tra hàng đợi có rỗng không. */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Thêm phần tử vào CUỐI hàng đợi (O(1) — chỉ cập nhật tail).
     * Trong BFS: gọi enqueue(v) khi tìm thấy đỉnh kề v chưa thăm.
     */
    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            // Hàng đợi rỗng → nút mới vừa là head vừa là tail
            head = tail = newNode;
        } else {
            tail.next = newNode; // Nối vào cuối
            tail = newNode; // Cập nhật tail
        }
    }

    /**
     * Lấy phần tử ra khỏi ĐẦU hàng đợi (O(1) — chỉ cập nhật head).
     * Trong BFS: gọi dequeue() để lấy đỉnh u hiện tại cần xử lý.
     * Trả về null nếu hàng đợi rỗng.
     */
    public T dequeue() {
        if (isEmpty())
            return null;
        T data = head.data;
        head = head.next; // Bỏ nút đầu, dịch chuyển head
        if (head == null)
            tail = null; // Hàng đợi trở nên rỗng
        return data;
    }

    /**
     * Xem phần tử đầu hàng đợi mà KHÔNG xóa.
     * Trả về null nếu rỗng.
     */
    public T peek() {
        if (isEmpty())
            return null;
        return head.data;
    }
}
