package com.csd201.dungeon.ds;

public class MyStack<T> {
    private Node<T> top;

    public MyStack() {
        top = null;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public void push(T data) {
        Node<T> n = new Node<>(data);
        n.next = top;
        top = n;
    }

    public T pop() {
        if (isEmpty()) return null;
        T data = top.data;
        top = top.next;
        return data;
    }

    public T peek() {
        if (isEmpty()) return null;
        return top.data;
    }
}
