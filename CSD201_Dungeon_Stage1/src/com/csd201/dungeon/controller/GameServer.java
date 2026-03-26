package com.csd201.dungeon.controller;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.csd201.dungeon.model.Room;
import com.csd201.dungeon.model.Monster;
import com.csd201.dungeon.ds.Node;
import com.csd201.dungeon.ds.MyLinkedList;

/**
 * ============================================================
 *  GAME SERVER — REST API Server Giao Tiếp Với React UI
 * ============================================================
 *
 * Dùng thư viện com.sun.net.httpserver (có sẵn trong JDK)
 * để tạo HTTP server đơn giản, nhẹ, không cần framework ngoài.
 *
 * CÁC ENDPOINT (cổng 8081):
 * ┌────────────────────┬─────────────────────────────────────┐
 * │ GET /api/status    │ Trả HP, ATK người chơi + phòng hiện │
 * │ GET /api/map       │ Trả danh sách phòng + cạnh Graph    │
 * │ GET /api/move?id=X │ Di chuyển đến phòng X               │
 * │ GET /api/attack    │ Người chơi tấn công quái hiện tại   │
 * └────────────────────┴─────────────────────────────────────┘
 *
 * TẤT CẢ response đều là JSON UTF-8.
 * CORS được bật cho mọi origin (để React localhost:5173 gọi được).
 *
 * Kiến trúc Handler Pattern:
 *   Mỗi endpoint là một inner class implements HttpHandler,
 *   truy cập dữ liệu game thông qua GameController instance.
 */
public class GameServer {

    /** Tham chiếu đến GameController — nguồn dữ liệu game duy nhất. */
    private GameController controller;

    /**
     * Tạo GameServer, gắn với GameController để truy cập dữ liệu.
     *
     * @param controller  Instance GameController đang chạy game
     */
    public GameServer(GameController controller) {
        this.controller = controller;
    }

    /**
     * Khởi động HTTP server, đăng ký tất cả endpoint và bắt đầu lắng nghe.
     * Gọi từ Main.java trong một Thread riêng để không block console game.
     *
     * @param port  Cổng lắng nghe (8081 mặc định)
     */
    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Đăng ký từng context (URL path → Handler tương ứng)
        server.createContext("/api/status", new StatusHandler()); // Trạng thái người chơi
        server.createContext("/api/map",    new MapHandler());     // Bản đồ + Graph edges
        server.createContext("/api/move",   new MoveHandler());    // Di chuyển phòng
        server.createContext("/api/attack", new AttackHandler());  // Tấn công quái

        server.setExecutor(null); // Dùng executor mặc định (single-threaded)
        server.start();
        System.out.println("🚀 API Server chạy tại http://localhost:" + port);
        System.out.println("CORS bật cho tất cả origin.");
    }

    // ============================================================
    //  HANDLER: GET /api/attack
    // ============================================================

    /**
     * Xử lý lệnh ĐÁNH từ React UI.
     *
     * Logic:
     *   1. Lấy Monster trong phòng hiện tại
     *   2. Nếu Monster còn sống: player đánh monster (takeDamage)
     *   3. Nếu monster vẫn còn sống: monster phản đòn player
     *
     * Response JSON:
     *   { "success": true/false, "monsterHp": 123, "playerHp": 456 }
     *
     * Lưu ý: React UI tự quản lý combat state chi tiết hơn (multi-pokemon,
     * element multiplier, ...). API này chỉ phục vụ bản console fallback.
     */
    private class AttackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("DEBUG: [ATTACK] Nhận request");
            addCorsHeaders(exchange); // Thêm header CORS trước mọi response

            // Xử lý preflight OPTIONS (trình duyệt gửi trước POST/PUT)
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Lấy phòng và Monster hiện tại
            Room current = controller.getRooms()[controller.getCurrentRoomId()];
            Monster m    = current.getMonster();
            boolean success = false;

            if (m != null && m.isAlive()) {
                m.takeDamage(controller.getPlayer().getAttack()); // Player đánh Monster
                success = true;
                if (m.isAlive()) {
                    // Monster còn sống → phản đòn ngay
                    controller.getPlayer().takeDamage(m.getAttack());
                }
            }

            // Build JSON response
            String json = String.format(
                "{\"success\": %b, \"monsterHp\": %d, \"playerHp\": %d}",
                success,
                (m != null ? m.getHp() : 0),
                controller.getPlayer().getHp()
            );
            sendResponse(exchange, json);
        }
    }

    // ============================================================
    //  HANDLER: GET /api/status
    // ============================================================

    /**
     * Trả về trạng thái hiện tại của người chơi.
     *
     * React UI gọi endpoint này khi khởi động để đồng bộ trạng thái
     * ban đầu (phòng, HP, ATK) từ Java backend.
     *
     * Response JSON:
     *   { "currentRoomId": 0, "playerHp": 575, "playerAtk": 29 }
     */
    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("DEBUG: [STATUS] Nhận request");
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String json = String.format(
                "{\"currentRoomId\": %d, \"playerHp\": %d, \"playerAtk\": %d}",
                controller.getCurrentRoomId(),
                controller.getPlayer().getHp(),
                controller.getPlayer().getAttack()
            );
            sendResponse(exchange, json);
        }
    }

    // ============================================================
    //  HANDLER: GET /api/map
    // ============================================================

    /**
     * Trả về TOÀN BỘ bản đồ dungeon:
     *   - Danh sách phòng + Monster trong từng phòng (nếu có)
     *   - Danh sách cạnh (edges) của Graph — dùng MyLinkedList.getNeighbors()
     *
     * React UI gọi endpoint này khi tải game để vẽ bản đồ SVG
     * và hiển thị thông tin Monster từng phòng.
     *
     * Response JSON:
     * {
     *   "rooms": [ {"id":0,"name":"Cổng Vào","monster":null}, ... ],
     *   "edges": { "0":[1], "1":[0,2,4], ... }
     * }
     *
     * Lưu ý: edges được lấy từ Graph.getNeighbors(i) trả về MyLinkedList,
     * sau đó duyệt từng Node trong Linked List để build JSON.
     */
    private class MapHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("DEBUG: [MAP] Nhận request");
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // ── Build JSON danh sách phòng ─────────────────────────
            StringBuilder sb = new StringBuilder();
            sb.append("{\"rooms\": [");
            Room[] rooms = controller.getRooms();
            for (int i = 0; i < rooms.length; i++) {
                Monster m = rooms[i].getMonster();

                // Nếu phòng có Monster → serialize thông tin, ngược lại → null
                String mJson = (m == null) ? "null" :
                    String.format("{\"id\": %d, \"name\": \"%s\", \"hp\": %d, \"atk\": %d}",
                        m.getId(), m.getName(), m.getHp(), m.getAttack());

                sb.append(String.format("{\"id\": %d, \"name\": \"%s\", \"monster\": %s}",
                    rooms[i].getId(), rooms[i].getName(), mJson));
                if (i < rooms.length - 1) sb.append(",");
            }

            // ── Build JSON danh sách cạnh Graph ────────────────────
            // Duyệt MyLinkedList<Integer> (danh sách kề) của từng đỉnh
            sb.append("], \"edges\": {");
            for (int i = 0; i < rooms.length; i++) {
                sb.append("\"").append(i).append("\": [");

                // getNeighbors(i) trả về MyLinkedList chứa ID các phòng kề
                MyLinkedList<Integer> neighbors = controller.getMap().getNeighbors(i);
                Node<Integer> cur = neighbors.getHead(); // Bắt đầu duyệt từ đầu

                while (cur != null) {
                    sb.append(cur.data);               // Thêm ID phòng kề vào JSON
                    if (cur.next != null) sb.append(","); // Dấu phẩy nếu chưa hết
                    cur = cur.next;                    // Duyệt sang Node tiếp theo
                }
                sb.append("]");
                if (i < rooms.length - 1) sb.append(",");
            }
            sb.append("}}");

            sendResponse(exchange, sb.toString());
        }
    }

    // ============================================================
    //  HANDLER: GET /api/move?id=X
    // ============================================================

    /**
     * Xử lý lệnh DI CHUYỂN đến phòng có ID = X.
     *
     * Quy trình:
     *   1. Đọc tham số id từ query string (?id=X)
     *   2. Lấy danh sách kề của phòng hiện tại (MyLinkedList từ Graph)
     *   3. Duyệt Linked List kiểm tra X có phải phòng kề không
     *   4. Nếu hợp lệ → cập nhật currentRoomId trong GameController
     *
     * Áp dụng cùng logic với movePlayer() trong GameController
     * nhưng qua HTTP thay vì console input.
     *
     * Response JSON:
     *   { "success": true, "newRoomId": 1 }
     *   { "success": false, "newRoomId": 0 }   (không tìm thấy / không hợp lệ)
     */
    private class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("DEBUG: [MOVE] Nhận request");
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Đọc tham số id từ URL query: /api/move?id=X
            String query = exchange.getRequestURI().getQuery();
            int targetId = -1;
            if (query != null && query.contains("id=")) {
                try {
                    targetId = Integer.parseInt(query.split("id=")[1]);
                } catch (Exception e) { /* Giá trị id không hợp lệ */ }
            }

            // Kiểm tra targetId có trong danh sách kề của phòng hiện tại không
            boolean success = false;
            MyLinkedList<Integer> neighbors =
                controller.getMap().getNeighbors(controller.getCurrentRoomId());

            // Duyệt Linked List để tìm targetId trong danh sách phòng kề
            Node<Integer> cur = neighbors.getHead();
            while (cur != null) {
                if (cur.data == targetId) {
                    controller.setCurrentRoomId(targetId); // Di chuyển thành công
                    success = true;
                    break;
                }
                cur = cur.next; // Duyệt tiếp nút kế
            }

            String json = String.format(
                "{\"success\": %b, \"newRoomId\": %d}",
                success, controller.getCurrentRoomId()
            );
            sendResponse(exchange, json);
        }
    }

    // ============================================================
    //  HELPER METHODS
    // ============================================================

    /**
     * Thêm header CORS vào mọi HTTP response.
     * Cần thiết để trình duyệt cho phép React (localhost:5173)
     * gọi API Java (localhost:8081) — hai origin khác nhau.
     */
    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json"); // Response là JSON
    }

    /**
     * Gửi JSON response với HTTP 200 OK.
     * Encode chuỗi UTF-8 để hỗ trợ tiếng Việt có dấu.
     *
     * @param exchange  Đối tượng HTTP exchange
     * @param json      Chuỗi JSON cần gửi về client
     */
    private void sendResponse(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes("UTF-8"); // Encode UTF-8 (hỗ trợ tiếng Việt)
        exchange.sendResponseHeaders(200, bytes.length); // HTTP 200 OK
        OutputStream os = exchange.getResponseBody();
        os.write(bytes); // Ghi dữ liệu vào response body
        os.close();      // Đóng stream sau khi gửi xong
    }
}
