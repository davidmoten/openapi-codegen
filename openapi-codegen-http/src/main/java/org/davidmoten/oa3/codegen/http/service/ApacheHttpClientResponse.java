package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;

public final class ApacheHttpClientResponse implements Response {

    private final ClassicHttpResponse response;

    public ApacheHttpClientResponse(ClassicHttpResponse response) {
        this.response = response;
    }
    
    @Override
    public int statusCode() {
        return response.getCode();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return response.getEntity().getContent();
    }

    @Override
    public Map<String, List<String>> headers() {
         Map<String, List<String>> map = new HashMap<>();
         for (Header header: response.getHeaders()) {
             List<String> list = map.get(header.getName());
             if (list == null) {
                 list = new ArrayList<>();
                 map.put(header.getName(), list);
             }
             list.add(header.getValue());
         }
         return map;
    }

}
