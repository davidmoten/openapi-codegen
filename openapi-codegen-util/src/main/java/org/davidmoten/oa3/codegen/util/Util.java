package org.davidmoten.oa3.codegen.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.openapitools.jackson.nullable.JsonNullable;

import com.github.davidmoten.guavamini.Preconditions;

public final class Util {

    public static String encodeOctets(byte[] a) {
        if (a == null) {
            return "";
        } else {
            return new BigInteger(a).toString(16);
        }
    }
    
    public static Optional<String> encodeOctets(Optional<byte[]> a) {
        return a.map(Util::encodeOctets);
    }
    
    @SuppressWarnings("unchecked")
    public static JsonNullable<String> encodeOctets(JsonNullable<byte[]> a) {
        if (a.isPresent() && a.get() != null) {
            return JsonNullable.of(encodeOctets(a.get()));
        } else {
            return (JsonNullable<String>) (JsonNullable<?>) a;
        }
    }

    public static byte[] decodeOctets(String s) {
        return new BigInteger(s, 16).toByteArray();
    }
    
    public static Optional<byte[]> decodeOctets(Optional<String> s) {
        return s.map(Util::decodeOctets);
    }
    
    @SuppressWarnings("unchecked")
    public static JsonNullable<byte[]> decodeOctets(JsonNullable<String> a) {
        if (a.isPresent() && a.get() != null) {
            return JsonNullable.of(decodeOctets(a.get()));
        } else {
            return (JsonNullable<byte[]>) (JsonNullable<?>) a;
        }
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
    
    public static <K, V> Map<K, V> createMapIfNull(Map<K, V> map) {
        return map == null ? new HashMap<>() : map;
    }

    public static <T> T nvl(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
    
    public static <T> T nvl(T value, Supplier<? extends T> defaultValue) {
        if (value == null) {
            return defaultValue.get();
        } else {
            return value;
        }
    }

}
