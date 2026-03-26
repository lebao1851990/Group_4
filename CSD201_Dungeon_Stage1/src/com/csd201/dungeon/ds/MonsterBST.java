package com.csd201.dungeon.ds;

import com.csd201.dungeon.model.Monster;

/**
 * ============================================================
 *  MONSTER BST — Cây Nhị Phân Tìm Kiếm (Binary Search Tree)
 *                Dùng làm Từ Điển Tra Cứu Quái Vật
 * ============================================================
 *
 * BST cho phép tra cứu Monster theo ID với độ phức tạp O(log n)
 * thay vì O(n) như mảng thông thường — hiệu quả hơn khi n lớn.
 *
 * QUY TẮC BST (sắp xếp theo Monster ID):
 *   - ID < root.id  → đặt vào NHÁNH TRÁI
 *   - ID > root.id  → đặt vào NHÁNH PHẢI
 *   - ID == root.id → trùng, không thêm (ID phải duy nhất)
 *
 * CẤY CÓ DẠNG (sau khi insert 104, 102, 106, 101, 103):
 *
 *          [104]
 *          /    \
 *       [102]  [106]
 *       /   \
 *    [101] [103]
 *
 * TÌM KIẾM theo ID:
 *   - Bắt đầu từ root, so sánh ID
 *   - Nhỏ hơn → sang trái | Lớn hơn → sang phải
 *   - Đến khi tìm thấy hoặc gặp null (không có)
 *
 * Được sử dụng tại GameController để:
 *   - Tra cứu thông tin quái vật khi vào phòng
 *   - Hiển thị thông số BST khi giới thiệu màn chơi
 */
public class MonsterBST {

    /** Gốc của cây (root node). null nếu BST chưa có phần tử nào. */
    private BSTNode root;

    // ============================================================
    //  INSERT — Thêm Monster Mới Vào BST
    // ============================================================

    /**
     * Thêm một Monster mới vào BST (gọi đệ quy từ root).
     * Bỏ qua nếu monster == null.
     */
    public void insert(Monster m) {
        if (m == null) return;
        root = insertRec(root, m); // Gọi hàm đệ quy bắt đầu từ root
    }

    /**
     * Đệ quy: tìm vị trí đúng theo quy tắc BST rồi chèn Node mới.
     *
     * @param node  Nút hiện tại đang xét
     * @param m     Monster cần chèn
     * @return      Nút gốc của cây con sau khi đã chèn
     */
    private BSTNode insertRec(BSTNode node, Monster m) {
        if (node == null) {
            // Tìm thấy vị trí trống → tạo nút mới và đặt vào đây
            return new BSTNode(m);
        }
        if (m.getId() < node.data.getId()) {
            // ID nhỏ hơn → chèn vào cây con TRÁI
            node.left = insertRec(node.left, m);
        } else if (m.getId() > node.data.getId()) {
            // ID lớn hơn → chèn vào cây con PHẢI
            node.right = insertRec(node.right, m);
        }
        // ID bằng nhau → không thêm (BST không cho phép trùng ID)
        return node;
    }

    // ============================================================
    //  SEARCH — Tìm Kiếm Monster Theo ID  [O(log n)]
    // ============================================================

    /**
     * Tìm kiếm Monster theo ID từ gốc cây.
     * Trả về Monster nếu tìm thấy, null nếu không có trong BST.
     */
    public Monster search(int id) {
        return searchRec(root, id);
    }

    /**
     * Đệ quy tìm kiếm: mỗi bước loại bỏ một nửa cây
     * nhờ so sánh ID → đạt O(log n).
     */
    private Monster searchRec(BSTNode node, int id) {
        if (node == null || node.data == null) {
            return null; // Không tìm thấy (đã đến lá cây)
        }
        if (id == node.data.getId()) {
            return node.data; // Tìm thấy!
        }
        if (id < node.data.getId()) {
            return searchRec(node.left, id);  // Tìm ở cây con trái
        }
        return searchRec(node.right, id); // Tìm ở cây con phải
    }

    // ============================================================
    //  DELETE — Xóa Monster Theo ID (3 trường hợp)
    // ============================================================

    /**
     * Xóa Monster có ID tương ứng khỏi BST.
     * Sau khi xóa cây vẫn đảm bảo tính chất BST.
     */
    public void delete(int id) {
        root = deleteRec(root, id);
    }

    /**
     * Đệ quy xử lý 3 trường hợp xóa nút trong BST:
     *
     *   TH1 — Nút xóa là LÁ (0 con):
     *     → Đặt thành null, xóa trực tiếp.
     *
     *   TH2 — Nút xóa có 1 CON:
     *     → Trả về con duy nhất để thay thế vị trí nút bị xóa.
     *
     *   TH3 — Nút xóa có 2 CON:
     *     → Tìm nút nhỏ nhất trong cây con PHẢI (kế thừa nhỏ nhất)
     *     → Dùng nó thay thế dữ liệu của nút bị xóa
     *     → Xóa nút kế thừa đó khỏi cây con phải.
     */
    private BSTNode deleteRec(BSTNode root, int id) {
        if (root == null || root.data == null) return root; // Không tìm thấy

        if (id < root.data.getId()) {
            root.left = deleteRec(root.left, id);   // Xóa ở cây con trái
        } else if (id > root.data.getId()) {
            root.right = deleteRec(root.right, id); // Xóa ở cây con phải
        } else {
            // Đây chính là nút cần xóa!

            // TH1 & TH2: 0 hoặc 1 con
            if (root.left == null)  return root.right; // Chỉ có nhánh phải (hoặc không có con)
            if (root.right == null) return root.left;  // Chỉ có nhánh trái

            // TH3: Có 2 con → dùng phần tử kế thừa (in-order successor)
            root.data = minValue(root.right); // Lấy Monster nhỏ nhất ở nhánh phải

            // Xóa nút kế thừa đó khỏi cây con phải
            root.right = deleteRec(root.right, root.data.getId());
        }
        return root;
    }

    /**
     * Tìm Monster có ID nhỏ nhất trong một cây con.
     * Trong BST luôn là nút TẬN CÙNG NHÁNH TRÁI.
     * Dùng để tìm "in-order successor" khi xóa nút có 2 con.
     */
    private Monster minValue(BSTNode root) {
        Monster minv = root.data;
        while (root.left != null) {
            minv = root.left.data; // Đi mãi sang trái
            root = root.left;
        }
        return minv;
    }
}
