package org.davidmoten.oa3.codegen.generator.internal;

public final class Util {
    
    public static <T> T orElse(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
