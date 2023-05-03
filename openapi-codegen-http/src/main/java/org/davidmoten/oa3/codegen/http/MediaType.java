package org.davidmoten.oa3.codegen.http;

public final class MediaType {

    public static boolean isJson(String mediaType) {
        return mediaType.endsWith("/json") || mediaType.endsWith("+json");
    }

    public static boolean isText(String mediaType) {
        return mediaType.startsWith("text/");
    }

    public static boolean isOctetStream(String mediaType) {
        return mediaType.equals("application/octet-stream");
    }

}
