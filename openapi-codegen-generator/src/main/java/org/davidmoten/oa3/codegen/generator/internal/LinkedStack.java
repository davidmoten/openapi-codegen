package org.davidmoten.oa3.codegen.generator.internal;

public final class LinkedStack<T> {
    LinkedStackNode<T> last;

    public void push(T value) {
        LinkedStackNode<T> node = new LinkedStackNode<>(value);
        if (last != null) {
            node.previous = last;
        }
        last = node;
    }

    public T pop() {
        if (last == null) {
            return null;
        } else {
            T v = last.value;
            last = last.previous;
            return v;
        }
    }

    public T peek() {
        if (last == null) {
            return null;
        } else {
            return last.value;
        }
    }

    public boolean isEmpty() {
        return last == null;
    }

    private static final class LinkedStackNode<T> {
        final T value;
        LinkedStackNode<T> previous;

        LinkedStackNode(T value) {
            this.value = value;
        }
    }

}