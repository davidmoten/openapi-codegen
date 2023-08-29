package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.OutputStream;
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

    void output(Consumer<? super OutputStream> consumer);
    
    Response response() throws IOException;
    
    void close() throws IOException;
    
}