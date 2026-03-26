package com.csd201.dungeon.ds;

/**
 * ============================================================
 *  MY STACK — Ngăn Xếp (Stack / LIFO: Last In, First Out)
 * ============================================================
 *
 * Phần tử vào SAU thì ra TRƯỚC — giống như chồng đĩa: đĩa
 * đặt trên cùng sẽ được lấy ra đầu tiên.
 *
 * Sơ đồ:
 *   push(A) → [A]         (đáy)
 *   push(B) → [A, B]      (B trên đỉnh)
 *   pop()   → trả về B, còn [A]
 *
 * TRONG DỰ ÁN NÀY:
 *   Dùng trong Graph.printPath() và Graph.printDijkstraPath() để
 *   TRUY VẾT ngược đường đi từ đích → điểm xuất phát.
 *
 *   Vì Dijkstra / BFS lưu mảng parent[] theo chiều NGƯỢC
 *   (parent[v] = u nghĩa là "từ u đi đến v"), ta phải:
 *     1. Duyệt từ đích ngược về start → push() từng đỉnh vào Stack
 *     2. Pop() từng đỉnh ra → số đầu ra sẽ là thứ tự ĐÚNG (start → đích)
 *
 *   Ví dụ path ngược: 5 → 4 → 1 → 0
 *   Stack sau push: [0, 1, 4, 5] (5 trên đỉnh)
 *   Pop lần lượt:  0 → 1 → 4 → 5   ✅ đúng thứ tự
 *
 * Cài bằng Linked List (top = đầu linked list) để push/pop O(1).
 */
public class MyStack<T> {

    /** Đỉnh ngăn xếp — con trỏ đến phần tử nằm trên cùng. */
    private Node<T> top;

    /** Khởi tạo ngăn xếp rỗng. */
    public MyStack() {
        top = null;
    }

    /** Kiểm tra ngăn xếp có rỗng không. */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Đẩy phần tử vào ĐỈnh ngăn xếp (O(1)).
     * Trong truy vết đường đi: push(đỉnh) theo chiều ngược.
     */
    public void push(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = top; // Nút mới trỏ vào đỉnh cũ
        top = newNode;       // Cập nhật đỉnh mới
    }

    /**
     * Lấy phần tử ra khỏi ĐỈNH ngăn xếp (O(1)).
     * Trong in đường đi: pop() liên tiếp cho đến khi rỗng.
     * Trả về null nếu ngăn xếp rỗng.
     */
    public T pop() {
        if (isEmpty()) return null;
        T data = top.data;
        top = top.next; // Bỏ đỉnh, dịch chuyển xuống phần tử bên dưới
        return data;
    }

    /**
     * Xem phần tử đỉnh mà KHÔNG xóa.
     * Trả về null nếu rỗng.
     */
    public T peek() {
        if (isEmpty()) return null;
        return top.data;
    }
}
