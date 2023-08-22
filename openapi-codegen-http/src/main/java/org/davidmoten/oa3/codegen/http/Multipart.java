package org.davidmoten.oa3.codegen.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class Multipart {
    
    private Multipart() {
        // prevent instantiation
    }

    public static String randomBoundary() {
        return nextId();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final List<FormEntry> formEntries = new ArrayList<>();

        public Builder addFormEntry(String key, byte[] value, Optional<String> filename, Optional<String> contentType) {
            formEntries.add(new FormEntry(key, value, filename, contentType));
            return this;
        }

        public Builder addFormEntry(String key, String value, Optional<String> filename, Optional<String> contentType) {
            return addFormEntry(key, value.getBytes(StandardCharsets.UTF_8), filename, contentType);
        }
        
        /**
         * Includes leading CRLF for http content.
         * 
         * @return multipart content bytes
         */
        public byte[] multipartContent(String boundary) {
            try (BytesOutputStream w = new BytesOutputStream()) {
                for (FormEntry entry : formEntries) {
                    w.write("--");
                    w.write(boundary);
                    w.eol();
                    String fname = entry.filename.map(x -> "; filename=\"" + x + "\"").orElse("");
                    w.write("Content-Disposition: form-data; name=\"" + entry.key + "\"" + fname);
                    w.eol();
                    if (entry.contentType.isPresent()) {
                        w.write("Content-Type: " + entry.contentType.get());
                        w.eol();
                    }
                    w.eol();
                    w.write(entry.value);
                    w.eol();
                }
                w.write("--");
                w.write(boundary);
                w.write("--");
                w.flush();
                return w.toByteArray();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

    }

    private static final class FormEntry {
        final String key;
        final byte[] value;
        final Optional<String> filename;
        final Optional<String> contentType;

        FormEntry(String key, byte[] value, Optional<String> filename, Optional<String> contentType) {
            this.key = key;
            this.value = value;
            this.filename = filename;
            this.contentType = contentType;
        }
    }

    private static final class BytesOutputStream extends ByteArrayOutputStream {

        void write(String s) throws IOException {
            write(s.getBytes(StandardCharsets.UTF_8));
        }

        void eol() throws IOException {
            write("\r\n");
        }
    }

    private static final Random RANDOM = new Random();
    private static final String ID_CHARS = "abcdefghijkmnopqrstuvwxyz23456789"; // remove 1, l, 0 because hard to
                                                                                // distinguish
    private static final int ID_LENGTH = 16;

    /**
     * Returns a new random id that is guaranteed unique given the expected number
     * of calls for use multipart boundaries.
     *
     * <p>
     * Number of id permutations is ID_CHARS.length()<sup>ID_LENGTH</sup>.
     *
     * <p>
     * Probability of collision after k calls to this method and given n
     * permutations is approximately 1 - e<sup>-k<sup>2</sup>/2n.
     *
     * <p>
     * For k = 1 million, 33 characters, id length 16, probability of collision is 2
     * x 10<sup>-13</sup>. That is one in 20 million million chance.
     *
     * @return next unique identifier (for egc broadcast id )
     */
    private static String nextId() {
        char[] a = new char[ID_LENGTH];
        for (int i = 0; i < a.length; i++) {
            a[i] = ID_CHARS.charAt(RANDOM.nextInt(Integer.MAX_VALUE) % ID_CHARS.length());
        }
        return new String(a);
    }

}
