package com.csd201.dungeon.ds;

public class Graph {
    private final int n; // Số lượng đỉnh (phòng)
    private final MyLinkedList<Integer>[] adj; // Danh sách kề

    @SuppressWarnings("unchecked")
    public Graph(int n) {
        this.n = n;
        adj = new MyLinkedList[n];
        for (int i = 0; i < n; i++)
            adj[i] = new MyLinkedList<>();
    }

    public void addEdge(int u, int v) {
        adj[u].add(v);
        adj[v].add(u); // Đồ thị vô hướng (đi được 2 chiều)
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

    // --- File I/O: Đọc bản đồ Dungeon từ File text ---
    public static Graph loadFromFile(String filename) {
        try (java.util.Scanner sc = new java.util.Scanner(new java.io.File(filename))) {
            int n = sc.nextInt(); // Dòng 1: Số đỉnh
            int m = sc.nextInt(); // Dòng 1: Số cạnh
            Graph g = new Graph(n);
            for (int i = 0; i < m; i++) { // Các dòng sau: Các cặp cạnh (u, v)
                int u = sc.nextInt();
                int v = sc.nextInt();
                g.addEdge(u, v);
            }
            return g;
        } catch (java.io.FileNotFoundException e) {
            System.err.println("File not found: " + filename);
            return null;
        }
    }
}
