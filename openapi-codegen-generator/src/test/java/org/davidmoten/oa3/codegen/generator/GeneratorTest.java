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
//        generate("../openapi-codegen-maven-plugin-test/.openapi-codegen/cache/zuora.yml");
    }
    
    private static void generate(String name) throws MalformedURLException {
        String definition = name;
        Packages packages = new Packages("test");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), x -> x,
                Collections.emptySet(), Collections.emptySet(), false, false, true, Optional.empty(), true);
        new Generator(d).generate();
        ClientServerGenerator g = new ClientServerGenerator(d);
        g.generateClient();
        g.generateServer();
    }

    private static void generateLocal(String name) throws MalformedURLException {
        generate("../openapi-codegen-maven-plugin-test/src/main/openapi/" + name);
    }
    
    public static void main(String[] args) throws MalformedURLException {
        // generate("../../openapi-directory/APIs/amazonaws.com/accessanalyzer/2019-11-01/openapi.yaml");
        //generate("https://raw.githubusercontent.com/Modern-Treasury/modern-treasury-openapi/main/openapi/mt_openapi_spec_v1.yamls");
        generate("../../temp.yaml");
    }

}
