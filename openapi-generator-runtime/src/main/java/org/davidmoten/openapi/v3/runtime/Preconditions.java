package org.davidmoten.openapi.v3.runtime;

public final class Preconditions {

    public static <T> T checkNotNull(T t, String parameterName) {
        if (t == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
        return t;
    }
}
