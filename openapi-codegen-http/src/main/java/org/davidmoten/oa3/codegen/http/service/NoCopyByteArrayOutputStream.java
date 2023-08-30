package org.davidmoten.oa3.codegen.http.service;

import java.io.ByteArrayOutputStream;

public final class NoCopyByteArrayOutputStream extends ByteArrayOutputStream {

    public NoCopyByteArrayOutputStream(int size) {
        super(size);
    }

    public byte[] buffer() {
        return buf;
    }

}
