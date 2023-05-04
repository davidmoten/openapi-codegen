package org.davidmoten.oa3.codegen.http;

@FunctionalInterface
public interface Interceptor {
    
    RequestBase intercept(RequestBase u);

}
