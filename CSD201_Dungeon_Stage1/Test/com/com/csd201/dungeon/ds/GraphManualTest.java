/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.com.csd201.dungeon.ds;

/**
 *
 * @author ASUS
 */
import com.csd201.dungeon.ds.Graph;
import com.csd201.dungeon.ds.Graph;

public class GraphManualTest {

    public static void main(String[] args) {

        System.out.println("=== GRAPH MANUAL TEST ===");

        Graph g = new Graph(6);

        // Add edges (rooms connected)
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(1, 4);
        g.addEdge(4, 5);

        // DFS
        System.out.println("\nDFS from room 0:");
        g.dfs(0);

        // BFS shortest path
        System.out.println("\nBFS shortest path from 0 to 5:");
        g.bfsShortestPath(0, 5);

        System.out.println("\n✅ GRAPH TEST DONE");
    }
}