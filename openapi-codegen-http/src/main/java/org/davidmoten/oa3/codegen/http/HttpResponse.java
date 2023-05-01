package org.davidmoten.oa3.codegen.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class HttpResponse {

    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final Optional<?> data;

    public HttpResponse(int statusCode, Map<String, List<String>> headers, Optional<?> data) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.data = data;
    }

    public int statusCode() {
        return statusCode;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public Optional<?> data() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("HttpResponse [statusCode=");
        b.append(statusCode);
        b.append(", headers=");
        b.append(headers);
        b.append(", data=");
        b.append(data);
        b.append("]");
        return b.toString();
    }
    
    

}
