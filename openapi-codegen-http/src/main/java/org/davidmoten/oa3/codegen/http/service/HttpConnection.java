package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Expected sequence of calls is (BNF):
 * 
 * <pre>
 * header()* [outputStream()] [response()] [close()]
 * </pre>
 */
public interface HttpConnection {

    void header(String key, String value);

    /**
     * When a consumer is defined (when this method is called), this class is
     * expected to set the Content-Type and Content-Length fields (and support
     * chunking if requested).
     * 
     * @param consumer action to be performed on the OutputStream
     * @param contentType Content-Type header value
     * @param contentEncoding if present is appended to the Content-Type value
     */
    void output(Consumer<? super OutputStream> consumer, String contentType, Optional<String> contentEncoding,
            boolean chunked);

    Response response() throws IOException;

    void close() throws IOException;

}