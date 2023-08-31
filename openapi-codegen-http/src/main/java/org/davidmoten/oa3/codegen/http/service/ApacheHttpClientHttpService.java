package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.davidmoten.oa3.codegen.http.HttpMethod;
import org.davidmoten.oa3.codegen.http.service.internal.ApacheHttpClientHttpConnection;

public final class ApacheHttpClientHttpService implements HttpService {
    
    public static final ApacheHttpClientHttpService INSTANCE = new ApacheHttpClientHttpService();

    @Override
    public HttpConnection connection(String url, HttpMethod method, Option... options) throws IOException {
        try {
            HttpUriRequestBase request = new HttpUriRequestBase(method.name(), new URI(url));
            return new ApacheHttpClientHttpConnection(request);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
