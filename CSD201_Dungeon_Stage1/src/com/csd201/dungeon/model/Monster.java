package com.csd201.dungeon.model;

/**
 * ============================================================
 *  MONSTER — Model Quái Vật Trong Dungeon
 * ============================================================
 *
 * Lưu trữ thông tin của một quái vật: ID, tên, HP hiện tại, và ATK.
 * Monster được lưu trong BST (MonsterBST) dùng ID làm khóa.
 *
 * HP có thể thay đổi khi bị tấn công (takeDamage).
 * Các trường còn lại là final → không thể thay đổi sau khi tạo.
 *
 * BẢNG MONSTER (đồng bộ với MONSTER_DB trong App.jsx):
 * ┌─────┬───────────────────┬─────┬─────┬──────────┐
 * │ ID  │ Tên               │  HP │ ATK │ Phòng    │
 * ├─────┼───────────────────┼─────┼─────┼──────────┤
 * │ 101 │ Caterpie Sâu      │ 220 │  28 │ Phòng 1  │
 * │ 102 │ Goblin Rừng       │ 260 │  32 │ Phòng 1  │
 * │ 103 │ Machamp Đá Tảng   │ 310 │  38 │ Phòng 2  │
 * │ 104 │ Dragonair Thủy    │ 350 │  42 │ Phòng 2  │
 * │ 105 │ Charizard Lửa     │ 330 │  40 │ Phòng 2  │
 * │ 106 │ Orc Đột Biến      │ 390 │  50 │ Phòng 4  │
 * │ 107 │ Gengar Bóng Tối   │ 420 │  56 │ Phòng 4  │
 * │ 108 │ Rayquaza Boss     │ 480 │  68 │ Phòng 5  │
 * │ 991 │ Lính Tiên Phong   │ 160 │  22 │ minion   │
 * │ 992 │ Trung Vệ Hầm Ngục │ 200 │  26 │ minion   │
 * └─────┴───────────────────┴─────┴─────┴──────────┘
 */
public class Monster {

    /** ID duy nhất của quái vật — dùng làm khóa tìm kiếm trong BST. */
    private final int id;

    /** Tên hiển thị của quái vật. */
    private final String name;

    /**
     * HP hiện tại (Health Points / Máu).
     * KHÔNG final vì sẽ giảm khi bị tấn công qua takeDamage().
     */
    private int hp;

    /** Sát thương mỗi đòn đánh của quái vật. */
    private final int attack;

    /**
     * Tạo một Monster với đầy đủ thông số.
     * HP khởi tạo = maxHp (quái vật ở trạng thái đầy máu).
     */
    public Monster(int id, String name, int hp, int attack) {
        this.id     = id;
        this.name   = name;
        this.hp     = hp;
        this.attack = attack;
    }

    // ── Getters ────────────────────────────────────────────────

    /** Trả về ID (dùng làm khóa tra cứu trong MonsterBST). */
    public int getId() { return id; }

    /** Trả về tên hiển thị của quái vật. */
    public String getName() { return name; }

    /** Trả về HP hiện tại (giảm dần khi bị đánh). */
    public int getHp() { return hp; }

    /** Trả về sát thương của quái vật mỗi lượt phản công. */
    public int getAttack() { return attack; }

    // ── Actions ───────────────────────────────────────────────

    /**
     * Trừ HP của quái vật đi đúng bằng lượng sát thương dmg.
     * HP không được xuống dưới 0 (tránh số âm).
     * Gọi từ: combatLoop() khi người chơi tấn công thành công.
     */
    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0; // Đảm bảo HP tối thiểu = 0
    }

    /**
     * Kiểm tra quái vật còn sống không (HP > 0).
     * Gọi từ: combatLoop() để quyết định tiếp tục hay kết thúc trận chiến.
     */
    public boolean isAlive() {
        return hp > 0;
    }

    // ── Object Methods ─────────────────────────────────────────

    /** In thông tin quái vật ra dạng chuỗi (dùng cho debug/log). */
    @Override
    public String toString() {
        return "Monster{" + id + "-" + name + ", hp=" + hp + ", atk=" + attack + "}";
    }

    /**
     * So sánh 2 Monster dựa theo ID (dùng trong MonsterBST).
     * Hai Monster được coi là GIỐNG NHAU nếu có cùng ID.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Monster monster = (Monster) obj;
        return id == monster.id;
    }
}