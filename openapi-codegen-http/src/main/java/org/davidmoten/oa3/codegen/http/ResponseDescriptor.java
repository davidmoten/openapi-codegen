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
            return 3;
        } else if (statusCode.contains("X") || mediaType.contains("*")) {
            return 2;
        } else {
            return 1;
        }
    }

    public boolean matches(int statusCode, String mediaType) {
        return (this.statusCode.equals("default")
                || Pattern.matches(this.statusCode.replace("X", "\\d"), statusCode + ""))
                && Pattern.matches(this.mediaType.replace("*", ".*"), mediaType);
    }
}
