package org.davidmoten.oa3.codegen.http.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public final class MemoryThenFileOutputStream extends ByteArrayOutputStream {

    private final File file;
    private final int maxSizeInMemory;
    private OutputStream fos;
    private int size;

    public MemoryThenFileOutputStream(File file, int maxSizeInMemory) {
        this.file = file;
        this.maxSizeInMemory = maxSizeInMemory;
    }

    @Override
    public void write(int b) {
        size++;
        if (size <= maxSizeInMemory) {
            super.write(b);
        } else {
            try {
                fos().write(b);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private OutputStream fos() throws FileNotFoundException {
        if (fos == null) {
            fos = new FileOutputStream(file);
        }
        return fos;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        int availableInMemory = maxSizeInMemory - size;
        size += len;
        try {
            if (availableInMemory > 0) {
                int numBytesToMemory = Math.min(availableInMemory, len);
                super.write(b, off, numBytesToMemory);
                int remaining = len - numBytesToMemory;
                if (remaining > 0) {
                    fos.write(b, off + numBytesToMemory, remaining);
                }
            } else {
                fos.write(b, off, len);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
}
