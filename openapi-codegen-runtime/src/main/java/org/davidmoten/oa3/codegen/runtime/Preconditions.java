package org.davidmoten.oa3.codegen.runtime;

import java.util.Collection;
import java.util.Optional;

public final class Preconditions {

    private static final PreconditionsBase p = new PreconditionsBase(IllegalArgumentException::new);

    public static <T> T checkNotNull(T t, String parameterName) {
        return p.checkNotNull(t, parameterName);
    }

    public static void checkMinimum(Number x, String min, String name) {
        p.checkMinimum(x, min, name);
    }

    public static void checkMinimum(Optional<? extends Number> x, String min, String name) {
        p.checkMinimum(x, min, name);
    }

    public static void checkMaximum(Number x, String max, String name) {
        p.checkMaximum(x, max, name);
    }

    public static void checkMaximum(Optional<? extends Number> x, String max, String name) {
        p.checkMaximum(x, max, name);
    }

    public static void checkMinimum(Number x, String min, String name, boolean exclusive) {
        p.checkMinimum(x, min, name, exclusive);
    }

    public static void checkMinimum(Optional<? extends Number> x, String min, String name, boolean exclusive) {
        p.checkMinimum(x, min, name, exclusive);
    }

    public static void checkMaximum(Number x, String max, String name, boolean exclusive) {
        p.checkMaximum(x, max, name, exclusive);
    }

    public static void checkMaximum(Optional<? extends Number> x, String max, String name, boolean exclusive) {
        p.checkMaximum(x, max, name, exclusive);
    }

    public static String checkMinLength(String s, int minLength, String name) {
        return p.checkMinLength(s, minLength, name);
    }
    
    public static <T extends Collection<String>> T checkMinLength(T list, int minLength, String name) {
        return p.checkMinLength(list, minLength, name);
    }

    public static Optional<String> checkMinLength(Optional<String> s, int minLength, String name) {
        return p.checkMinLength(s, minLength, name);
    }

    public static String checkMaxLength(String s, int maxLength, String name) {
        return p.checkMaxLength(s, maxLength, name);
    }

    public static <T extends Collection<String>> T checkMaxLength(T list, int maxLength, String name) {
        return p.checkMaxLength(list, maxLength, name);
    }
    
    public static Optional<String> checkMaxLength(Optional<String> s, int maxLength, String name) {
        return p.checkMaxLength(s, maxLength, name);
    }

    public static <S extends Collection<T>, T> S checkMinSize(S collection, int min, String name) {
        return p.checkMinSize(collection, min, name);
    }

    public static <S extends Collection<T>, T> Optional<S> checkMinSize(Optional<S> collection, int min, String name) {
        return p.checkMinSize(collection, min, name);
    }

    public static <S extends Collection<T>, T> S checkMaxSize(S collection, int max, String name) {
        return p.checkMaxSize(collection, max, name);
    }

    public static <S extends Collection<T>, T> Optional<S> checkMaxSize(Optional<S> collection, int max, String name) {
        return p.checkMaxSize(collection, max, name);
    }

    public static String checkMatchesPattern(String s, String pattern, String name) {
        return p.checkMatchesPattern(s, pattern, name);
    }

    public static Optional<String> checkMatchesPattern(Optional<String> s, String pattern, String name) {
        return p.checkMatchesPattern(s, pattern, name);
    }
    
    public static <T extends Collection<String>> T checkMatchesPattern(T s, String pattern, String name) {
        return p.checkMatchesPattern(s, pattern, name);
    }

}
