package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class DefaultHttpConnection implements HttpConnection {

    final HttpURLConnection con;
    private boolean once = false;
    private Consumer<? super OutputStream> consumer;

    public DefaultHttpConnection(HttpURLConnection con) {
        this.con = con;
    }

    @Override
    public void header(String key, String value) {
        con.setRequestProperty(key, value);
    }

    @Override
    public void output(Consumer<? super OutputStream> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Response response() throws IOException {
        if (!once) {
            con.setDoInput(true);
        }
        if (consumer != null) {
            try (OutputStream out = con.getOutputStream()) {
                consumer.accept(out);
            }
        }
        int statusCode = con.getResponseCode();
        Map<String, List<String>> map = con.getHeaderFields();
        return new DefaultResponse(statusCode, map, this);
    }

    @Override
    public void close() throws IOException {
        con.disconnect();
    }
}
