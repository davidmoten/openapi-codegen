package org.davidmoten.oa3.generator.runtime.internal;

import java.util.regex.Pattern;

public final class Preconditions {

    public static <T> T checkNotNull(T t, String parameterName) {
        if (t == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
        return t;
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
    
    public static String checkMatchesPattern(String s, String pattern, String name) {
        if (!Pattern.matches(pattern, s)) {
            throw new IllegalArgumentException(name + " must match this regex pattern: " + pattern);
        }
        return s;
    }
}
