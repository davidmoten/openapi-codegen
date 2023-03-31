package org.davidmoten.openapi.v3.runtime;

import java.math.BigInteger;

public final class Util {

    public static String encodeOctets(byte[] a) {
        return new BigInteger(a).toString(16);
    }

    public static byte[] decodeOctets(String s) {
        return new BigInteger(s, 16).toByteArray();
    }

    public static String toPrimitive(String canonicalClassName) {
        if (canonicalClassName.equals(Integer.class.getCanonicalName())) {
            return "int";
        } else if (canonicalClassName.equals(Short.class.getCanonicalName())) {
            return "short";
        } else if (canonicalClassName.equals(Long.class.getCanonicalName())) {
            return "long";
        } else if (canonicalClassName.equals(Float.class.getCanonicalName())) {
            return "float";
        } else if (canonicalClassName.equals(Double.class.getCanonicalName())) {
            return "double";
        } else if (canonicalClassName.equals(Boolean.class.getCanonicalName())) {
            return "boolean";
        } else if (canonicalClassName.equals(Byte.class.getCanonicalName())) {
            return "byte";
        }  else {
            return canonicalClassName;
        }
    }

    public static Class<?> toPrimitive(Class<?> c) {
        if (c.equals(Integer.class)) {
            return int.class;
        } else if (c.equals(Long.class)) {
            return long.class;
        } else if (c.equals(Float.class)) {
            return float.class;
        } else if (c.equals(Boolean.class)) {
            return boolean.class;
        } else if (c.equals(Short.class)) {
            return short.class;
        } else if (c.equals(Byte.class)) {
            return byte.class;
        } else if (c.equals(BigInteger.class)) {
            return c;
        } else {
            return c;
        }
    }

}
