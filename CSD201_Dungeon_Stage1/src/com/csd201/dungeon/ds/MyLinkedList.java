package com.csd201.dungeon.ds;

import java.util.function.Predicate;

public class MyLinkedList<T> {
    private Node<T> head;

    public MyLinkedList() {
        head = null;
    }

    public void add(T data) {
        Node<T> n = new Node<>(data);
        if (head == null) {
            head = n;
        } else {
            Node<T> cur = head;
            while (cur.next != null) {
                cur = cur.next;
            }
            cur.next = n;
        }
    }

    public T search(Predicate<T> condition) {
        Node<T> cur = head;
        while (cur != null) {
            if (condition.test(cur.data)) {
                return cur.data;
            }
            cur = cur.next;
        }
        return null;
    }

    public T remove(Predicate<T> condition) {
        if (head == null) return null;

        if (condition.test(head.data)) {
            T data = head.data;
            head = head.next;
            return data;
        }

        Node<T> prev = head;
        Node<T> cur = head.next;

        while (cur != null) {
            if (condition.test(cur.data)) {
                prev.next = cur.next;
                return cur.data;
            }
            prev = cur;
            cur = cur.next;
        }
        return null;
    }

    public void display() {
        Node<T> cur = head;
        while (cur != null) {
            System.out.println(cur.data);
            cur = cur.next;
        }
    }
}
