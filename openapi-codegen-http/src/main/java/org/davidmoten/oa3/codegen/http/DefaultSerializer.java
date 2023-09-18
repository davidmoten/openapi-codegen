package org.davidmoten.oa3.codegen.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultSerializer implements Serializer {

    private final ObjectMapper m;

    public DefaultSerializer(ObjectMapper m) {
        this.m = m;
    }

    @Override
    public void serialize(Object o, String contentType, OutputStream out) {
        try {
            if (MediaType.isJson(contentType)) {
                m.writeValue(out, o);
            } else if (MediaType.isText(contentType)) {
                if (o != null) {
                    out.write(String.valueOf(o).getBytes(StandardCharsets.UTF_8));
                }
            } else if (o instanceof byte[] && MediaType.isOctetStream(contentType)) {
                out.write((byte[]) o);
            } else {
                throw new RuntimeException(
                        "unsupported serialization of " + o.getClass() + " with contentType=" + contentType);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(Class<T> cls, String contentType, InputStream in) {
        try {
            if (MediaType.isJson(contentType)) {
                if (cls.equals(byte[].class)) {
                    return (T) m.readTree(in);
                } else {
                    return m.readValue(in, cls);
                }
            } else if (cls.equals(String.class) && MediaType.isText(contentType)) {
                try (InputStream is = in) {
                    return (T) new String(Util.read(is), StandardCharsets.UTF_8);
                }
            } else if (MediaType.isOctetStream(contentType)) {
                try (InputStream is = in) {
                    return (T) Util.read(is);
                }
            } else {
                throw new RuntimeException(
                        "unsupported deserialization to " + cls + " with contentType=" + contentType);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Map<String, Object> properties(Object o, String contentType) {
        if (MediaType.isJson(contentType)) {
            try {
                return m.valueToTree(o).properties().stream()
                        .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
            } catch (IllegalArgumentException e) {
                return Collections.emptyMap();
            }
        } else {
            return Collections.emptyMap();
        }
    }

}
