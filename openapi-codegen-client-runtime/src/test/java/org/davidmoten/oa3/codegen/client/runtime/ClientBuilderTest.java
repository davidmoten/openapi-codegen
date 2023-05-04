package org.davidmoten.oa3.codegen.client.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.davidmoten.oa3.codegen.runtime.Config;
import org.junit.jupiter.api.Test;

public class ClientBuilderTest {

    @Test
    public void testBasePathIsTrimmedAndFinalSlashRemoved() {
        ClientBuilder<String> b = new ClientBuilder<>(x -> "hi", Config.builder().build(), " http://thing:123/hello/ ");
        assertEquals("http://thing:123/hello", b.basePath());
    }

}
