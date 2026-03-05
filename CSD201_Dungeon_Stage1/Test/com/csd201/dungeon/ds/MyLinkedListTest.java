package com.csd201.dungeon.ds;

import com.csd201.dungeon.model.Item;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyLinkedListTest {

    @Test
    public void testAddAndSearch() {
        MyLinkedList<Item> list = new MyLinkedList<>();
        list.add(new Item(10, "Potion"));
        list.add(new Item(20, "Sword"));
        list.add(new Item(30, "Shield"));

        Item result = list.search(new Item(20, ""));
        assertNotNull("Item should be found", result);
        assertEquals(20, result.getId());

        Item notFound = list.search(new Item(100, ""));
        assertNull("Non-existent item should return null", notFound);
    }

    @Test
    public void testRemove() {
        MyLinkedList<Item> list = new MyLinkedList<>();
        list.add(new Item(10, "Potion"));
        list.add(new Item(20, "Sword"));

        Item removed = list.remove(new Item(20, ""));
        assertNotNull("Item should be removed successfully", removed);
        assertEquals(20, removed.getId());

        Item notFound = list.search(new Item(20, ""));
        assertNull("Item should no longer exist after removal", notFound);
    }

    @Test
    public void testRemoveHead() {
        MyLinkedList<Item> list = new MyLinkedList<>();
        list.add(new Item(10, "A"));
        list.add(new Item(20, "B"));

        Item removed = list.remove(new Item(10, ""));
        assertNotNull(removed);
        assertEquals(10, removed.getId());

        // Assert New Head is 20
        assertNotNull(list.getHead().data);
        assertEquals(20, list.getHead().data.getId());
    }

    @Test
    public void testEmptyList() {
        MyLinkedList<Item> list = new MyLinkedList<>();
        assertNull("Search on empty list should not throw NullPointerException", list.search(new Item(10, "")));
        assertNull("Remove on empty list should not throw NullPointerException", list.remove(new Item(10, "")));
    }
}
