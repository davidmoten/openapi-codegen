package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ClientServerGeneratorTest {

    @Test
    public void test() throws MalformedURLException {
        String definition = new File("../openapi-codegen-maven-plugin-test/src/main/openapi/library.yml").toURI().toURL()
                .toExternalForm();
        Packages packages = new Packages("test");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), x -> x,
                Collections.emptySet(), Collections.emptySet(), false, true, Optional.empty());
        ClientServerGenerator generator = new ClientServerGenerator(d);
        generator.generateServer();
        generator.generateClient();
    }

}
