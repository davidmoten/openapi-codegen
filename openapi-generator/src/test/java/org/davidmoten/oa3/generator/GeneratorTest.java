package org.davidmoten.oa3.generator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.davidmoten.oa3.generator.Definition;
import org.davidmoten.oa3.generator.Generator;
import org.davidmoten.oa3.generator.Packages;
import org.junit.Test;

public final class GeneratorTest {

    @Test
    public void testGenerate() throws IOException {
        String definition = new String(Files.readAllBytes(new File("../openapi-generator-maven-plugin-test/src/main/openapi/openapi.yml").toPath()),
                StandardCharsets.UTF_8);
        Packages packages = new Packages("test");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), x -> x);
        new Generator(d).generate();
    }
    
}