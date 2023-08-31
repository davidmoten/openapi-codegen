package org.davidmoten.oa3.codegen.http.service.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityTemplate;
import org.davidmoten.oa3.codegen.http.service.HttpConnection;
import org.davidmoten.oa3.codegen.http.service.Response;
import org.davidmoten.oa3.codegen.util.Util;

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
        
        request.setEntity(new EntityTemplate(bytes.size(), ContentType.parse(contentType),
                contentEncoding.orElse(null), out -> consumer.accept(out)));
    }

    @Override
    public Response response() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpClientResponseHandler<Response> handler = r -> {
                final InputStream in;
                if (r.getEntity() != null) {
                    in = new ByteArrayInputStream(Util.read(r.getEntity().getContent()));
                } else {
                    in = null;
                }
                Map<String, List<String>> map = new HashMap<>();
                for (Header header : r.getHeaders()) {
                    List<String> list = map.get(header.getName());
                    if (list == null) {
                        list = new ArrayList<>();
                        map.put(header.getName(), list);
                    }
                    list.add(header.getValue());
                }
                return new ApacheHttpClientResponse(r.getCode(), in, map);
            };
            return client.execute(request, handler);
        }
    }

    @Override
    public void close() throws IOException {

    }

}
