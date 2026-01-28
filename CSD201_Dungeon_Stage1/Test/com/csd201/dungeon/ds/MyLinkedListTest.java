package com.csd201.dungeon.ds;

import com.csd201.dungeon.model.Item;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyLinkedListTest {

    @Test
    public void remove_onEmpty_returnsNull() {
        MyLinkedList<Item> list = new MyLinkedList<>();
        Item removed = list.remove(i -> i.getId() == 1);
        assertNull(removed);
        assertEquals(0, list.size());
    }

    @Test
    public void remove_head_singleNode() {
        MyLinkedList<Item> list = new MyLinkedList<>();
        list.add(new Item(1, "Potion"));
        Item removed = list.remove(i -> i.getId() == 1);

        assertNotNull(removed);
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    public void remove_middle_threeNodes() {
        MyLinkedList<Item> list = new MyLinkedList<>();
        list.add(new Item(1, "Potion"));
        list.add(new Item(2, "Key"));
        list.add(new Item(3, "Bomb"));

        Item removed = list.remove(i -> i.getId() == 2);

        assertNotNull(removed);
        assertEquals(2, list.size());
        assertNull(list.search(i -> i.getId() == 2));
    }

    @Test
    public void remove_tail_twoNodes() {
        MyLinkedList<Item> list = new MyLinkedList<>();
        list.add(new Item(1, "Potion"));
        list.add(new Item(2, "Key"));

        Item removed = list.remove(i -> i.getId() == 2);

        assertNotNull(removed);
        assertEquals(1, list.size());
        assertNotNull(list.search(i -> i.getId() == 1));
    }
}
