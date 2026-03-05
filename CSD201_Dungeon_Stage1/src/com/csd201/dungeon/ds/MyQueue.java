package com.csd201.dungeon.ds;

public class MyQueue<T> {
    private Node<T> head;
    private Node<T> tail;

    public MyQueue() {
        head = null;
        tail = null;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public void enqueue(T data) {
        Node<T> n = new Node<>(data);
        if (isEmpty()) {
            head = tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
    }

    public T dequeue() {
        if (isEmpty()) return null;
        T data = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        return data;
    }

    public T peek() {
        if (isEmpty()) return null;
        return head.data;
    }
}
