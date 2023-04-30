package org.davidmoten.oa3.codegen.http;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

    void serialize(Object o, String contentType, OutputStream out);

    <T> T deserialize(Class<T> cls, String contentType, InputStream in);

}
