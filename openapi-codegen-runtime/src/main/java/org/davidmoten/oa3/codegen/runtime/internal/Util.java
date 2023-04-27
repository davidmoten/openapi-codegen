package org.davidmoten.oa3.codegen.runtime.internal;

import java.math.BigInteger;

import com.github.davidmoten.guavamini.Preconditions;

public final class Util {

    public static String encodeOctets(byte[] a) {
        return new BigInteger(a).toString(16);
    }

    public static byte[] decodeOctets(String s) {
        return new BigInteger(s, 16).toByteArray();
    }

    public static <T> T orElse(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static String toString(Class<?> cls, Object... items) {
        Preconditions.checkArgument(items.length % 2 == 0);
        StringBuilder s = new StringBuilder();
        int i = 0;
        while (i < items.length) {
            if (i > 0) {
                s.append(", ");
            }
            s.append(items[i]);
            s.append("=");
            s.append(items[i + 1]);
            i += 2;
        }
        return cls.getSimpleName() + "[" + s + "]";
    }

}
