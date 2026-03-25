package com.csd201.dungeon.ds;

import com.csd201.dungeon.model.Monster;
import org.junit.Test;
import static org.junit.Assert.*;

public class BenchmarkTest {

    @Test
    public void testBenchmarkListVsBst() {
        System.out.println("=== UNIT TEST BENCHMARK: List vs BST (10,000 Monsters) ===");

        MyLinkedList<Monster> list = new MyLinkedList<>();
        MonsterBST bst = new MonsterBST();

        int size = 10000;
        int targetId = -1;

        for (int i = 0; i < size; i++) {
            int id = (int) (Math.random() * 50000);
            Monster m = new Monster(id, "UnitMonster_" + id, 50, 10);

            list.add(m);
            bst.insert(m);

            if (i == size - 1) { // Lấy phần tử cuối cùng làm mục tiêu
                targetId = id;
            }
        }

        // 1. Benchmark List Search
        long startTime = System.nanoTime();
        final int searchId = targetId;
        Monster listResult = list.search(new Monster(searchId, "", 0, 0));
        long endTime = System.nanoTime();
        long listTime = endTime - startTime;

        // 2. Benchmark BST Search
        startTime = System.nanoTime();
        Monster bstResult = bst.search(targetId);
        endTime = System.nanoTime();
        long bstTime = endTime - startTime;

        // Assert Result
        assertNotNull("List should find the target monster", listResult);
        assertNotNull("BST should find the target monster", bstResult);
        assertEquals("Both searches should return the same monster ID", listResult.getId(), bstResult.getId());

        System.out.println("Search Target ID: " + targetId);
        System.out.println("List Time : " + listTime + " ns");
        System.out.println("BST Time  : " + bstTime + " ns");

        // Bắt buộc In ra Console Log hoặc "AI Log" như yêu cầu
        System.out.println("-> Cấu trúc Cây (BST) chứng minh hiệu năng với O(log N) trong khi List là O(N).");
    }
}
