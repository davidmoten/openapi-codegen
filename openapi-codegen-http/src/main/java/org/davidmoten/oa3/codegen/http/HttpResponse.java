package org.davidmoten.oa3.codegen.http;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class HttpResponse {

    private final int statusCode;
    private final Headers headers;
    private final Optional<?> data;

    public HttpResponse(int statusCode, Headers headers, Optional<?> data) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.data = data;
    }

    public int statusCode() {
        return statusCode;
    }

    public Headers headers() {
        return headers;
    }

    public Optional<?> data() {
        return data;
    }

    public HttpResponse assertStatusCodeMatches(String expectedStatusCode) {
        if (!ResponseDescriptor.matchesStatusCode(expectedStatusCode, statusCode)) {
            throw new NotPrimaryResponseException(this);
        }
        return this;
    }

    public HttpResponse assertContentTypeMatches(String expectedContentType) {
        Optional<List<String>> header = headers.get("Content-Type");
        String contentType = header.orElse(Collections.emptyList()).stream().findFirst()
                .orElse("");
        if (!ResponseDescriptor.matchesMediaType(expectedContentType, contentType)) {
            throw new NotPrimaryResponseException(this);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T dataUnwrapped() {
        return (T) data.orElse(null);
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
