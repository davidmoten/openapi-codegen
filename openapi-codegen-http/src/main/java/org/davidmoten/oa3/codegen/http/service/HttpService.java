package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;

import org.davidmoten.oa3.codegen.http.HttpMethod;

public interface HttpService {
    HttpConnection connection(String url, HttpMethod method) throws IOException;
}