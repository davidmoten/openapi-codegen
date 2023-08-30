package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityTemplate;

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
    public void output(Consumer<? super OutputStream> consumer, String contentType, Optional<String> contentEncoding,
            boolean chunked) {
        NoCopyByteArrayOutputStream bytes = new NoCopyByteArrayOutputStream(256);
        consumer.accept(bytes);
        request.setEntity(new EntityTemplate(bytes.size(), ContentType.create(contentType),
                contentEncoding.orElse(null), out -> consumer.accept(out)));
    }

    @Override
    public Response response() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpClientResponseHandler<ClassicHttpResponse> handler = r -> r;
            ClassicHttpResponse r = client.execute(request, handler);
            return new ApacheHttpClientResponse(r);
        }
    }

    @Override
    public void close() throws IOException {

    }

}
