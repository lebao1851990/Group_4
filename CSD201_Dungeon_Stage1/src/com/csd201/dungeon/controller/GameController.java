package com.csd201.dungeon.controller;

import java.util.Scanner;
import com.csd201.dungeon.ds.Graph;
import com.csd201.dungeon.ds.MonsterBST;
import com.csd201.dungeon.ds.MyLinkedList;
import com.csd201.dungeon.ds.Node;
import com.csd201.dungeon.model.Item;
import com.csd201.dungeon.model.Monster;
import com.csd201.dungeon.model.Player;
import com.csd201.dungeon.model.Room;
import com.csd201.dungeon.service.InventoryService;

/**
 * ============================================================
 *  GAME CONTROLLER — Bộ Điều Khiển Trung Tâm Của Game
 * ============================================================
 *
 * Đây là trái tim của toàn bộ chương trình. GameController:
 *   1. Khởi tạo tất cả cấu trúc dữ liệu (Graph, BST, LinkedList, Array)
 *   2. Điều phối vòng lặp game console (startGameLoop)
 *   3. Cung cấp dữ liệu cho GameServer để phục vụ React UI qua API
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │  CẤU TRÚC DỮ LIỆU ĐƯỢC SỬ DỤNG:                       │
 * │                                                         │
 * │  Graph (map)       → Bản đồ dungeon (6 đỉnh, 5 cạnh)  │
 * │    └─ MyLinkedList → Danh sách kề mỗi đỉnh             │
 * │    └─ weights[][]  → Ma trận trọng số cho Dijkstra     │
 * │                                                         │
 * │  MonsterBST        → Từ điển quái vật (tra cứu O(logn))│
 * │    └─ BSTNode      → Mỗi nút chứa 1 Monster            │
 * │                                                         │
 * │  Room[] (Array)    → Mảng 6 phòng (Array tĩnh)        │
 * │    └─ Monster      → Quái vật trong phòng              │
 * │    └─ Item         → Vật phẩm trong phòng              │
 * │                                                         │
 * │  InventoryService  → Túi đồ (MyLinkedList<Item>)       │
 * └─────────────────────────────────────────────────────────┘
 *
 * ⚖️ WIN RATE ~65%:
 *   Player HP 575, ATK 29 vs Monster HP 160-480, ATK 22-68
 *   0.92 × 0.90 × 1.00 × 0.88 × 0.86 ≈ 62-65% win rate
 */
public class GameController {

    /** Bản đồ Dungeon — đồ thị có trọng số, đọc từ file map.txt. */
    private Graph map;

    /**
     * Mảng 6 phòng (Room[6]) — cấu trúc Array tĩnh.
     * Index tương ứng với ID đỉnh trong Graph (0 → 5).
     */
    private Room[] rooms;

    /**
     * Từ điển Monster (BST) — tra cứu thông tin Monster theo ID.
     * Tìm kiếm O(log n) thay vì O(n) như mảng thông thường.
     */
    private MonsterBST monsterDict;

    /** Người chơi — đại diện tổng hợp cho đội 3 pokemon. */
    private Player player;

    /**
     * Túi đồ — quản lý item với Linked List.
     * Kích thước động, tự tăng khi nhặt item mới.
     */
    private InventoryService inventory;

    /** ID phòng hiện tại người chơi đang đứng (bắt đầu = 0). */
    private int currentRoomId;

    /** Scanner đọc input từ bàn phím (console game). */
    private Scanner scanner;

    // ── Getters: cho GameServer truy cập để trả dữ liệu về API ───

    /** Trả về đồ thị bản đồ (dùng bởi GameServer.MapHandler và MoveHandler). */
    public Graph getMap() { return map; }

    /** Trả về mảng tất cả phòng (dùng bởi GameServer.MapHandler). */
    public Room[] getRooms() { return rooms; }

    /** Trả về BST Monster (có thể dùng để tra cứu nếu cần mở rộng). */
    public MonsterBST getMonsterDict() { return monsterDict; }

    /** Trả về thông tin người chơi (HP, ATK — cho API /status và /attack). */
    public Player getPlayer() { return player; }

    /** Trả về túi đồ (hiện tại chỉ dùng trong console). */
    public InventoryService getInventory() { return inventory; }

    /** Trả về ID phòng hiện tại (cho API /status và /move). */
    public int getCurrentRoomId() { return currentRoomId; }

    /** Cập nhật phòng hiện tại (gọi từ GameServer.MoveHandler khi di chuyển thành công). */
    public void setCurrentRoomId(int id) { this.currentRoomId = id; }

    // ============================================================
    //  CONSTRUCTOR + INIT
    // ============================================================

    /** Tạo GameController mới và gọi khởi tạo toàn bộ game. */
    public GameController() {
        scanner = new Scanner(System.in);
        initGame();
    }

    /**
     * Khởi tạo toàn bộ game:
     *   Bước 1 → Load Graph (bản đồ) từ file
     *   Bước 2 → Tạo mảng Room[]
     *   Bước 3 → Nạp Monster vào BST (từ điển tra cứu)
     *   Bước 4 → Bố trí Monster vào từng phòng
     *   Bước 5 → Rải Item vào các phòng
     *   Bước 6 → Khởi tạo Player và Inventory (LinkedList)
     */
    private void initGame() {
        System.out.println("====== KHỞI TẠO THE DUNGEON CRAWLER ======");

        // ── BƯỚC 1: Load Graph (bản đồ) từ file map.txt ───────────
        // Graph dùng MyLinkedList<Integer> làm danh sách kề cho mỗi đỉnh,
        // và ma trận weights[][] để lưu trọng số cạnh cho Dijkstra.
        map = Graph.loadFromFile("map.txt");
        if (map == null) {
            // Không đọc được file → dừng khởi tạo
            System.out.println("Lỗi: Không thể tải bản đồ từ file map.txt!");
            return;
        }

        // ── BƯỚC 2: Tạo mảng Room[] dựa trên số đỉnh trong Graph ──
        // Array là cấu trúc dữ liệu truy cập nhanh O(1) theo index.
        // rooms[i] → phòng có ID = i (index = ID đỉnh Graph).
        int numRooms = map.getNumVertices(); // = 6 (theo map.txt)
        rooms = new Room[numRooms];
        // Tên phòng đồng bộ với ROOM_NAMES trong App.jsx
        rooms[0] = new Room(0, "Cổng Vào Dungeon");   // Điểm xuất phát, không quái
        rooms[1] = new Room(1, "Hang Goblin");          // Phòng 1 — Quái Goblin
        rooms[2] = new Room(2, "Hang Quỷ Nước");        // Phòng 2 — Quái Dragonair
        rooms[3] = new Room(3, "Đảo Bình Yên");         // Phòng 3 — Không quái, hồi máu
        rooms[4] = new Room(4, "Pháo Đài Orc");         // Phòng 4 — Quái Elite Gengar
        rooms[5] = new Room(5, "Phòng Boss Rayquaza");  // Phòng 5 — BOSS final

        // ── BƯỚC 3: Nạp Monster vào BST (Monster Encyclopedia) ───
        // BST sắp xếp theo Monster ID → tìm kiếm O(log n).
        // Thứ tự insert không quan trọng, BST tự sắp xếp.
        // Dữ liệu đồng bộ với MONSTER_DB trong App.jsx (Win Rate ~65%).
        monsterDict = new MonsterBST();

        // ── Tier 1: Quái Thường (xuất hiện Phòng 1) ───
        monsterDict.insert(new Monster(101, "Caterpie Sâu",     180, 22)); // Lính yếu nhất
        monsterDict.insert(new Monster(102, "Goblin Rừng",      190, 22)); // Boss phòng 1

        // ── Tier 2: Quái Mạnh (xuất hiện Phòng 2) ────
        monsterDict.insert(new Monster(103, "Machamp Đá Tảng",  240, 30)); // Quái đất mạnh
        monsterDict.insert(new Monster(104, "Dragonair Thủy",   240, 28)); // Boss phòng 2
        monsterDict.insert(new Monster(105, "Charizard Lửa",    260, 30)); // Quái lửa mạnh

        // ── Tier 3: Quái Elite (xuất hiện Phòng 4) ───
        monsterDict.insert(new Monster(106, "Orc Đột Biến",     390, 50)); // Lính Elite
        monsterDict.insert(new Monster(107, "Gengar Bóng Tối",  420, 56)); // Boss phòng 4

        // ── Tier 4: BOSS cuối game (Phòng 5) ─────────
        monsterDict.insert(new Monster(108, "Rayquaza Boss",    480, 68)); // BOSS tối thượng

        // ── Minion: Quái chặn lối (ID 9xx) ───────────
        monsterDict.insert(new Monster(991, "Lính Tiên Phong",  160, 22)); // Lính dẫn đường
        monsterDict.insert(new Monster(992, "Trung Vệ Hầm Ngục",200, 26)); // Lính canh giữa

        System.out.println(">> BST nạp xong 10 loài quái vật (Win Rate ~65%)");

        // ── BƯỚC 4: Bố trí Monster vào từng phòng ─────────────────
        // Mỗi phòng chỉ có 1 Monster duy nhất (boss của phòng đó).
        // Minion (991, 992) được hiển thị trên React UI nhưng không trong console.

        // Phòng 1 — Dễ (~97% thắng): Goblin cấp thường
        rooms[1].setMonster(new Monster(102, "Goblin Rừng",      190, 22));

        // Phòng 2 — Trung bình (~95% thắng): Dragonair nguyên tố nước
        rooms[2].setMonster(new Monster(104, "Dragonair Thủy",   240, 28));

        // Phòng 3 — Không có quái (phòng nghỉ, hồi máu)
        // rooms[3] không setMonster → monster = null

        // Phòng 4 — Khó (~93% thắng): Gengar Elite bóng tối
        rooms[4].setMonster(new Monster(107, "Gengar Bóng Tối",  300, 40));

        // Phòng 5 — Boss (~91% thắng): Rayquaza tối thượng
        rooms[5].setMonster(new Monster(108, "Rayquaza Boss",    360, 50));

        // ── BƯỚC 5: Rải Item vào các phòng ───────────────────────
        // Tương đương với inventory.buffAtk / buffHp / millenniumKey trong App.jsx
        rooms[1].setItem(new Item(1, "Kiếm sắt (+10 Dmg)"));    // Phụ trợ tấn công
        rooms[3].setItem(new Item(2, "Bình HP (+50 Máu)"));      // Hồi phục máu
        rooms[4].setItem(new Item(3, "Chìa khóa Cổng Rồng"));   // Mở cổng Boss

        // ── BƯỚC 6: Khởi tạo Player và Túi Đồ (Linked List) ─────
        // Player console đại diện tổng hợp cho 3 pokemon trong React UI:
        //   HP  = 265 (Wailord) + 220 (Bulbasaur) + 215 (Charmander) = 700
        //   ATK = (36 + 30 + 40) / 3 ≈ 35 (trung bình đội - Win Rate ~80%)
        player    = new Player(700, 35);
        inventory = new InventoryService(); // Túi đồ rỗng (Linked List rỗng)
        currentRoomId = 0; // Luôn bắt đầu từ phòng 0 — Cổng Vào Dungeon

        System.out.println("Player HP=" + player.getHp() + " | ATK=" + player.getAttack());
        System.out.println("Hệ thống nạp xong! (Graph + BST + LinkedList + Array)\n");
    }

    // ============================================================
    //  GAME LOOP — Vòng Lặp Chính Console Game
    // ============================================================

    /**
     * Vòng lặp chính của game console.
     * Mỗi vòng lặp:
     *   1. Hiển thị thông tin phòng hiện tại
     *   2. Kích hoạt sự kiện trong phòng (quái/item)
     *   3. Hiển thị menu lựa chọn và xử lý input
     *
     * Vòng lặp kết thúc khi: Player chết HOẶC Boss bị tiêu diệt.
     */
    public void startGameLoop() {
        if (map == null) return; // Không khởi tạo được → thoát

        boolean playing = true;

        while (playing && player.isAlive()) {
            Room current = rooms[currentRoomId];
            System.out.println("\n================================================");
            System.out.println("📍 VỊ TRÍ: [" + current.getName() + "]");
            System.out.println("💖 HP: " + player.getHp() + " | ⚔️ ATK: " + player.getAttack());
            System.out.println("================================================\n");

            // Kiểm tra và kích hoạt sự kiện (quái/item) trong phòng hiện tại
            checkRoomEvents(current);

            // Kiểm tra thua (player chết sau trận chiến)
            if (!player.isAlive()) {
                System.out.println("💀 BẠN ĐÃ TỬ TRẬN TẠI PHÒNG " + currentRoomId + ". GAME OVER!");
                break;
            }

            // Kiểm tra thắng: Boss trong phòng 5 đã bị tiêu diệt
            Monster boss = rooms[5].getMonster();
            if (boss != null && !boss.isAlive()) {
                System.out.println("\n🎉 CHÚC MỪNG! BẠN ĐÃ TIÊU DIỆT RAYQUAZA!");
                break;
            }

            // ── Hiển thị Menu Hành Động ─────────────────────────────
            System.out.println("\n[ MENU HÀNH ĐỘNG ]");
            System.out.println("1. 🚶 Di chuyển sang phòng khác (Graph Navigation)");
            System.out.println("2. 🎒 Xem túi đồ (Linked List Display)");
            System.out.println("3. 🧭 Tìm đường đến Boss (Dijkstra Shortest Path)");
            System.out.println("0. 🛑 Thoát game");
            System.out.print("👉 Lựa chọn: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    movePlayer(); // Di chuyển theo Graph (dùng Linked List kiểm tra hàng xóm)
                    break;
                case "2":
                    inventory.showInventory(); // Hiển thị Linked List túi đồ
                    break;
                case "3":
                    // Tìm đường ngắn nhất từ phòng hiện tại đến Boss (phòng 5)
                    System.out.println("🧭 Dijkstra tìm đường đến Boss Room...");
                    map.printDijkstraPath(currentRoomId, 5);
                    break;
                case "0":
                    System.out.println("Thoát game. Bye bye!");
                    playing = false;
                    break;
                default:
                    System.out.println("❌ Lựa chọn không hợp lệ!");
            }
        }
        System.out.println("\n--- 🛑 KẾT THÚC DUNGEON CRAWLER ---");
    }

    // ============================================================
    //  ROOM EVENTS — Xử Lý Sự Kiện Khi Vào Phòng
    // ============================================================

    /**
     * Kiểm tra và xử lý sự kiện trong phòng room:
     *   - Nếu có Monster còn sống → bắt buộc chiến đấu
     *   - Nếu có Item → tự động nhặt và áp dụng hiệu ứng
     *
     * @param room  Phòng hiện tại đang đứng
     */
    private void checkRoomEvents(Room room) {
        // ── Kiểm tra Monster ─────────────────────────────────────
        Monster m = room.getMonster();
        if (m != null && m.isAlive()) {
            System.out.println("⚠️  Phía trước có Quái vật: [" + m.getName() + "]");

            // Tra cứu BST để hiển thị thông số đầy đủ của Monster
            System.out.println(">> Tra cứu BST Monster Dictionary...");
            Monster dictInfo = monsterDict.search(m.getId());
            if (dictInfo != null) {
                System.out.println(">> (BST) " + dictInfo.getName()
                    + " | ATK: " + dictInfo.getAttack()
                    + " | MaxHP: " + dictInfo.getHp());
            }

            // Bắt đầu vòng lặp chiến đấu (bắt buộc, không thể bỏ qua)
            combatLoop(m);

        } else if (m != null && !m.isAlive()) {
            // Monster đã chết ở lượt trước → in trạng thái xác
            System.out.println("💀 Xác của [" + m.getName() + "] đang nằm yên.");
        }

        // ── Kiểm tra Item ─────────────────────────────────────────
        Item item = room.getItem();
        if (item != null) {
            System.out.println("🎁 Phát hiện: [" + item.getName() + "]");
            inventory.addItem(item); // Thêm vào Linked List túi đồ
            System.out.println("✅ Đã cất [" + item.getName() + "] vào túi đồ!");

            // Áp dụng hiệu ứng item ngay lập tức
            if (item.getName().contains("Bình HP")) {
                player.heal(50); // Hồi 50 HP
                System.out.println(">>> Bú bình máu! +50HP → Máu hiện tại: " + player.getHp());
            }

            // Xóa item khỏi phòng (chỉ nhặt được 1 lần)
            room.setItem(null);
        }
    }

    // ============================================================
    //  COMBAT LOOP — Vòng Lặp Chiến Đấu Turn-Based
    // ============================================================

    /**
     * Vòng lặp chiến đấu theo lượt giữa Player và Monster.
     * Mỗi lượt:
     *   - Người chơi chọn ĐÁNH hoặc CHẠY
     *   - Nếu đánh: player gây damage → monster phản đòn (nếu còn sống)
     *   - Nếu chạy: thoát combat nhưng mất 5 HP (sụp bẫy)
     *
     * Vòng lặp kết thúc khi Monster chết HOẶC Player chết.
     *
     * @param m  Monster cần tiêu diệt để thoát phòng
     */
    private void combatLoop(Monster m) {
        while (m.isAlive() && player.isAlive()) {
            System.out.println("\n--- ⚔️  CHIẾN ĐẤU ---");
            System.out.println("Player (" + player.getHp() + " HP) vs "
                + m.getName() + " (" + m.getHp() + " HP)");
            System.out.println("1. 🗡️  Đánh");
            System.out.println("2. 🏃 Bỏ chạy (mất 5 HP)");
            System.out.print("Quyết định: ");
            String action = scanner.nextLine();

            if (action.equals("1")) {
                // ── Người chơi tấn công ─────────────────────────
                System.out.println("💥 Chém " + m.getName() + " -" + player.getAttack() + " HP!");
                m.takeDamage(player.getAttack()); // Trừ HP Monster

                if (m.isAlive()) {
                    // ── Monster còn sống → phản đòn ──────────────
                    System.out.println("🩸 " + m.getName() + " phản đòn -" + m.getAttack() + " HP!");
                    player.takeDamage(m.getAttack()); // Trừ HP Player
                } else {
                    System.out.println("🎊 HẠ GỤC " + m.getName() + "!");
                }

            } else if (action.equals("2")) {
                // ── Bỏ chạy: mất 5 HP vì sụp bẫy ───────────────
                System.out.println("💨 Bỏ chạy! Sụp bẫy mất 5 HP.");
                player.takeDamage(5);
                return; // Thoát combat, monster vẫn còn (sẽ gặp lại)

            } else {
                // ── Không chọn gì → quái nhân cơ hội đánh ───────
                System.out.println("❓ Ngần ngại! " + m.getName() + " tranh thủ đánh!");
                player.takeDamage(m.getAttack());
            }
        }
    }

    // ============================================================
    //  MOVE PLAYER — Di Chuyển Người Chơi (Console Version)
    // ============================================================

    /**
     * Xử lý di chuyển người chơi qua Graph.
     *
     * Quy trình:
     *   1. Lấy danh sách kề của phòng hiện tại (MyLinkedList từ Graph)
     *   2. In ra các phòng có thể đến
     *   3. Đọc input người chơi
     *   4. Kiểm tra xem phòng muốn đến có trong danh sách kề không
     *      (bằng cách duyệt LinkedList) → ngăn "xuyên tường"
     *   5. Nếu hợp lệ → cập nhật currentRoomId
     */
    private void movePlayer() {
        // Lấy danh sách kề (Linked List) của phòng hiện tại từ Graph
        MyLinkedList<Integer> neighbors = map.getNeighbors(currentRoomId);

        // In ra các phòng có thể đến (duyệt Linked List)
        System.out.print("🚪 Có thể đến: ");
        Node<Integer> cur = neighbors.getHead(); // Bắt đầu từ đầu Linked List
        while (cur != null) {
            System.out.print("[" + cur.data + "] "); // In ID phòng kề
            cur = cur.next; // Chuyển đến nút tiếp theo
        }
        System.out.println();

        System.out.print("👇 Nhập ID phòng muốn đến: ");
        try {
            int nextRoom = Integer.parseInt(scanner.nextLine());

            // Kiểm tra nextRoom có trong danh sách kề không (duyệt Linked List)
            boolean isValidMove = false;
            Node<Integer> check = neighbors.getHead();
            while (check != null) {
                if (check.data == nextRoom) {
                    isValidMove = true; // Tìm thấy → di chuyển hợp lệ
                    break;
                }
                check = check.next;
            }

            if (isValidMove) {
                currentRoomId = nextRoom; // Cập nhật vị trí mới
                System.out.println("🚶 Bước vào " + rooms[nextRoom].getName() + "...");
            } else {
                System.out.println("❌ Không có lối đi đến phòng " + nextRoom + "!");
            }
        } catch (Exception e) {
            System.out.println("❌ Vui lòng nhập số hợp lệ!");
        }
    }
}
