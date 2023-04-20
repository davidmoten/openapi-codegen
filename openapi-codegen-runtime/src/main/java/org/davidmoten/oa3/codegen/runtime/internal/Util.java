package org.davidmoten.oa3.codegen.runtime.internal;

import java.math.BigInteger;

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

}
