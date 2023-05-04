package org.davidmoten.oa3.codegen.http;

import java.util.Locale;
import java.util.Set;

import com.github.davidmoten.guavamini.Sets;

public final class MediaType {

    private static final Set<String> BINARY_MEDIA_TYPES = Sets.newHashSet( //
            "application/octet-stream", //
            "application/pdf", //
            "image/jpeg", //
            "image/gif", //
            "image/png");

    public static boolean isJson(String mediaType) {
        return lc(mediaType).endsWith("/json") || lc(mediaType).endsWith("+json");
    }

    private static String lc(String s) {
        return s.toLowerCase(Locale.ENGLISH);
    }

    public static boolean isText(String mediaType) {
        return lc(mediaType).startsWith("text/");
    }

    public static boolean isOctetStream(String mediaType) {
        return BINARY_MEDIA_TYPES.contains(lc(mediaType));
    }

}
