package org.davidmoten.oa3.codegen.http;

import java.util.Locale;
import java.util.Set;

import com.github.davidmoten.guavamini.Sets;

public final class MediaType {

    private static final Set<String> BINARY_MEDIA_TYPES = Sets.of( //
            "application/octet-stream", //
            "application/pdf", //
            "image/jpeg", //
            "image/gif", //
            "image/png");

    public static boolean isJson(String mediaType) {
        return lc(stripExtras(mediaType)).endsWith("/json") || lc(mediaType).endsWith("+json");
    }
    
    private static String stripExtras(String mediaType) {
        int i= mediaType.indexOf(";");
        if ( i == -1) {
            return mediaType.trim();
        } else {
            return mediaType.substring(0, i).trim();
        }
    }

    public static boolean isMultipartFormData(String mediaType) {
        return lc(stripExtras(mediaType)).equals("multipart/form-data");
    }
    
    public static boolean isWwwFormUrlEncoded(String mediaType) {
        return lc(stripExtras(mediaType)).equals("application/x-www-form-urlencoded");
    }

    public static boolean isText(String mediaType) {
        return lc(mediaType).startsWith("text/");
    }

    public static boolean isOctetStream(String mediaType) {
        return BINARY_MEDIA_TYPES.contains(lc(mediaType));
    }
    
    private static String lc(String s) {
        return s.toLowerCase(Locale.ENGLISH);
    }

}
