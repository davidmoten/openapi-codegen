package org.davidmoten.oa3.codegen.http;

/**
 * Indicates that a response other than the primary response was received and
 * the actual response is available from this exception using the {@link #response()} method.
 */
@SuppressWarnings("serial")
public final class NotPrimaryResponseException extends RuntimeException {

    private final HttpResponse response;

    public NotPrimaryResponseException(HttpResponse response) {
        super(response.statusCode() + ", headers=" + response.headers());
        this.response = response;
    }

    public HttpResponse response() {
        return response;
    }

}
