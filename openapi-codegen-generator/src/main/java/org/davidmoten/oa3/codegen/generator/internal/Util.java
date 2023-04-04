package org.davidmoten.oa3.codegen.generator.internal;

import java.util.Set;

import com.github.davidmoten.guavamini.Sets;

public final class Util {

    public static <T> T orElse(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static Set<String> PRIMITIVE_CLASS_NAMES = Sets.newHashSet("int", "double", "float", "long", "boolean",
            "byte", "short");

    public static boolean isPrimitiveFullClassName(String className) {
        return PRIMITIVE_CLASS_NAMES.contains(className);
    }

}
