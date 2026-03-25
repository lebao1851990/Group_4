package com.csd201.dungeon;

import com.csd201.dungeon.controller.GameController;
import com.csd201.dungeon.controller.GameServer;

public class Main {
    public static void main(String[] args) {
        GameController game = new GameController();
        
        // --- 🚀 KHỞI TẠO API SERVER GIAO TIẾP VỚI REACT ---
        try {
            GameServer server = new GameServer(game);
            new Thread(() -> {
                try {
                    server.start(8081);
                } catch (Exception e) {
                    System.err.println("❌ Lỗi khởi động Server: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.err.println("❌ Không thể tạo Server: " + e.getMessage());
        }

        // Chạy vòng lặp console song song
        game.startGameLoop();
    }
}
