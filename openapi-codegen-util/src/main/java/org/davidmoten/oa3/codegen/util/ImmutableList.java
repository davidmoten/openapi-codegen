package org.davidmoten.oa3.codegen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class ImmutableList<T> implements Iterable<T> {

    private final List<T> list;

    private static final ImmutableList<Object> EMPTY = of();

    public ImmutableList() {
        this(new ArrayList<>());
    }

    public ImmutableList(List<T> list) {
        this.list = list;
    }

    public ImmutableList<T> add(T value) {
        List<T> list2 = new ArrayList<>(list);
        list2.add(value);
        return new ImmutableList<>(list2);
    }

    @SafeVarargs
    public static <T> ImmutableList<T> of(T... values) {
        List<T> list = Arrays.asList(values);
        return new ImmutableList<>(list);
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> empty() {
        return (ImmutableList<T>) EMPTY;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    public T last() {
        return list.get(list.size() - 1);
    }

    public T first() {
        return list.get(0);
    }

    public T secondLast() {
        return list.get(list.size() - 2);
    }

    public int size() {
        return list.size();
    }

}