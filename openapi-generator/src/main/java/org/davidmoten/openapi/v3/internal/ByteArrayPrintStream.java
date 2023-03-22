package org.davidmoten.openapi.v3.internal;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public final class ByteArrayPrintStream extends PrintStream {

    private ByteArrayOutputStream bytes;

    private ByteArrayPrintStream(OutputStream out) {
        super(out);
    }

    private void setBytes(ByteArrayOutputStream bytes) {
        this.bytes = bytes;
    }

    public static ByteArrayPrintStream create() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ByteArrayPrintStream p = new ByteArrayPrintStream(bytes);
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
