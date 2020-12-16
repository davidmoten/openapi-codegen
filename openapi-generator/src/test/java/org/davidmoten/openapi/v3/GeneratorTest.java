package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.Test;

public final class GeneratorTest {

    @Test
    public void testGenerate() throws IOException {
        String definition = new String(Files.readAllBytes(new File("src/test/resources/openapi.yml").toPath()),
                StandardCharsets.UTF_8);
        Packages packages = new Packages("test.model", "test.client");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), Collections.emptyMap());
        new Generator(d).generate();
    }

}
