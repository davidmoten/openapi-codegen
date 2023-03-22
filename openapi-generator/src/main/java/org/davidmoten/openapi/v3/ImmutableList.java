package org.davidmoten.openapi.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

final class ImmutableList<T> implements Iterable<T> {

        private final List<T> list;

        ImmutableList() {
            this(new ArrayList<>());
        }

        ImmutableList(List<T> list) {
            this.list = list;
        }

        ImmutableList<T> add(T value) {
            List<T> list2 = new ArrayList<>(list);
            list2.add(value);
            return new ImmutableList<T>(list2);
        }

        @SafeVarargs
        static <T> ImmutableList<T> of(T... values) {
            List<T> list = Arrays.asList(values);
            return new ImmutableList<>(list);
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

    }