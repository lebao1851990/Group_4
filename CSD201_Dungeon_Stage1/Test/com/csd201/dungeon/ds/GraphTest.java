package com.csd201.dungeon.ds;

import org.junit.Test;
import static org.junit.Assert.*;

public class GraphTest {

    @Test
    public void testGraphInitializationAndEdges() {
        Graph g = new Graph(3);

        // Assert creation works
        assertNotNull(g);

        g.addEdge(0, 1);
        g.addEdge(1, 2);

        // As long as no Exception is thrown, basic edge cases pass
    }

    @Test
    public void testShortestPathBFS() {
        Graph g = new Graph(6);

        // Simple straight path 0 -> 1 -> 2
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);

        // Alternative longer path 0 -> 4 -> 5 -> 3
        g.addEdge(0, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 3);

        int[] parent = g.bfsShortestPath(0, 3);

        // To get to 3, BFS should prefer 0 -> 1 -> 2 -> 3
        // because it's the exact same length as the other route,
        // but 1 will be enqueued before 4 assuming 0 added 1 first.
        // What we really care about is ensuring we get a valid parent map

        assertNotNull("Path mapping parent array should not be null", parent);
        assertNotEquals("Target node 3 should have a parent", -1, parent[3]);

        // Validate path length manually
        int pathLength = 0;
        int current = 3;
        while (current != 0 && current != -1) {
            current = parent[current];
            pathLength++;
        }
        assertEquals("Path length from 0 to 3 should be 3 edges", 3, pathLength);
    }

    @Test
    public void testNoPathBFS() {
        Graph g = new Graph(5);
        g.addEdge(0, 1);
        g.addEdge(2, 3);

        // Disconnected nodes 0 and 2
        int[] parent = g.bfsShortestPath(0, 2);

        // Target 2 should have no parent (-1)
        assertEquals("Unreachable node should keep default parent -1", -1, parent[2]);
    }

    @Test
    public void testLoadFromFile() {
        // We know we can't reliably test this in Junit without the actual text file
        // but we can test invalid filenames to assure we avoid NPE and handle
        // exceptions gracefully.
        Graph gFail = Graph.loadFromFile("missing_file_for_test.txt");
        assertNull("Missing file should handle Exception inside and return Null safely instead of throwing up", gFail);
    }
}
