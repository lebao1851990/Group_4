package com.csd201.dungeon.model;

/**
 * ============================================================
 *  ROOM — Phòng Trong Dungeon
 * ============================================================
 *
 * Mỗi Room đại diện cho một đỉnh (vertex) trong đồ thị Graph.
 * Room có thể chứa không hoặc một Monster cần tiêu diệt,
 * và không hoặc một Item để người chơi nhặt.
 *
 * Sơ đồ 6 phòng (6 đỉnh Graph):
 *   [0] Cổng Vào      — Không quái, không item (điểm xuất phát)
 *   [1] Hang Goblin   — Goblin (Monster) + Kiếm sắt (Item)
 *   [2] Hang Quỷ Nước — Dragonair (Monster)
 *   [3] Đảo Bình Yên  — Bình HP (Item), không quái → điểm hồi phục
 *   [4] Pháo Đài Orc  — Gengar (Monster) + Chìa khóa (Item)
 *   [5] Boss Room     — Rayquaza Boss (Monster) → điểm kết thúc
 *
 * Item bị xóa khỏi phòng sau khi nhặt (setItem(null)).
 */
public class Room {

    /** ID phòng — tương ứng với ID đỉnh trong Graph (0 đến 5). */
    private final int id;

    /** Tên phòng hiển thị trên console và gửi qua API đến React UI. */
    private final String name;

    /**
     * Quái vật đang chặn phòng.
     * null nếu phòng không có quái hoặc quái đã bị tiêu diệt.
     */
    private Monster monster;

    /**
     * Vật phẩm trong phòng có thể nhặt.
     * null nếu không có item hoặc đã nhặt rồi.
     */
    private Item item;

    /**
     * Tạo một phòng mới (chưa có Monster và Item).
     * Gọi từ GameController.initGame() cho mỗi trong 6 phòng.
     */
    public Room(int id, String name) {
        this.id   = id;
        this.name = name;
        // monster và item = null mặc định
    }

    // ── Getters ────────────────────────────────────────────────

    /** Trả về ID phòng (dùng để tra cứu và hiển thị trên bản đồ). */
    public int getId() { return id; }

    /** Trả về tên phòng (gửi qua API /map để React hiển thị). */
    public String getName() { return name; }

    /** Trả về Monster trong phòng (null nếu không có). */
    public Monster getMonster() { return monster; }

    /** Trả về Item trong phòng (null nếu không có hoặc đã nhặt). */
    public Item getItem() { return item; }

    // ── Setters ────────────────────────────────────────────────

    /**
     * Đặt Monster vào phòng.
     * Gọi từ GameController.initGame() để bố trí quái vào từng phòng.
     */
    public void setMonster(Monster monster) { this.monster = monster; }

    /**
     * Đặt hoặc xóa Item trong phòng.
     * Truyền null để xóa item sau khi người chơi nhặt.
     */
    public void setItem(Item item) { this.item = item; }

    /** Hiển thị phòng dạng "Room ID - Tên" (dùng cho debug/log). */
    @Override
    public String toString() {
        return "Room " + id + " - " + name;
    }
}