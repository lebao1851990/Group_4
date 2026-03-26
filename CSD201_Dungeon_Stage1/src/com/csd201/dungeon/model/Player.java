package com.csd201.dungeon.model;

/**
 * ============================================================
 *  PLAYER — Model Người Chơi (Java Console Version)
 * ============================================================
 *
 * Trong phiên bản console Java, Player được đại diện bởi 1 nhân vật
 * tổng hợp thay vì 3 pokemon riêng biệt (như giao diện React).
 *
 * Chỉ số ban đầu (đồng bộ với đội pokemon trong App.jsx):
 *   HP  = 575 (= 220 Wailord + 180 Bulbasaur + 175 Charmander)
 *   ATK = 29  (= trung bình ATK 3 pokemon: (30+24+34)/3)
 *
 * HP giảm khi bị quái tấn công (takeDamage).
 * HP có thể được hồi phục bằng bình máu trong Dungeon (heal).
 */
public class Player {

    /**
     * Máu hiện tại (Health Points).
     * Không final vì thay đổi liên tục trong trận chiến.
     */
    private int hp;

    /** Sát thương cơ bản mỗi đòn đánh của người chơi. */
    private int attack;

    /**
     * Khởi tạo người chơi với HP và ATK cho trước.
     * Được gọi từ GameController.initGame() với HP=575, ATK=29.
     */
    public Player(int hp, int attack) {
        this.hp     = hp;
        this.attack = attack;
    }

    // ── Getters ────────────────────────────────────────────────

    /** Trả về HP hiện tại. Dùng để hiển thị trên console và API. */
    public int getHp() { return hp; }

    /** Trả về sát thương mỗi đòn đánh. */
    public int getAttack() { return attack; }

    // ── Actions ───────────────────────────────────────────────

    /**
     * Hồi phục HP thêm một lượng amount.
     * Gọi khi người chơi nhặt bình máu trong Dungeon.
     * Không giới hạn HP tối đa trong console version.
     */
    public void heal(int amount) {
        if (amount < 0) return; // Không nhận giá trị âm
        hp += amount;
    }

    /**
     * Trừ HP một lượng dmg (bị quái đánh).
     * HP không được xuống dưới 0.
     * Gọi từ combatLoop() mỗi khi quái phản công.
     */
    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    /**
     * Kiểm tra người chơi còn sống không (HP > 0).
     * Gọi từ startGameLoop() để quyết định có tiếp tục vòng lặp game không.
     */
    public boolean isAlive() {
        return hp > 0;
    }

    /** Hiển thị thông tin người chơi (dùng cho debug/log). */
    @Override
    public String toString() {
        return "Player{hp=" + hp + ", attack=" + attack + "}";
    }
}
