package org.davidmoten.oa3.codegen.http;

import java.util.Comparator;
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

    private static int countX(String s) {
        return count(s, 'X');
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
        return matches(this.statusCode, statusCode, this.mediaType, mediaType);
    }

    public static boolean matches(String statusCodePattern, int statusCode, String mediaTypePattern, String mediaType) {
        return (statusCodePattern.equals("default")
                || Pattern.matches(statusCodePattern.replace("X", "\\d"), statusCode + ""))
                && Pattern.matches(mediaTypePattern.replace("*", ".*"), mediaType);
    }

    public static Comparator<ResponseDescriptor> specificity() {
        return (a, b) -> {
            if (!a.statusCode().equals(b.statusCode())) {
                if (a.statusCode.equals("default")) {
                    return 1;
                } else if (b.statusCode().equals("default")) {
                    return -1;
                }
                if (countX(a.statusCode) != countX(b.statusCode)) {
                    return Integer.compare(countX(a.statusCode), countX(b.statusCode));
                }
            }
            // otherwise check mediaType length, longer is more specific (so b is compared
            // to a rather than a to b)
            return Integer.compare(b.mediaType.length(), a.mediaType.length());
        };
    }
}
