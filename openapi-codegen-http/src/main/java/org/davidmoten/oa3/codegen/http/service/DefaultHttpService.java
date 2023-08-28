package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.davidmoten.oa3.codegen.http.HttpMethod;

public final class DefaultHttpService implements HttpService {
    
    public static final DefaultHttpService INSTANCE = new DefaultHttpService();

    @Override
    public HttpConnection connection(String url, HttpMethod method) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(method.name());
        return new DefaultHttpConnection(con);
    }

}
