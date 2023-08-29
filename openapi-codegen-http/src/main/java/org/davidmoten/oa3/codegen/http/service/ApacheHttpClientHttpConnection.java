package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

public class ApacheHttpClientHttpConnection implements HttpConnection {

    private final HttpUriRequestBase request;

    public ApacheHttpClientHttpConnection(HttpUriRequestBase request) {
        this.request = request;
    }

    @Override
    public void header(String key, String value) {
        request.addHeader(key, value);
    }

    @Override
    public Response response() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void output(Consumer<? super OutputStream> consumer) {
        // TODO Auto-generated method stub
        
    }

}
