package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.Test;

public final class GeneratorTest {

    @Test
    public void testGenerate() throws IOException {
        String definition = new String(
                Files.readAllBytes(
                        new File("../openapi-codegen-maven-plugin-test/src/main/openapi/openapi.yml").toPath()),
                StandardCharsets.UTF_8);
        Packages packages = new Packages("test");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), x -> x,
                Collections.emptySet(), Collections.emptySet());
        new Generator(d).generate();
    }

}
