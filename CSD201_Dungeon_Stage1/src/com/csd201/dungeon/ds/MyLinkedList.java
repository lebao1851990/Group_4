package com.csd201.dungeon.ds;

public class MyLinkedList<T> {
    private Node<T> head;

    public MyLinkedList() {
        head = null;
    }

    public Node<T> getHead() {
        return head;
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

    public T search(T target) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data.equals(target)) {
                return cur.data;
            }
            cur = cur.next;
        }
        return null;
    }

    public T remove(T target) {
        if (head == null)
            return null;

        if (head.data.equals(target)) {
            T data = head.data;
            head = head.next;
            return data;
        }

        Node<T> prev = head;
        Node<T> cur = head.next;

        while (cur != null) {
            if (cur.data.equals(target)) {
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
