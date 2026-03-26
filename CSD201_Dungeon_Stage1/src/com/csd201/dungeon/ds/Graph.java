package com.csd201.dungeon.ds;

/**
 * ============================================================
 *  GRAPH — Đồ Thị Có Trọng Số (Weighted Undirected Graph)
 *          Biểu diễn Bản Đồ Dungeon (6 phòng, 5 hành lang)
 * ============================================================
 *
 * Cấu trúc Bản Đồ Dungeon (6 đỉnh / 5 cạnh):
 *
 *   [0] ─2─ [1] ─3─ [2] ─1─ [3]
 *             │
 *            ─4─
 *             │
 *            [4] ─5─ [5-BOSS]
 *
 * Đỉnh = Phòng | Cạnh = Hành lang | Trọng số = Chi phí di chuyển
 *
 * Cài đặt bằng HAI cấu trúc song song:
 *   1. adj[]  — Mảng các MyLinkedList<Integer>: danh sách kề
 *              adj[u] = danh sách tất cả phòng có thể đi từ u
 *              → Duyệt O(deg) để kiểm tra hàng xóm
 *   2. weights[][] — Ma trận 2D: lưu trọng số cạnh (u,v)
 *              weights[u][v] = chi phí hành lang nối u ↔ v
 *              → Tra cứu O(1) trong Dijkstra
 *
 * THUẬT TOÁN CÓ SẴN:
 *   - DFS  (Depth-First Search)     → duyệt theo chiều sâu
 *   - BFS  (Breadth-First Search)   → đường đi ngắn nhất (không trọng số)
 *   - Dijkstra                      → đường đi ngắn nhất (có trọng số) ★
 *   - loadFromFile()                → đọc bản đồ từ file map.txt
 */
public class Graph {

    /** Số lượng đỉnh (phòng) trong đồ thị. */
    private final int n;

    /**
     * Danh sách kề — adj[u] chứa Linked List các ID đỉnh kề với u.
     * Ví dụ: adj[1] = [0, 2, 4] → từ phòng 1 có thể đến phòng 0, 2, 4.
     * Dùng MyLinkedList để minh họa cấu trúc dữ liệu tự cài.
     */
    private final MyLinkedList<Integer>[] adj;

    /**
     * Ma trận trọng số — weights[u][v] = chi phí đi từ u sang v.
     * Khởi tạo = Integer.MAX_VALUE/2 biểu thị "vô cực" (không có cạnh).
     * Dùng trong thuật toán Dijkstra để tính tổng chi phí tuyến đường.
     */
    private final int[][] weights;

    /**
     * Khởi tạo đồ thị n đỉnh với danh sách kề rỗng và ma trận trọng số
     * mặc định là vô cực (chưa có cạnh nào).
     */
    @SuppressWarnings("unchecked")
    public Graph(int n) {
        this.n   = n;
        adj      = new MyLinkedList[n];
        weights  = new int[n][n];

        for (int i = 0; i < n; i++) {
            adj[i] = new MyLinkedList<>(); // Mỗi đỉnh có một danh sách kề rỗng
            for (int j = 0; j < n; j++) {
                // Đường chéo = 0 (tự đến chính mình), còn lại = ∞
                weights[i][j] = (i == j) ? 0 : Integer.MAX_VALUE / 2;
            }
        }
    }

    // ============================================================
    //  ADD EDGE — Thêm Cạnh Vào Đồ Thị
    // ============================================================

    /** Thêm cạnh không trọng số (mặc định weight = 1). */
    public void addEdge(int u, int v) {
        addEdge(u, v, 1);
    }

    /**
     * Thêm cạnh CÓ TRỌNG SỐ giữa u và v (vô hướng: u↔v).
     * Cập nhật CẢ HAI chiều trong danh sách kề và ma trận trọng số.
     *
     * @param u      Đỉnh đầu (phòng u)
     * @param v      Đỉnh cuối (phòng v)
     * @param weight Chi phí hành lang nối u ↔ v
     */
    public void addEdge(int u, int v, int weight) {
        adj[u].add(v);          // Thêm v vào danh sách kề của u
        adj[v].add(u);          // Thêm u vào danh sách kề của v (vô hướng)
        weights[u][v] = weight; // Ghi trọng số chiều u→v
        weights[v][u] = weight; // Ghi trọng số chiều v→u
    }

    /** Trả về số lượng đỉnh (phòng) trong đồ thị. */
    public int getNumVertices() {
        return n;
    }

    /**
     * Trả về danh sách kề của đỉnh u (Linked List các ID phòng kề).
     * Gọi từ: GameController.movePlayer() để kiểm tra đường đi hợp lệ,
     *         GameServer.MoveHandler để xác nhận lệnh di chuyển từ UI.
     */
    public MyLinkedList<Integer> getNeighbors(int u) {
        return adj[u];
    }

    /** Trả về trọng số cạnh (u, v). Integer.MAX_VALUE/2 nếu không có cạnh. */
    public int getWeight(int u, int v) {
        return weights[u][v];
    }

    // ============================================================
    //  DFS — Duyệt Theo Chiều Sâu (Depth-First Search)
    // ============================================================

    /**
     * Duyệt DFS từ đỉnh start, in thứ tự thăm các đỉnh ra console.
     * DFS: từ một đỉnh, đi sâu nhất có thể theo một nhánh trước khi quay lui.
     * Dùng array boolean vis[] để tránh thăm lại đỉnh đã qua.
     */
    public void dfs(int start) {
        boolean[] vis = new boolean[n]; // vis[i] = true nếu đã thăm đỉnh i
        dfsRec(start, vis);
        System.out.println(); // Xuống dòng sau khi in xong
    }

    /**
     * Đệ quy DFS: đánh dấu đỉnh u đã thăm, rồi duyệt tất cả đỉnh kề
     * chưa thăm theo chiều sâu.
     */
    private void dfsRec(int u, boolean[] vis) {
        vis[u] = true;        // Đánh dấu đỉnh u đã thăm
        System.out.print(u + " "); // In ra đỉnh đang thăm

        // Duyệt toàn bộ đỉnh kề v của u qua Linked List
        Node<Integer> cur = adj[u].getHead();
        while (cur != null) {
            int v = cur.data;
            if (!vis[v]) {
                dfsRec(v, vis); // Chưa thăm → đệ quy đi sâu hơn
            }
            cur = cur.next; // Chuyển sang đỉnh kề tiếp theo
        }
    }

    // ============================================================
    //  BFS — Duyệt Theo Chiều Rộng (Breadth-First Search)
    //        Tìm Đường Đi Ngắn Nhất (Không Trọng Số)
    // ============================================================

    /**
     * BFS từ start đến target — trả về mảng parent[] để truy vết đường đi.
     * parent[v] = u có nghĩa "để đến v ta đi qua u".
     *
     * BFS dùng Queue (FIFO): lan rộng theo từng lớp → đảm bảo đường tìm thấy
     * đầu tiên luôn là ngắn nhất (tính theo SỐ CẠNH, không phải trọng số).
     */
    public int[] bfsShortestPath(int start, int target) {
        boolean[] vis = new boolean[n];
        int[] parent  = new int[n];
        for (int i = 0; i < n; i++) parent[i] = -1; // -1 = chưa thăm / không có cha

        MyQueue<Integer> q = new MyQueue<>();
        q.enqueue(start);   // Bắt đầu BFS từ đỉnh start
        vis[start] = true;

        System.out.println("-> Bắt đầu BFS tìm từ " + start + " đến " + target + "...");

        while (!q.isEmpty()) {
            int u = q.dequeue(); // Lấy đỉnh ở đầu hàng đợi ra xử lý
            if (u == target) break; // Đã tìm thấy đích → dừng sớm

            // Duyệt tất cả đỉnh kề v của u
            Node<Integer> cur = adj[u].getHead();
            while (cur != null) {
                int v = cur.data;
                if (!vis[v]) {
                    vis[v]    = true; // Đánh dấu v đã thăm
                    parent[v] = u;    // Ghi lại "cha" của v là u
                    q.enqueue(v);     // Đưa v vào cuối hàng đợi để xử lý tiếp
                }
                cur = cur.next;
            }
        }
        return parent; // Dùng parent[] để truy vết lại đường đi
    }

    /**
     * In đường đi ngắn nhất (BFS) từ start đến target ra console.
     * Dùng Stack để đảo ngược mảng parent[] thành thứ tự đúng.
     */
    public void printPath(int start, int target) {
        int[] parent = bfsShortestPath(start, target);

        // Kiểm tra có tồn tại đường đi không
        if (parent[target] == -1 && start != target) {
            System.out.println("Không có đường đi từ " + start + " đến " + target);
            return;
        }

        // Truy vết ngược từ target về start → push vào Stack
        MyStack<Integer> path = new MyStack<>();
        int curr = target;
        while (curr != -1) {
            path.push(curr);     // Đẩy từng đỉnh vào ngăn xếp
            curr = parent[curr]; // Đi ngược theo cha
        }

        // Pop ra theo thứ tự ĐÚNG (start → ... → target)
        System.out.print("Đường đi ngắn nhất: ");
        while (!path.isEmpty()) {
            System.out.print(path.pop() + (path.isEmpty() ? "" : " -> "));
        }
        System.out.println();
    }

    // ============================================================
    //  DIJKSTRA — Đường Đi Ngắn Nhất Có Trọng Số ★
    //             Được Dùng Cho Tính Năng "La Bàn" Trên UI
    // ============================================================

    /**
     * Lõi thuật toán Dijkstra — tính khoảng cách ngắn nhất từ start
     * đến tất cả các đỉnh trong đồ thị CÓ TRỌNG SỐ.
     *
     * Thuật toán Dijkstra (Greedy):
     *   1. dist[start] = 0, dist[mọi đỉnh khác] = ∞
     *   2. Lặp n-1 lần:
     *      a. Chọn đỉnh u chưa thăm có dist[u] nhỏ nhất
     *      b. Đánh dấu u đã thăm (vào "Shortest Path Tree")
     *      c. Cập nhật dist[v] = min(dist[v], dist[u] + weight[u][v])
     *         với mọi đỉnh v kề u chưa thăm
     *
     * Trả về mảng [dist[], parent[]] để dùng chung cho 2 mục đích.
     */
    private int[][] dijkstraCore(int start) {
        int[] dist   = new int[n]; // dist[i] = tổng chi phí ngắn nhất từ start đến i
        int[] parent = new int[n]; // parent[i] = đỉnh đứng trước i trên đường đi ngắn nhất
        boolean[] sptSet = new boolean[n]; // sptSet[i] = true nếu đỉnh i đã được xử lý

        // Khởi tạo: tất cả khoảng cách = ∞, chưa thăm đỉnh nào
        for (int i = 0; i < n; i++) {
            dist[i]   = Integer.MAX_VALUE / 2;
            sptSet[i] = false;
            parent[i] = -1;
        }
        dist[start] = 0; // Khoảng cách từ start đến chính nó = 0

        for (int count = 0; count < n - 1; count++) {
            // Bước a: Chọn đỉnh u chưa thăm có khoảng cách nhỏ nhất
            int u = minDistance(dist, sptSet);
            if (u == -1) break; // Mọi đỉnh còn lại không thể đến
            sptSet[u] = true;   // Đánh dấu u đã được xử lý

            // Bước c: Cập nhật khoảng cách các đỉnh v kề u
            Node<Integer> cur = adj[u].getHead();
            while (cur != null) {
                int v = cur.data;
                // Chỉ cập nhật nếu v chưa thăm và có đường qua u ngắn hơn
                if (!sptSet[v]
                        && dist[u] != Integer.MAX_VALUE / 2
                        && dist[u] + weights[u][v] < dist[v]) {
                    dist[v]   = dist[u] + weights[u][v]; // Cập nhật khoảng cách mới
                    parent[v] = u; // Ghi lại "đến v thì đi qua u"
                }
                cur = cur.next;
            }
        }
        return new int[][]{ dist, parent }; // Trả về cả dist[] và parent[]
    }

    /**
     * Hàm phụ: tìm đỉnh chưa thăm có dist[] nhỏ nhất.
     * Dùng trong vòng lặp Dijkstra để chọn đỉnh tối ưu tiếp theo.
     * Trả về -1 nếu không tìm thấy.
     */
    private int minDistance(int[] dist, boolean[] sptSet) {
        int min       = Integer.MAX_VALUE / 2;
        int min_index = -1;
        for (int v = 0; v < n; v++) {
            if (!sptSet[v] && dist[v] <= min) {
                min       = dist[v];
                min_index = v;
            }
        }
        return min_index;
    }

    /**
     * Chạy Dijkstra, trả về mảng parent[] để truy vết.
     * Cũng in ra tổng chi phí đường đi trên console.
     */
    public int[] dijkstraShortestPath(int start, int target) {
        int[][] result = dijkstraCore(start);
        int[] dist     = result[0];
        int[] parent   = result[1];
        System.out.println("-> Dijkstra từ " + start + " đến " + target
            + " | Tổng chi phí: " + dist[target]);
        return parent;
    }

    /**
     * Chạy Dijkstra, trả về mảng dist[] (khoảng cách đến từng đỉnh).
     * Dùng khi chỉ cần biết chi phí, không cần truy vết đường đi.
     */
    public int[] dijkstraDist(int start) {
        return dijkstraCore(start)[0];
    }

    /**
     * In đường đi ngắn nhất (Dijkstra) từ start đến target ra console.
     * Dùng Stack để đảo ngược mảng parent[] thành thứ tự đúng.
     * Được gọi từ GameController khi người chơi dùng "La Bàn Ma Thuật".
     */
    public void printDijkstraPath(int start, int target) {
        int[] parent = dijkstraShortestPath(start, target);

        if (parent[target] == -1 && start != target) {
            System.out.println("Không có đường đi từ " + start + " đến " + target);
            return;
        }

        // Truy vết ngược từ target → push vào Stack
        MyStack<Integer> path = new MyStack<>();
        int curr = target;
        while (curr != -1) {
            path.push(curr);
            curr = parent[curr];
        }

        // Pop Stack ra → thứ tự start → ... → target
        System.out.print("Đường đi ngắn nhất (Dijkstra): ");
        while (!path.isEmpty()) {
            System.out.print(path.pop() + (path.isEmpty() ? "" : " -> "));
        }
        System.out.println();
    }

    // ============================================================
    //  FILE I/O — Đọc Bản Đồ Từ File map.txt
    // ============================================================

    /**
     * Đọc cấu trúc đồ thị từ file văn bản (map.txt).
     *
     * Định dạng file:
     *   Dòng 1: <số đỉnh n> <số cạnh m>
     *   Dòng i=2..m+1: <u> <v> <weight>  (u, v: 2 đỉnh; weight: trọng số)
     *
     * Ví dụ file map.txt:
     *   6 5
     *   0 1 2
     *   1 2 3
     *   1 4 4
     *   2 3 1
     *   4 5 5
     *
     * Trả về null nếu file không tồn tại.
     */
    public static Graph loadFromFile(String filename) {
        try (java.util.Scanner sc = new java.util.Scanner(new java.io.File(filename))) {
            int n = sc.nextInt(); // Đọc số đỉnh
            int m = sc.nextInt(); // Đọc số cạnh
            Graph g = new Graph(n);
            for (int i = 0; i < m; i++) {
                int u = sc.nextInt(); // Đỉnh đầu cạnh
                int v = sc.nextInt(); // Đỉnh cuối cạnh
                int w = sc.hasNextInt() ? sc.nextInt() : 1; // Trọng số (mặc định 1 nếu thiếu)
                g.addEdge(u, v, w);
            }
            return g;
        } catch (java.io.FileNotFoundException e) {
            System.err.println("File not found: " + filename);
            return null; // Không tìm thấy file → trả về null
        }
    }
}
