package org.davidmoten.oa3.codegen.generator;

import java.util.Map;
import java.util.Optional;

public final class HttpResponse<T> {

    private final int statusCode;
    private final Map<String, String> headers;
    private final Optional<T> data;

    public HttpResponse(int statusCode, Map<String, String> headers, Optional<T> data) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.data = data;
    }

    public int statusCode() {
        return statusCode;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Optional<T> data() {
        return data;
    }

}
