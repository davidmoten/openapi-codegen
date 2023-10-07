package org.davidmoten.oa3.codegen.runtime;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.openapitools.jackson.nullable.JsonNullable;

public final class PreconditionsBase {

    private final Function<? super String, ? extends RuntimeException> factory;

    public PreconditionsBase(Function<? super String, ? extends RuntimeException> factory) {
        this.factory = factory;
    }

    public <T> T checkNotNull(T t, String parameterName) {
        if (t == null) {
            throw factory.apply(parameterName + " cannot be null");
        }
        return t;
    }

    ////////////////////////////////
    // minimum
    ////////////////////////////////

    public void checkMinimum(Number x, String min, String name) {
        checkMinimum(x, min, name, false);
    }

    public void checkMinimum(Optional<? extends Number> x, String min, String name) {
        if (x.isPresent()) {
            checkMinimum(x.get(), min, name, false);
        }
    }

    public void checkMinimum(JsonNullable<? extends Number> x, String min, String name) {
        if (hasValue(x)) {
            checkMinimum(x.get(), min, name, false);
        }
    }

    public void checkMinimum(Collection<?> list, String min, String name) {
        checkMinimum(list, min, name, false);
    }

    @SuppressWarnings("unchecked")
    public void checkMinimum(Collection<?> list, String min, String name, boolean exclusive) {
        if (list == null) {
            return;
        }
        for (Object x : list) {
            if (x instanceof Number) {
                checkMinimum((Number) x, min, name, exclusive);
            } else if (x instanceof JsonNullable) {
                checkMinimum((JsonNullable<Number>) x, min, name, exclusive);
            }
        }
    }

    public void checkMinimum(Number x, String min, String name, boolean exclusive) {
        if (x == null) {
            return;
        }
        int compare = new BigDecimal(min).compareTo(BigDecimal.valueOf(x.doubleValue()));
        if (!exclusive && compare > 0) {
            throw factory.apply(name + " must be >= " + min);
        }
        if (exclusive && compare >= 0) {
            throw factory.apply(name + " must be > " + min);
        }
    }

    public void checkMinimum(Optional<? extends Number> x, String min, String name, boolean exclusive) {
        if (x.isPresent()) {
            checkMinimum(x.get(), min, name, exclusive);
        }
    }

    public void checkMinimum(JsonNullable<? extends Number> x, String min, String name, boolean exclusive) {
        if (hasValue(x)) {
            checkMinimum(x.get(), min, name, exclusive);
        }
    }

    ////////////////////////////////
    // maximum
    ////////////////////////////////

    public void checkMaximum(Collection<?> list, String max, String name) {
        checkMaximum(list, max, name, false);
    }

    @SuppressWarnings("unchecked")
    public void checkMaximum(Collection<?> list, String max, String name, boolean exclusive) {
        if (list == null) {
            return;
        }
        for (Object x : list) {
            if (x instanceof Number) {
                checkMaximum((Number) x, max, name, exclusive);
            } else if (x instanceof JsonNullable) {
                checkMaximum((JsonNullable<Number>) x, max, name, exclusive);
            }
        }
    }

    public void checkMaximum(Number x, String max, String name) {
        checkMaximum(x, max, name, false);
    }

    public void checkMaximum(Optional<? extends Number> x, String max, String name) {
        if (x.isPresent()) {
            checkMaximum(x.get(), max, name, false);
        }
    }

    public void checkMaximum(JsonNullable<? extends Number> x, String max, String name) {
        if (hasValue(x)) {
            checkMaximum(x.get(), max, name, false);
        }
    }

    public void checkMaximum(Number x, String max, String name, boolean exclusive) {
        if (x == null) {
            return;
        }
        int compare = new BigDecimal(max).compareTo(BigDecimal.valueOf(x.doubleValue()));
        if (!exclusive && compare < 0) {
            throw factory.apply(name + " must be <= " + max);
        }
        if (exclusive && compare <= 0) {
            throw factory.apply(name + " must be < " + max);
        }
    }

    public void checkMaximum(Optional<? extends Number> x, String max, String name, boolean exclusive) {
        if (x.isPresent()) {
            checkMaximum(x.get(), max, name, exclusive);
        }
    }

    public void checkMaximum(JsonNullable<? extends Number> x, String max, String name, boolean exclusive) {
        if (hasValue(x)) {
            checkMaximum(x.get(), max, name, exclusive);
        }
    }

    ////////////////////////////////
    // minLength
    ////////////////////////////////

    public void checkMinLength(String s, int minLength, String name) {
        if (s != null && s.length() < minLength) {
            throw factory.apply(name + " must have a length of at least " + minLength);
        }
    }

    @SuppressWarnings("unchecked")
    public void checkMinLength(Optional<?> s, int minLength, String name) {
        if (s.isPresent()) {
            if (s.get() instanceof Collection) {
                checkMaxLength((Collection<String>) s.get(), minLength, name);
            } else {
                checkMinLength((String) s.get(), minLength, name);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void checkMinLength(JsonNullable<?> s, int minLength, String name) {
        if (hasValue(s)) {
            if (s.get() instanceof Collection) {
                checkMaxLength((Collection<String>) s.get(), minLength, name);
            } else {
                checkMinLength((String) s.get(), minLength, name);
            }
        }
    }

    public void checkMinLength(Collection<?> list, int minLength, String name) {
        if (list != null && list //
                .stream() //
                .filter(x -> {
                    final String s;
                    if (x instanceof String) {
                        s = (String) x;
                    } else if (x instanceof JsonNullable) {
                        @SuppressWarnings("unchecked")
                        JsonNullable<String> n = (JsonNullable<String>) x;
                        if (hasValue(n)) {
                            s = n.get();
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    return s.length() < minLength;
                }) //
                .findAny() //
                .isPresent()) {
            throw factory.apply(name + " elements must have a length of at least " + minLength);
        }
    }

    ////////////////////////////////
    // maxLength
    ////////////////////////////////

    public void checkMaxLength(String s, int maxLength, String name) {
        if (s != null && s.length() > maxLength) {
            throw factory.apply(name + " must have a length of at most " + maxLength);
        }
    }

    public void checkMaxLength(Collection<?> list, int maxLength, String name) {
        if (list != null && list //
                .stream() //
                .filter(x -> {
                    final String s;
                    if (x instanceof String) {
                        s = (String) x;
                    } else if (x instanceof JsonNullable) {
                        @SuppressWarnings("unchecked")
                        JsonNullable<String> n = (JsonNullable<String>) x;
                        if (hasValue(n)) {
                            s = n.get();
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    return s.length() > maxLength;
                }) //
                .findAny() //
                .isPresent()) {
            throw factory.apply(name + " elements must have a length of at most " + maxLength);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void checkMaxLength(Optional<T> s, int maxLength, String name) {
        if (s.isPresent()) {
            if (s.get() instanceof Collection) {
                checkMaxLength((Collection<String>) s.get(), maxLength, name);
            } else {
                checkMaxLength((String) s.get(), maxLength, name);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void checkMaxLength(JsonNullable<T> s, int maxLength, String name) {
        if (hasValue(s)) {
            if (s.get() instanceof Collection) {
                checkMaxLength((Collection<String>) s.get(), maxLength, name);
            } else {
                checkMaxLength((String) s.get(), maxLength, name);
            }
        }
    }

    ////////////////////////////////
    // minSize
    ////////////////////////////////

    public void checkMinSize(Collection<?> collection, int min, String name) {
        if (collection != null && collection.size() < min) {
            throw factory.apply("collection must be have at least " + min + " elements");
        }
    }

    public <T> void checkMinSize(Optional<? extends Collection<T>> collection, int min, String name) {
        if (collection != null && collection.isPresent() && collection.get().size() < min) {
            throw factory.apply("collection must be have at least " + min + " elements");
        }
    }

    public <T> void checkMinSize(JsonNullable<? extends Collection<T>> collection, int min, String name) {
        if (collection != null && hasValue(collection) && collection.get().size() < min) {
            throw factory.apply("collection must be have at least " + min + " elements");
        }
    }

    ////////////////////////////////
    // maxSize
    ////////////////////////////////

    public void checkMaxSize(Collection<?> collection, int max, String name) {
        if (collection != null && collection.size() > max) {
            throw factory.apply("collection must be have at most " + max + " elements");
        }
    }

    public <T> void checkMaxSize(Optional<? extends Collection<T>> collection, int max, String name) {
        if (collection != null && collection.isPresent() && collection.get().size() > max) {
            throw factory.apply("collection must be have at most " + max + " elements");
        }
    }
    
    public <T> void checkMaxSize(JsonNullable<? extends Collection<T>> collection, int max, String name) {
        if (collection != null && hasValue(collection) && collection.get().size() > max) {
            throw factory.apply("collection must be have at most " + max + " elements");
        }
    }
    ////////////////////////////////
    // matchesPattern
    ////////////////////////////////

    public void checkMatchesPattern(String s, String pattern, String name) {
        if (s != null && !Pattern.matches(pattern, s)) {
            throw factory.apply(name + " must match this regex pattern: " + pattern);
        }
    }

    public void checkMatchesPattern(Collection<?> list, String pattern, String name) {
        if (list != null && list //
                .stream().filter(x -> {
                    final String s;
                    if (x instanceof String) {
                        s = (String) x;
                    } else if (x instanceof JsonNullable) {
                        @SuppressWarnings("unchecked")
                        JsonNullable<String> n = (JsonNullable<String>) x;
                        if (hasValue(n)) {
                            s = n.get();
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    return !Pattern.matches(pattern, s);
                }) //
                .findAny() //
                .isPresent()) {
            throw factory.apply(name + " elements must match this regex pattern: " + pattern);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> void checkMatchesPattern(Optional<T> s, String pattern, String name) {
        if (s.isPresent()) {
            if (s.get() instanceof Collection) {
                checkMatchesPattern((Collection<String>) s.get(), pattern, name);
            } else {
                checkMatchesPattern((String) s.get(), pattern, name);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> void checkMatchesPattern(JsonNullable<T> s, String pattern, String name) {
        if (hasValue(s)) {
            if (s.get() instanceof Collection) {
                checkMatchesPattern((Collection<String>) s.get(), pattern, name);
            } else {
                checkMatchesPattern((String) s.get(), pattern, name);
            }
        }
    }

    private static boolean hasValue(JsonNullable<?> x) {
        return x.isPresent() && x.get() != null;
    }
}
