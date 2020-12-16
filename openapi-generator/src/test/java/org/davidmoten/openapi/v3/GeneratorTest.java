package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

public final class GeneratorTest {

    @Test
    public void testGenerate() throws IOException {
        String definition = new String(Files.readAllBytes(new File("src/test/resources/openapi.yml").toPath()),
                StandardCharsets.UTF_8);
        Definition d = new Definition(definition, "test.model", "test.client", new File("target/generated-source/java"));
        new Generator(d).generate();
    }

}
