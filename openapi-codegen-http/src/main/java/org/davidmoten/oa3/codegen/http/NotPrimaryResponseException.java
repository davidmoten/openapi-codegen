package org.davidmoten.oa3.codegen.http;

@SuppressWarnings("serial")
public final class NotPrimaryResponseException extends RuntimeException {
    
    private final HttpResponse response;

    public NotPrimaryResponseException(HttpResponse response) {
        super();
        this.response = response;
    }
    
    public HttpResponse response() {
        return response;
    }

}
