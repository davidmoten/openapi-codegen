package org.davidmoten.oa3.codegen.generator.internal;

public final class Mutable<T> {

    public T value;

    public Mutable(T value) {
        this.value = value;
    }

    public static <T> Mutable<T> create(T value) {
        return new Mutable<T>(value);
    }
}
