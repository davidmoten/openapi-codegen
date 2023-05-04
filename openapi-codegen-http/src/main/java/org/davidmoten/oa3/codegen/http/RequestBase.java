package org.davidmoten.oa3.codegen.http;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * The components of an HTTP request not including the request body
 *
 */
public final class RequestBase {

    private final HttpMethod method;
    private final String url;
    private final Headers headers;

    public RequestBase(HttpMethod method, String url, Headers headers) {
        this.method = Preconditions.checkNotNull(method);
        this.url = Preconditions.checkNotNull(url);
        this.headers = Preconditions.checkNotNull(headers);
    }

    public HttpMethod method() {
        return method;
    }

    public String url() {
        return url;
    }

    public Headers headers() {
        return headers;
    }

}
