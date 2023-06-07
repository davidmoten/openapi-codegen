package org.davidmoten.oa3.codegen.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Utf8ByteArrayOutputStream extends ByteArrayOutputStream {
    
    public void write(String s) throws IOException {
        write(s.getBytes(StandardCharsets.UTF_8));
    }

    public void crLf() throws IOException {
        write("\r\n");
    }

}
