package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;


public final class GeneratorTest {

    @Test
    public void testGeneratePaths() throws IOException {
        generate("paths.yml");
    }

    @Test
    public void testGenerateMain() throws IOException {
        generate("main.yml");
    }
    
    @Test
    public void testGenerateLibraryApi() throws IOException {
        generate("library.yml");
    }
    
    @Test
    public void testGenerateOpenFlow() throws IOException {
        generate("openflow.yml");
    }
    
    @Test
    public void testSmall() throws MalformedURLException {
        generate("bitbucket.yml");
    }

    private static void generate(String name) throws MalformedURLException {
        String definition = new File("../openapi-codegen-maven-plugin-test/src/main/openapi/" + name).toURI().toURL()
                .toExternalForm();
        Packages packages = new Packages("test");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), x -> x,
                Collections.emptySet(), Collections.emptySet(), false, true, Optional.empty());
        new Generator(d).generate();
    }

}
