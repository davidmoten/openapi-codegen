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
    
    private Consumer<? super OutputStream> consumer;
    private Optional<String> outputContentType = Optional.empty();
    private Optional<String> outputContentEncoding = Optional.empty();

    public DefaultHttpConnection(HttpURLConnection con) {
        this.con = con;
    }

    @Override
    public void header(String key, String value) {
        con.setRequestProperty(key, value);
    }

    @Override
    public void output(Consumer<? super OutputStream> consumer, String contentType, Optional<String> contentEncoding,
            boolean chunked) {
        this.consumer = consumer;
        this.outputContentType = Optional.of(contentType);
        this.outputContentEncoding = contentEncoding;
    }

    @Override
    public Response response() throws IOException {
        con.setDoInput(true);
        if (consumer != null) {
            con.setRequestProperty("Content-Type", outputContentType.get());
            if (outputContentEncoding.isPresent()) {
                con.setRequestProperty("Content-Encoding", outputContentEncoding.get());
            }

            // buffer request to measure length
            NoCopyByteArrayOutputStream bytes = new NoCopyByteArrayOutputStream(256);
            consumer.accept(bytes);

            con.setRequestProperty("Content-Length", String.valueOf(bytes.size()));
            con.setDoOutput(true);
            try (OutputStream out = con.getOutputStream()) {
                out.write(bytes.buffer(), 0, bytes.size());
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
