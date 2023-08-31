package org.davidmoten.oa3.codegen.http.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import org.davidmoten.oa3.codegen.http.service.Response;

public final class DefaultResponse implements Response {

    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final DefaultHttpConnection connection;

    public DefaultResponse(int statusCode, Map<String, List<String>> headers, DefaultHttpConnection connection) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.connection = connection;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public InputStream inputStream() throws IOException {
        HttpURLConnection con = connection.con;
        return statusCode < 400 ? con.getInputStream() : con.getErrorStream();
    }

    @Override
    public Map<String, List<String>> headers() {
        return headers;
    }

}
