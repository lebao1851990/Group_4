package com.csd201.dungeon;

import com.csd201.dungeon.controller.GameController;
import com.csd201.dungeon.controller.GameServer;
import java.io.PrintStream;

/**
 * ============================================================
 *  MAIN — Điểm Khởi Chạy Ứng Dụng
 * ============================================================
 *
 * Chạy SONG SONG hai thành phần:
 *
 *   1. GameServer (Thread riêng, cổng 8081)
 *      → Cung cấp REST API cho giao diện React (web-ui)
 *      → Các endpoint: /api/status, /api/map, /api/move, /api/attack
 *
 *   2. GameController.startGameLoop() (Thread chính)
 *      → Vòng lặp game console, nhận input từ bàn phím
 *      → Dùng cùng một instance GameController với GameServer
 *         → dữ liệu game (phòng, HP, ...) NHẤT QUÁN ở cả hai nơi
 *
 * Sơ đồ kiến trúc:
 *
 *   ┌─────────────┐    HTTP :8081     ┌──────────────┐
 *   │  React UI   │ ←──────────────→ │  GameServer  │
 *   │  (web-ui)   │                  └──────┬───────┘
 *   └─────────────┘                         │ dùng chung
 *                                    ┌──────▼───────┐
 *   ┌─────────────┐                  │GameController│
 *   │   Console   │ ←──────────────→ │  (Game Data) │
 *   │  (Scanner)  │  startGameLoop   └──────────────┘
 *   └─────────────┘
 */
public class Main {

    public static void main(String[] args) {

        // ── Fix encoding tiếng Việt trên Windows console ──────────
        // Mặc định Windows dùng Code Page 1252 (Latin) — không hiển thị
        // được ký tự Unicode tiếng Việt. Phải redirect System.out/err
        // sang PrintStream UTF-8 TRƯỚC KHI in bất cứ thứ gì.
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (Exception e) {
            // Fallback: báo lỗi bằng ASCII nếu set encoding thất bại
            System.out.println("[WARN] Cannot set UTF-8 encoding: " + e.getMessage());
        }

        // Bước 1: Khởi tạo GameController (load Graph, BST, Array rooms, LinkedList inventory)
        GameController game = new GameController();

        // Bước 2: Khởi động GameServer trên Thread riêng để không block game console
        try {
            GameServer server = new GameServer(game);

            // Thread daemon: chạy song song với thread chính, tự dừng khi main dừng
            new Thread(() -> {
                try {
                    server.start(8081); // Lắng nghe cổng 8081
                } catch (Exception e) {
                    System.err.println("Loi khoi dong API Server: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Khong the tao GameServer: " + e.getMessage());
        }

        // Bước 3: Chạy vòng lặp game console (thread chính — chặn đến khi game kết thúc)
        game.startGameLoop();
    }
}
