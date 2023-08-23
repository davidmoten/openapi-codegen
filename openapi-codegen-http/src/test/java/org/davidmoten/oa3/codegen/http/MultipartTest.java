package org.davidmoten.oa3.codegen.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.davidmoten.oa3.codegen.http.Multipart.Builder;
import org.junit.jupiter.api.Test;

public class MultipartTest {

    @Test
    public void testMultipartBuilder() {
        Builder b = Multipart.builder();
        b.addFormEntry("key1", "value1", Optional.empty(), Optional.empty());
        b.addFormEntry("key2", "value2", Optional.empty(), Optional.empty());
        byte[] bytes = b.multipartContent("e0a1a4eb64a40b05");
        assertEquals(170, bytes.length);
        String output = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(
                "--e0a1a4eb64a40b05\r\n" //
                + "Content-Disposition: form-data; name=\"key1\"\r\n" //
                + "\r\n" //
                + "value1\r\n" //
                + "--e0a1a4eb64a40b05\r\n" //
                + "Content-Disposition: form-data; name=\"key2\"\r\n" //
                + "\r\n" //
                + "value2\r\n" //
                + "--e0a1a4eb64a40b05--", output);
    }

}
