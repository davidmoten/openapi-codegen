package org.davidmoten.oa3.codegen.http;

public final class NotPrimaryResponseException extends RuntimeException {
    
    private static final long serialVersionUID = -4609453679904943321L;
    
    private final HttpResponse response;

    public NotPrimaryResponseException(HttpResponse response) {
        super();
        this.response = response;
    }
    
    public HttpResponse response() {
        return response;
    }

}
