package org.davidmoten.oa3.codegen.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static byte[] read(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        int n = 0;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while ((n = in.read(buffer)) != -1) {
            bytes.write(buffer, 0, n);
        }
        return bytes.toByteArray();
    }

}
