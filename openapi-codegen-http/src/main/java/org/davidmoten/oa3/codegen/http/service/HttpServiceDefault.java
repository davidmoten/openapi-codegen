package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.davidmoten.oa3.codegen.http.HttpMethod;

public class HttpServiceDefault implements HttpService {
    
    public static HttpServiceDefault INSTANCE = new HttpServiceDefault();

    @Override
    public HttpConnection connection(String url, HttpMethod method) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        return new HttpConnectionDefault(con);
    }

}
