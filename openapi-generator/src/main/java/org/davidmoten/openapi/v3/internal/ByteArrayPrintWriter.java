package org.davidmoten.openapi.v3.internal;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public final class ByteArrayPrintWriter extends PrintWriter {

    private ByteArrayOutputStream bytes;

    private ByteArrayPrintWriter(OutputStream out) {
        super(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    private void setBytes(ByteArrayOutputStream bytes) {
        this.bytes = bytes;
    }

    public static ByteArrayPrintWriter create() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ByteArrayPrintWriter p = new ByteArrayPrintWriter(bytes);
        p.setBytes(bytes);
        return p;
    }

    public String text() {
        this.flush();
        try {
            return bytes.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
