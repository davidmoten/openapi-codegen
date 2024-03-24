package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ClientServerGeneratorTest {

    @Test
    public void test() throws MalformedURLException {
        String definition = new File("../openapi-codegen-maven-plugin-test/src/main/openapi/paths.yml").toURI().toURL()
                .toExternalForm();
        generate(definition);
    }
    
    @Test
    public void testCached() throws MalformedURLException {
        File f = new File("../openapi-codegen-maven-plugin-test/.openapi-codegen/cache/stripe.yml");
        if (f.exists()) {
            String definition = f.toURI().toURL().toExternalForm();
            generate(definition);
        }
    }

    private void generate(String definition) {
        Packages packages = new Packages("test");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), x -> x,
                Collections.emptySet(), Collections.emptySet(), false, false, true, Optional.empty(), true, true, true);
        ClientServerGenerator generator = new ClientServerGenerator(d);
        generator.generateServer();
        generator.generateClient();
    }

}
