package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class DefaultHttpConnection implements HttpConnection {

    final HttpURLConnection con;
    private boolean once = false;
    private Consumer<? super OutputStream> consumer;
    private Optional<String> outputContentType = Optional.empty();

    public DefaultHttpConnection(HttpURLConnection con) {
        this.con = con;
    }

    @Override
    public void header(String key, String value) {
        con.setRequestProperty(key, value);
    }

    @Override
    public void output(Consumer<? super OutputStream> consumer, String contentType, Optional<String> contentEncoding) {
        this.consumer = consumer;
        this.outputContentType = Optional.of(contentType);
    }

    @Override
    public Response response() throws IOException {
        if (consumer != null) {
            con.setRequestProperty("Content-Type", outputContentType.get());
        }
        if (!once) {
            con.setDoInput(true);
        }
        if (consumer != null) {
            con.setDoOutput(true);
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
