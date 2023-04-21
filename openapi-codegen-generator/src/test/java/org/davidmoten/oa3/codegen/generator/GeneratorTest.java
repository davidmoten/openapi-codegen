package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

public final class GeneratorTest {

    @Test
    public void testGenerate() throws IOException {
        String definition = new File("../openapi-codegen-maven-plugin-test/src/main/openapi/paths.yml").toURI().toURL().toExternalForm();
        Packages packages = new Packages("test");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), x -> x,
                Collections.emptySet(), Collections.emptySet(), false, true);
        new Generator(d).generate();
    }

}
