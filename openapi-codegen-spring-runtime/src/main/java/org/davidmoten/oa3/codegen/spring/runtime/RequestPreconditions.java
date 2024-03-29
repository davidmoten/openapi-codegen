package org.davidmoten.oa3.codegen.spring.runtime;

import java.util.Collection;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.PreconditionsBase;
import org.openapitools.jackson.nullable.JsonNullable;

public final class RequestPreconditions {

    private static final PreconditionsBase p = new PreconditionsBase(BadRequestException::new);

    public static <T> T checkNotNull(T t, String parameterName) {
        return p.checkNotNull(t, parameterName);
    }

    public static void checkMinimum(Number x, String min, String name) {
        p.checkMinimum(x, min, name);
    }

    public static void checkMinimum(Optional<? extends Number> x, String min, String name) {
        p.checkMinimum(x, min, name);
    }

    public static void checkMinimum(JsonNullable<? extends Number> x, String min, String name) {
        p.checkMinimum(x, min, name);
    }
    
    public static void checkMaximum(Number x, String max, String name) {
        p.checkMaximum(x, max, name);
    }

    public static void checkMaximum(Optional<? extends Number> x, String max, String name) {
        p.checkMaximum(x, max, name);
    }
    
    public static void checkMaximum(JsonNullable<? extends Number> x, String max, String name) {
        p.checkMaximum(x, max, name);
    }

    public static void checkMinimum(Number x, String min, String name, boolean exclusive) {
        p.checkMinimum(x, min, name, exclusive);
    }

    public static void checkMinimum(Optional<? extends Number> x, String min, String name, boolean exclusive) {
        p.checkMinimum(x, min, name, exclusive);
    }

    public static void checkMinimum(Collection<? extends Number> x, String min, String name) {
        p.checkMinimum(x, min, name);
    }

    public static void checkMinimum(Collection<? extends Number> x, String min, String name, boolean exclusive) {
        p.checkMinimum(x, min, name, exclusive);
    }
    
    public static void checkMaximum(Number x, String max, String name, boolean exclusive) {
        p.checkMaximum(x, max, name, exclusive);
    }

    public static void checkMaximum(Optional<? extends Number> x, String max, String name, boolean exclusive) {
        p.checkMaximum(x, max, name, exclusive);
    }
    
    public static void checkMaximum(JsonNullable<? extends Number> x, String max, String name, boolean exclusive) {
        p.checkMaximum(x, max, name, exclusive);
    }

    public static void checkMaximum(Collection<? extends Number> x, String max, String name) {
        p.checkMaximum(x, max, name);
    }

    public static void checkMaximum(Collection<? extends Number> x, String max, String name, boolean exclusive) {
        p.checkMaximum(x, max, name, exclusive);
    }

    public static void checkMinLength(String s, int minLength, String name) {
        p.checkMinLength(s, minLength, name);
    }

    public static void checkMinLength(Optional<String> s, int minLength, String name) {
        p.checkMinLength(s, minLength, name);
    }

    public static void checkMinLength(JsonNullable<String> s, int minLength, String name) {
        p.checkMinLength(s, minLength, name);
    }
    
    public static void checkMaxLength(String s, int maxLength, String name) {
        p.checkMaxLength(s, maxLength, name);
    }

    public static void checkMinLength(Collection<String> list, int minLength, String name) {
        p.checkMinLength(list, minLength, name);
    }

    public static void checkMaxLength(Collection<String> list, int maxLength, String name) {
        p.checkMaxLength(list, maxLength, name);
    }

    public static void checkMaxLength(Optional<?> s, int maxLength, String name) {
        p.checkMaxLength(s, maxLength, name);
    }
    
    public static void checkMaxLength(JsonNullable<?> s, int maxLength, String name) {
        p.checkMaxLength(s, maxLength, name);
    }

    public static void checkMinSize(Collection<?> collection, int min, String name) {
        p.checkMinSize(collection, min, name);
    }

    public static <T> void checkMinSize(Optional<? extends Collection<T>> collection, int min, String name) {
        p.checkMinSize(collection, min, name);
    }
    
    public static <T> void checkMinSize(JsonNullable<? extends Collection<T>> collection, int min, String name) {
        p.checkMinSize(collection, min, name);
    }

    public static void checkMaxSize(Collection<?> collection, int max, String name) {
        p.checkMaxSize(collection, max, name);
    }

    public static <T> void checkMaxSize(Optional<? extends Collection<T>> collection, int max, String name) {
        p.checkMaxSize(collection, max, name);
    }

    public static void checkMatchesPattern(String s, String pattern, String name) {
        p.checkMatchesPattern(s, pattern, name);
    }

    public static void checkMatchesPattern(Collection<String> list, String pattern, String name) {
        p.checkMatchesPattern(list, pattern, name);
    }
    
    public static void checkMatchesPattern(Optional<?> s, String pattern, String name) {
        p.checkMatchesPattern(s, pattern, name);
    }

}
