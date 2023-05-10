package org.davidmoten.oa3.codegen.http;

@FunctionalInterface
public interface BearerAuthenticator extends Interceptor {

    String token();
    
    default RequestBase intercept(RequestBase r) {
        Headers h = new Headers(r.headers());
        h.put("Authorization", "Bearer " + token());
        return new RequestBase(r.method(), r.url(), h);
    }
    
}
