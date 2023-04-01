package org.davidmoten.oa3.generator.runtime.internal;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.regex.Pattern;

public final class Preconditions {

    public static <T> T checkNotNull(T t, String parameterName) {
        if (t == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
        return t;
    }

    public static void checkMinimum(Number x, String min, String name, boolean exclusive) {
        int compare = new BigDecimal(min).compareTo(BigDecimal.valueOf(x.doubleValue()));
        if (!exclusive && compare > 0) {
            throw new IllegalArgumentException(name + " must be >= " + min);
        }
        if (exclusive && compare >= 0) {
            throw new IllegalArgumentException(name + " must be > " + min);
        }
    }

    public static void checkMaximum(Number x, String max, String name, boolean exclusive) {
        int compare = new BigDecimal(max).compareTo(BigDecimal.valueOf(x.doubleValue()));
        if (!exclusive && compare < 0) {
            throw new IllegalArgumentException(name + " must be <= " + max);
        }
        if (exclusive && compare <= 0) {
            throw new IllegalArgumentException(name + " must be < " + max);
        }
    }

    public static String checkMinLength(String s, int minLength, String name) {
        if (s.length() < minLength) {
            throw new IllegalArgumentException(name + " must have a length of at least " + minLength);
        }
        return s;
    }

    public static String checkMaxLength(String s, int maxLength, String name) {
        if (s.length() > maxLength) {
            throw new IllegalArgumentException(name + " must have a length of at most " + maxLength);
        }
        return s;
    }
    
    public static <S extends Collection<T>, T> S checkMinSize(S collection, int min, String name) {
        if (collection.size() < min) {
            throw new IllegalArgumentException("collection must be have at least " + min + " elements");
        }
        return collection;
    }
    
    public static <S extends Collection<T>, T> S checkMaxSize(S collection, int max, String name) {
        if (collection.size() > max) {
            throw new IllegalArgumentException("collection must be have at most " + max + " elements");
        }
        return collection;
    }

    public static String checkMatchesPattern(String s, String pattern, String name) {
        if (!Pattern.matches(pattern, s)) {
            throw new IllegalArgumentException(name + " must match this regex pattern: " + pattern);
        }
        return s;
    }

}
