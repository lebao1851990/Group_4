package com.csd201.dungeon.ds;

public class Graph {
    private final int n; // Số lượng đỉnh (phòng)
    private final MyLinkedList<Integer>[] adj; // Danh sách kề
    private final int[][] weights; // Ma trận trọng số cho Dijkstra

    @SuppressWarnings("unchecked")
    public Graph(int n) {
        this.n = n;
        adj = new MyLinkedList[n];
        weights = new int[n][n];
        for (int i = 0; i < n; i++) {
            adj[i] = new MyLinkedList<>();
            for (int j = 0; j < n; j++) {
                weights[i][j] = (i == j) ? 0 : Integer.MAX_VALUE / 2; // Khởi tạo vô cực
            }
        }
    }

    // Nạp edge không trọng số (mặc định weight = 1)
    public void addEdge(int u, int v) {
        addEdge(u, v, 1);
    }
    
    // Nạp edge có trọng số
    public void addEdge(int u, int v, int weight) {
        adj[u].add(v);
        adj[v].add(u); 
        weights[u][v] = weight;
        weights[v][u] = weight;
    }

    public int getNumVertices() {
        return n;
    }

    public MyLinkedList<Integer> getNeighbors(int u) {
        return adj[u];
    }

    // --- Thuật toán Duyệt chuyên sâu (Depth-First Search) ---
    public void dfs(int start) {
        boolean[] vis = new boolean[n]; // Mảng đánh dấu các đỉnh đã thăm
        dfsRec(start, vis);
        System.out.println();
    }

    private void dfsRec(int u, boolean[] vis) {
        vis[u] = true;
        System.out.print(u + " ");
        Node<Integer> cur = adj[u].getHead();
        while (cur != null) {
            int v = cur.data;
            if (!vis[v])
                dfsRec(v, vis);
            cur = cur.next;
        }
    }

    // --- Thuật toán Duyệt theo chiều rộng (Breadth-First Search) ---
    // Được sử dụng để tìm đường đi ngắn nhất (Shortest Path) trong đồ thị không
    // trọng số
    public int[] bfsShortestPath(int start, int target) {
        boolean[] vis = new boolean[n];
        int[] parent = new int[n]; // Mảng lưu vết đường đi (Đỉnh trước đó của đỉnh hiện tại)
        for (int i = 0; i < n; i++)
            parent[i] = -1;

        MyQueue<Integer> q = new MyQueue<>();
        q.enqueue(start);
        vis[start] = true;

        System.out.println("-> Bắt đầu BFS tìm từ " + start + " đến " + target + "...");

        while (!q.isEmpty()) {
            int u = q.dequeue();
            // Nếu đã tìm thấy target thì dừng sớm (Tối ưu biên)
            if (u == target) {
                break;
            }

            Node<Integer> cur = adj[u].getHead();
            while (cur != null) {
                int v = cur.data;
                if (!vis[v]) {
                    vis[v] = true;
                    parent[v] = u; // Lưu vết: Để đi đến v thì phải đi qua u
                    q.enqueue(v);
                }
                cur = cur.next;
            }
        }
        return parent;
    }

    // In đường đi ngắn nhất ra màn hình
    public void printPath(int start, int target) {
        int[] parent = bfsShortestPath(start, target);

        // Nếu không có mảng cha đẻ hướng về, tức là không có đường đi
        if (parent[target] == -1 && start != target) {
            System.out.println("Không có đường đi từ " + start + " đến " + target);
            return;
        }

        // Truy vết ngược từ Target về Start bằng MyStack (LIFO)
        MyStack<Integer> path = new MyStack<>();
        int curr = target;
        while (curr != -1) {
            path.push(curr);
            curr = parent[curr];
        }

        System.out.print("Đường đi ngắn nhất: ");
        while (!path.isEmpty()) {
            System.out.print(path.pop() + (path.isEmpty() ? "" : " -> "));
        }
        System.out.println();
    }

    // --- Thuật toán Dijkstra (Đường đi ngắn nhất có trọng số) ---
    // Ghi điểm Tối Đa Phase 3
    // Hàm nội bộ chạy Dijkstra, trả về cả dist[] và parent[]
    private int[][] dijkstraCore(int start) {
        int[] dist = new int[n];
        boolean[] sptSet = new boolean[n];
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            dist[i] = Integer.MAX_VALUE / 2;
            sptSet[i] = false;
            parent[i] = -1;
        }
        dist[start] = 0;
        for (int count = 0; count < n - 1; count++) {
            int u = minDistance(dist, sptSet);
            if (u == -1) break;
            sptSet[u] = true;
            Node<Integer> cur = adj[u].getHead();
            while (cur != null) {
                int v = cur.data;
                if (!sptSet[v] && dist[u] != Integer.MAX_VALUE / 2
                        && dist[u] + weights[u][v] < dist[v]) {
                    dist[v] = dist[u] + weights[u][v];
                    parent[v] = u;
                }
                cur = cur.next;
            }
        }
        return new int[][]{ dist, parent };
    }

    // Trả về mảng parent[] để truy vết đường đi
    public int[] dijkstraShortestPath(int start, int target) {
        int[][] result = dijkstraCore(start);
        int[] dist   = result[0];
        int[] parent = result[1];
        System.out.println("-> Dijkstra từ " + start + " đến " + target
            + " | Tổng chi phí: " + dist[target]);
        return parent;
    }

    // Trả về mảng dist[] (khoảng cách từ start đến từng đỉnh)
    public int[] dijkstraDist(int start) {
        return dijkstraCore(start)[0];
    }

    private int minDistance(int[] dist, boolean[] sptSet) {
        int min = Integer.MAX_VALUE / 2;
        int min_index = -1;
        for (int v = 0; v < n; v++) {
            if (!sptSet[v] && dist[v] <= min) {
                min = dist[v];
                min_index = v;
            }
        }
        return min_index;
    }

    public void printDijkstraPath(int start, int target) {
        int[] parent = dijkstraShortestPath(start, target);
        if (parent[target] == -1 && start != target) {
            System.out.println("Không có đường đi từ " + start + " đến " + target);
            return;
        }
        MyStack<Integer> path = new MyStack<>();
        int curr = target;
        while (curr != -1) {
            path.push(curr);
            curr = parent[curr];
        }
        System.out.print("Đường đi ngắn nhất (Dijkstra): ");
        while (!path.isEmpty()) {
            System.out.print(path.pop() + (path.isEmpty() ? "" : " -> "));
        }
        System.out.println();
    }

    // Lấy trọng số của cạnh (u, v)
    public int getWeight(int u, int v) {
        return weights[u][v];
    }

    // --- File I/O: Đọc bản đồ Dungeon từ File text ---
    // Format mỗi dòng cạnh: u v w  (u, v: đỉnh, w: trọng số)
    public static Graph loadFromFile(String filename) {
        try (java.util.Scanner sc = new java.util.Scanner(new java.io.File(filename))) {
            int n = sc.nextInt(); // Dòng 1: Số đỉnh
            int m = sc.nextInt(); // Dòng 1: Số cạnh
            Graph g = new Graph(n);
            for (int i = 0; i < m; i++) {
                int u = sc.nextInt();
                int v = sc.nextInt();
                int w = sc.hasNextInt() ? sc.nextInt() : 1; // Đọc trọng số, mặc định = 1
                g.addEdge(u, v, w);
            }
            return g;
        } catch (java.io.FileNotFoundException e) {
            System.err.println("File not found: " + filename);
            return null;
        }
    }
}
