package org.davidmoten.oa3.codegen.runtime;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

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

    public void checkMinimum(Number x, String min, String name) {
        checkMinimum(x, min, name, false);
    }

    public void checkMinimum(Optional<? extends Number> x, String min, String name) {
        if (x.isPresent()) {
            checkMinimum(x.get(), min, name, false);
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

    public void checkMinimum(Number x, String min, String name, boolean exclusive) {
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

    public void checkMaximum(Number x, String max, String name, boolean exclusive) {
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

    public String checkMinLength(String s, int minLength, String name) {
        if (s.length() < minLength) {
            throw factory.apply(name + " must have a length of at least " + minLength);
        }
        return s;
    }

    public Optional<String> checkMinLength(Optional<String> s, int minLength, String name) {
        if (s.isPresent()) {
            checkMinLength(s.get(), minLength, name);
        }
        return s;
    }

    public String checkMaxLength(String s, int maxLength, String name) {
        if (s.length() > maxLength) {
            throw factory.apply(name + " must have a length of at most " + maxLength);
        }
        return s;
    }

    public Optional<String> checkMaxLength(Optional<String> s, int maxLength, String name) {
        if (s.isPresent()) {
            checkMaxLength(s.get(), maxLength, name);
        }
        return s;
    }

    public <S extends Collection<T>, T> S checkMinSize(S collection, int min, String name) {
        if (collection.size() < min) {
            throw factory.apply("collection must be have at least " + min + " elements");
        }
        return collection;
    }

    public <S extends Collection<T>, T> Optional<S> checkMinSize(Optional<S> collection, int min, String name) {
        if (collection != null && collection.isPresent() && collection.get().size() < min) {
            throw factory.apply("collection must be have at least " + min + " elements");
        }
        return collection;
    }

    public <S extends Collection<T>, T> S checkMaxSize(S collection, int max, String name) {
        if (collection != null && collection.size() > max) {
            throw factory.apply("collection must be have at most " + max + " elements");
        }
        return collection;
    }

    public <S extends Collection<T>, T> Optional<S> checkMaxSize(Optional<S> collection, int max, String name) {
        if (collection != null && collection.isPresent() && collection.get().size() > max) {
            throw factory.apply("collection must be have at most " + max + " elements");
        }
        return collection;
    }

    public String checkMatchesPattern(String s, String pattern, String name) {
        if (!Pattern.matches(pattern, s)) {
            throw factory.apply(name + " must match this regex pattern: " + pattern);
        }
        return s;
    }

    public Optional<String> checkMatchesPattern(Optional<String> s, String pattern, String name) {
        if (s.isPresent()) {
            return Optional.of(checkMatchesPattern(s.get(), pattern, name));
        } else {
            return s;
        }

    }

}
