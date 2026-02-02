package com.csd201.dungeon.ds;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Graph {
    private final int n;
    private final ArrayList<Integer>[] adj;

    @SuppressWarnings("unchecked")
    public Graph(int n) {
        this.n = n;
        adj = new ArrayList[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
    }

    public void addEdge(int u, int v) {
        adj[u].add(v);
        adj[v].add(u);
    }

    public void dfs(int start) {
        boolean[] vis = new boolean[n];
        dfsRec(start, vis);
        System.out.println();
    }

    private void dfsRec(int u, boolean[] vis) {
        vis[u] = true;
        System.out.print(u + " ");
        for (int v : adj[u]) {
            if (!vis[v]) dfsRec(v, vis);
        }
    }

    public int[] bfsShortestPath(int start, int target) {
        boolean[] vis = new boolean[n];
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = -1;

        Queue<Integer> q = new LinkedList<>();
        q.add(start);
        vis[start] = true;

        while (!q.isEmpty()) {
            int u = q.poll();
            if (u == target) break;
            for (int v : adj[u]) {
                if (!vis[v]) {
                    vis[v] = true;
                    parent[v] = u;
                    q.add(v);
                }
            }
        }
        return parent;
    }
}
