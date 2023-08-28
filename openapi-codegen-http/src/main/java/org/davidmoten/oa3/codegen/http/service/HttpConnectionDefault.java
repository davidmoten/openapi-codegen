package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public final class HttpConnectionDefault implements HttpConnection {

    final HttpURLConnection con;
    private boolean once = false;

    public HttpConnectionDefault(HttpURLConnection con) {
        this.con = con;
    }

    @Override
    public void header(String key, String value) {
        con.setRequestProperty(key, value);
    }

    @Override
    public OutputStream outputStream() throws IOException {
        con.setDoInput(true);
        con.setDoOutput(true);
        once = true;
        return con.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        con.disconnect();
    }

    @Override
    public Response response() throws IOException {
        if (!once) {
            con.setDoInput(true);
        }
        int statusCode = con.getResponseCode();
        Map<String, List<String>> map = con.getHeaderFields();
        return new DefaultResponse(statusCode, map, this);
    }

}
