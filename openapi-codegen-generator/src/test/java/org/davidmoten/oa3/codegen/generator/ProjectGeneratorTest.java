package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;

public class ProjectGeneratorTest {

    @Test
    public void testGenerateClient() throws FileNotFoundException, IOException {
        generate(false, "target/myapi-client.zip");
    }
    
    @Test
    public void testGenerateClientAndService() throws FileNotFoundException, IOException {
        generate(true, "target/myapi-service.zip");
    }

    private static void generate(boolean generateService, String zipFilename) throws IOException, FileNotFoundException {
        long t = System.currentTimeMillis();
        String filename = "../openapi-codegen-maven-plugin-test/src/main/openapi/main.yml";
        File zip = new File(zipFilename);
        zip.delete();
        try (OutputStream out = new FileOutputStream(zip)) {
            ProjectGenerator.generateZipped(filename, "com.demo", "myapi", "1.0", "com.demo.myapi", true, generateService, out, 255, true);
        }
        System.out.println("zipFile length=" + zip.length());
        System.out.println(System.currentTimeMillis() - t + "ms");
    }

}
