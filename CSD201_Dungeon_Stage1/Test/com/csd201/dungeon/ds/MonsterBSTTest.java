package com.csd201.dungeon.ds;

import com.csd201.dungeon.model.Monster;
import org.junit.Test;
import static org.junit.Assert.*;

public class MonsterBSTTest {

    @Test
    public void testInsertAndSearch() {
        MonsterBST bst = new MonsterBST();

        // Test search on empty tree (prevent NullPointerException)
        assertNull("Search on empty BST should return null", bst.search(10));

        // Insert nodes
        bst.insert(new Monster(10, "Dragon", 100, 20));
        bst.insert(new Monster(5, "Goblin", 40, 8));
        bst.insert(new Monster(15, "Orc", 60, 12));

        // Search existing
        Monster found = bst.search(5);
        assertNotNull("Monster should be found", found);
        assertEquals("Found monster ID mismatch", 5, found.getId());

        // Search non-existing
        Monster notFound = bst.search(99);
        assertNull("Non-existent monster should return null", notFound);
    }

    @Test
    public void testDeleteNode() {
        MonsterBST bst = new MonsterBST();

        // Assert delete on empty tree doesn't throw NPE
        try {
            bst.delete(99); // Should simply do nothing, no Exceptions
            assertTrue(true);
        } catch (NullPointerException e) {
            fail("Delete on empty tree shouldn't throw NullPointerException");
        }

        bst.insert(new Monster(10, "Dragon", 100, 20));
        bst.insert(new Monster(5, "Goblin", 40, 8));
        bst.insert(new Monster(15, "Orc", 60, 12));

        // Delete leaf
        bst.delete(5);
        assertNull("Goblin should be deleted", bst.search(5));

        // Delete parent (Root with children)
        bst.delete(10);
        assertNull("Dragon should be deleted", bst.search(10));
        assertNotNull("Orc should remain", bst.search(15));
    }
}
