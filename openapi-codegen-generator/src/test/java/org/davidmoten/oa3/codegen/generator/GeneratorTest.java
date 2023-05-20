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
        generateLocal("paths.yml");
    }

    @Test
    public void testGenerateMain() throws IOException {
        generateLocal("main.yml");
    }
    
    @Test
    public void testGenerateLibraryApi() throws IOException {
        generateLocal("library.yml");
    }
    
    @Test
    public void testGenerateOpenFlow() throws IOException {
        generateLocal("openflow.yml");
    }
    
    @Test
    public void testSmall() throws MalformedURLException {
        generateLocal("small.yml");
//        generate("../../openapi-directory/APIs/amazonaws.com/s3/2006-03-01/openapi.yaml");
    }

    private static void generate(String name) throws MalformedURLException {
        String definition = new File(name).toURI().toURL()
                .toExternalForm();
        Packages packages = new Packages("test");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), x -> x,
                Collections.emptySet(), Collections.emptySet(), false, false, true, Optional.empty());
        new Generator(d).generate();
    }

    private static void generateLocal(String name) throws MalformedURLException {
        generate("../openapi-codegen-maven-plugin-test/src/main/openapi/" + name);
    }

}
