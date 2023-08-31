package org.davidmoten.oa3.codegen.http.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.davidmoten.oa3.codegen.http.service.Response;

public final class ApacheHttpClientResponse implements Response {

    private final int statusCode;
    private final InputStream in;
    private final Map<String, List<String>> headers;

    public ApacheHttpClientResponse(int statusCode, InputStream in , Map<String, List<String>> headers) {
        this.statusCode = statusCode;
        this.in = in;
        this.headers = headers;
    }
    
    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public InputStream inputStream() throws IOException {
        return in;
    }

    @Override
    public Map<String, List<String>> headers() {
        return headers;
    }

}
