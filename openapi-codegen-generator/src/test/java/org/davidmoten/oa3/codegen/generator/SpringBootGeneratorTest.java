package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;

import org.junit.Test;

public class SpringBootGeneratorTest {
    
    @Test
    public void test() throws MalformedURLException {
        String definition = new File("../openapi-codegen-maven-plugin-test/src/main/openapi/ebay.yml").toURI().toURL().toExternalForm();
        Packages packages = new Packages("test");
        Definition d = new Definition(definition, packages, new File("target/generated-source/java"), x -> x,
                Collections.emptySet(), Collections.emptySet(), false, true);
        new SpringBootGenerator(d).generate();
    }

}
