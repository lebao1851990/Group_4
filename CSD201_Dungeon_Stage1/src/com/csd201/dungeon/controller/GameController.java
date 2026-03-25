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

public class GameController {
    private Graph map;
    private Room[] rooms;
    private MonsterBST monsterDict;
    private Player player;
    private InventoryService inventory;
    private int currentRoomId;
    private Scanner scanner;

    public Graph getMap() { return map; }
    public Room[] getRooms() { return rooms; }
    public MonsterBST getMonsterDict() { return monsterDict; }
    public Player getPlayer() { return player; }
    public InventoryService getInventory() { return inventory; }
    public int getCurrentRoomId() { return currentRoomId; }
    public void setCurrentRoomId(int id) { this.currentRoomId = id; }

    public GameController() {
        scanner = new Scanner(System.in);
        initGame();
    }

    private void initGame() {
        System.out.println("====== KHỞI TẠO THE DUNGEON CRAWLER ======");
        
        // 1. Tải bản đồ từ file bằng Graph
        map = Graph.loadFromFile("map.txt");
        if (map == null) {
            System.out.println("Lỗi: Không thể tải bản đồ từ file map.txt!");
            return;
        }
        
        // 2. Tạo các phòng dựa trên dữ liệu file text
        int numRooms = map.getNumVertices();
        rooms = new Room[numRooms];
        rooms[0] = new Room(0, "Cổng Vào Dungeon");
        rooms[1] = new Room(1, "Khu Rừng Tối");
        rooms[2] = new Room(2, "Hang Lửa");
        rooms[3] = new Room(3, "Đầm Lầy Chết");
        rooms[4] = new Room(4, "Tháp Bóng Đêm");
        rooms[5] = new Room(5, "Phòng Ngai Boss");
        
        // 3. Tạo BST - Từ điển Quái Vật (Monster Encyclopedia)
        // BST sắp xếp theo Monster ID, tìm kiếm O(log n)
        monsterDict = new MonsterBST();
        // Cấp 1 - Quái Thường
        monsterDict.insert(new Monster(101, "Caterpie Sâu",     20,  5));
        monsterDict.insert(new Monster(102, "Goblin Rừng",      40, 10));
        // Cấp 2 - Quái Mạnh
        monsterDict.insert(new Monster(103, "Machamp Đá Tảng",  60, 18));
        monsterDict.insert(new Monster(104, "Dragonair Thủy",   80, 22));
        monsterDict.insert(new Monster(105, "Charizard Lửa",    70, 25));
        // Cấp 3 - Quái Elite
        monsterDict.insert(new Monster(106, "Orc Đột Biến",    100, 28));
        monsterDict.insert(new Monster(107, "Gengar Bóng Tối",   90, 35));
        // Cấp 4 - BOSS
        monsterDict.insert(new Monster(108, "Rayquaza Boss",    200, 50));
        System.out.println(">> BST nạp xong 8 loài quái vật!");
        
        // 4. Bố trí quái vật vào các phòng (độ khó tăng dần)
        rooms[1].setMonster(new Monster(102, "Goblin Rừng",      40, 10));
        rooms[2].setMonster(new Monster(105, "Charizard Lửa",    70, 25));
        rooms[3].setMonster(new Monster(103, "Machamp Đá Tảng",  60, 18));
        rooms[4].setMonster(new Monster(107, "Gengar Bóng Tối",   90, 35));
        rooms[5].setMonster(new Monster(108, "Rayquaza Boss",    200, 50));
        
        // Đặt Loot / Item rải rác trong Dungeon
        rooms[1].setItem(new Item(1, "Kiếm sắt (+10 Dmg)"));
        rooms[3].setItem(new Item(2, "Bình HP (+50 Máu)"));
        rooms[4].setItem(new Item(3, "Chìa khóa Cổng Rồng"));

        // 5. Khởi tạo Thông số Người chơi & Túi đồ Linked List
        player = new Player(100, 20); // HP: 100, Atk: 20
        inventory = new InventoryService();
        currentRoomId = 0; // Luôn bắt đầu từ Room ID 0
        
        System.out.println("Hệ thống nạp xong Data Structures (Array, LinkedList, BST, Graph) ! \nBắt đầu cuộc hành trình!!\n");
    }

    public void startGameLoop() {
        if (map == null) return;
        boolean playing = true;
        
        while (playing && player.isAlive()) {
            Room current = rooms[currentRoomId];
            System.out.println("\n================================================");
            System.out.println("📍 VỊ TRÍ HIỆN TẠI: [" + current.getName() + "]");
            System.out.println("💖 NGƯỜI CHƠI: " + player.getHp() + " HP | ⚔️ LỰC CHIẾN: " + player.getAttack());
            System.out.println("================================================\n");
            
            checkRoomEvents(current);
            if (!player.isAlive()) {
                System.out.println("💀 BẠN ĐÃ TỬ TRẬN TẠI PHÒNG " + currentRoomId + ". GAME OVER!");
                break;
            }

            // Chống vòng lặp hiển thị nếu Boss chết (Bạn đã win)
            boolean win = true;
            for (Room r : rooms) {
                if (r.getMonster() != null && r.getMonster().getName().contains("Boss") && r.getMonster().isAlive()) {
                    win = false;
                }
            }
            if(win) {
                 System.out.println("\n🎉🎉 CHÚC MỪNG! BẠN ĐÃ TIÊU DIỆT ĐƯỢC BOSS KHU VỰC VÀ GIÀNH CHIẾN THẮNG!");
                 break;
            }

            System.out.println("\n[ TRÌNH ĐIỀU KHIỂN HÀNH ĐỘNG ]");
            System.out.println("1. 🚶 Di chuyển qua lối đi khác (Graph Navigation)");
            System.out.println("2. 🎒 Mở xem túi đồ (Linked List Read)");
            System.out.println("3. 🧭 Dùng la bàn ma thuật chỉ đường tới phòng chứa Boss (Graph BFS Shortest Path)");
            System.out.println("0. 🛑 Thoát game");
            System.out.print("👉 Lựa chọn của bạn: ");
            
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    movePlayer();
                    break;
                case "2":
                    inventory.showInventory();
                    break;
                case "3":
                    System.out.println("🧭 Khởi động La Bàn Dijkstra tìm đường tới Phòng 5 (Boss Room)...");
                    map.printDijkstraPath(currentRoomId, 5); // Đổi từ printPath sang printDijkstraPath
                    break;
                case "0":
                    System.out.println("Thoát trò chơi... Bye bye!");
                    playing = false;
                    break;
                default:
                    System.out.println("❌ Bạn bấm sai nút, hãy chọn lại!");
            }
        }
        System.out.println("\n--- 🛑 KẾT THÚC THE DUNGEON CRAWLER ---");
    }

    private void checkRoomEvents(Room room) {
        Monster m = room.getMonster();
        if (m != null && m.isAlive()) {
            System.out.println("⚠️ CẢNH BÁO: Phía trước có Quái vật [" + m.getName() + "]");
            System.out.println(">> Đang quét dữ liệu từ khóa trong BST Monster Dictionary...");
            Monster dictInfo = monsterDict.search(m.getId());
            if(dictInfo != null) {
                System.out.println(">> (BST Result) Chủng loài: " + dictInfo.getName() + " | Sức mạnh atk: " + dictInfo.getAttack() + " | Tối đa HP: " + dictInfo.getHp());
            }

            combatLoop(m);
        } else if (m != null && !m.isAlive()){
            System.out.println("💀 Cảnh vật hoang tàn. Xác của [" + m.getName() + "] đang nằm gục trên mặt đất.");
        }

        Item item = room.getItem();
        if (item != null) {
            System.out.println("🎁 Bạn tinh mắt phát hiện thấy góc phòng có đồ vật: [" + item.getName() + "]");
            inventory.addItem(item);
            System.out.println("✅ Đã cất [" + item.getName() + "] vào Túi Đồ của bạn!");
            
            // Nếu là máu hoặc kiếm thì cộng đồ luôn cho khỏe!
            if(item.getName().contains("Bình HP")) {
                 player.heal(50);
                 System.out.println(">>> Đã bú bình máu (+50HP). Máu hiện tại: " + player.getHp());
            }
            // Xóa item ở map vì nhặt rồi
            room.setItem(null); 
        }
    }

    private void combatLoop(Monster m) {
        while (m.isAlive() && player.isAlive()) {
            System.out.println("\n--- ⚔️ BƯỚC VÀO TRẬN CHIẾN ---");
            System.out.println("Tình trạng: Player (" + player.getHp() + " HP) || " + m.getName() + " (" + m.getHp() + " HP)");
            System.out.println("1. 🗡️ Đánh (Tấn công)");
            System.out.println("2. 🏃 Bỏ chạy (Run away)");
            System.out.print("Quyết định của bạn: ");
            String action = scanner.nextLine();
            
            if (action.equals("1")) {
                System.out.println("💥 Bạn chém " + m.getName() + " làm nó văng đi " + player.getAttack() + " Máu!");
                m.takeDamage(player.getAttack());
                if(m.isAlive()) {
                    System.out.println("🩸 " + m.getName() + " gầm gừ và cắn lại bạn, mất " + m.getAttack() + " HP!");
                    player.takeDamage(m.getAttack());
                } else {
                     System.out.println("🎊 BẠN ĐÃ HẠ GỤC ĐƯỢC QUÁI VẬT NÀY!");
                }
            } else if (action.equals("2")) {
                System.out.println("💨 Bạn sợ hãi bỏ chạy toán loạn!");
                player.takeDamage(5); // Chạy thì dẫm trúng bẫy đá, hụt 5 máu
                System.out.println("Bị sụp bẫy trong lúc rượt đuổi, mất 5 HP.");
                return; // Thoát combat loop tạm thời đứng chờ trong phòng, nếu không out thì tí bị đánh tiếp
            } else {
                 System.out.println("Luống cuống không biết chọn gì. Quái nhân cơ hội phang bạn!");
                 player.takeDamage(m.getAttack());
            }
        }
    }

    private void movePlayer() {
        MyLinkedList<Integer> neighbors = map.getNeighbors(currentRoomId);
        System.out.print("🚪 Từ đây có thể thông qua các Hành lang dẫn tới Phòng Cửa số: ");
        Node<Integer> cur = neighbors.getHead();
        while (cur != null) {
            System.out.print("[" + cur.data + "] ");
            cur = cur.next;
        }
        System.out.println("- (Hãy cẩn thận đường hẹp)");
        
        System.out.print("👇 Nhập số Phòng bạn muốn tới (Từ các ID cửa ở trên): ");
        try {
            int nextRoom = Integer.parseInt(scanner.nextLine());
            
            // Graph Navigation Logic check (đảm bảo đang không khinh công xuyên tường)
            boolean isValidMove = false;
            Node<Integer> check = neighbors.getHead();
            while (check != null) {
                if (check.data == nextRoom) {
                    isValidMove = true; 
                    break;
                }
                check = check.next;
            }
            
            if (isValidMove) {
                System.out.println("🚶 Bạn lách qua khe hở và bước chậm chạp sang Phòng " + nextRoom + "...");
                currentRoomId = nextRoom; // Thay đổi căn phòng trên bản đồ hiện diện
            } else {
                System.out.println("❌ Vách đá ở đó rất dày, làm gì có đường mà đi?");
            }
        } catch (Exception e) {
            System.out.println("❌ Máy nhầm ký tự, vui lòng nhập SỐ ĐÚNG CỦA CỬA!");
        }
    }
}
