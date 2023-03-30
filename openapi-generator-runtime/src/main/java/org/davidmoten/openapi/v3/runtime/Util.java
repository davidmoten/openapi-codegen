package org.davidmoten.openapi.v3.runtime;

import java.math.BigInteger;

public final class Util {

    public static String encodeOctets(byte[] a) {
        return new BigInteger(a).toString(16);
    }
    
    public static byte[] decodeOctets(String s) {
        return new BigInteger(s, 16).toByteArray();
    }

}
