package org.davidmoten.oa3.codegen.http;

import java.util.regex.Pattern;

public final class ResponseDescriptor {
    private final String statusCode; // can be a pattern like `2*`
    private final String mediaType;
    private final Class<?> cls;

    public ResponseDescriptor(String statusCode, String mediaType, Class<?> cls) {
        this.statusCode = statusCode;
        this.mediaType = mediaType;
        this.cls = cls;
    }

    public String statusCode() {
        return statusCode;
    }

    public String mediaType() {
        return mediaType;
    }

    public Class<?> cls() {
        return cls;
    }

    public int specificity() {
        if (statusCode.equals("default")) {
            return Integer.MAX_VALUE;
        } else {
            return 10000 * count(statusCode, 'X') + 1000 * count(mediaType, '*') + (1000 - mediaType.length());
        }
    }

    private static int count(String s, char ch) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    public boolean matches(int statusCode, String mediaType) {
        return (this.statusCode.equals("default")
                || Pattern.matches(this.statusCode.replace("X", "\\d"), statusCode + ""))
                && Pattern.matches(this.mediaType.replace("*", ".*"), mediaType);
    }
}
