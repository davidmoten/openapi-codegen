package org.davidmoten.oa3.codegen.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

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
        System.out.println("deserializing " + contentType + " to " + cls.getSimpleName());
        try {
            if (MediaType.isJson(contentType)) {
                return m.readValue(in, cls);
            } else if (cls.equals(String.class) && MediaType.isText(contentType)) {
                try (InputStream is = in) {
                    return (T) new String(Util.read(is), StandardCharsets.UTF_8);
                }
            } else if (cls.equals(byte[].class) && MediaType.isOctetStream(contentType)) {
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

}
