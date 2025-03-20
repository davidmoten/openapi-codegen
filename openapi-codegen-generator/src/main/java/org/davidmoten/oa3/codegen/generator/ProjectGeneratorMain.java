package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ProjectGeneratorMain {

    public static void main(String[] args) throws IOException {
        String groupId = System.getProperty("groupId", "com.demo");
        String artifactId = System.getProperty("artifactId", "client");
        String basePackage = System.getProperty("basePackage", "com.demo.api");
        String version = System.getProperty("version", "1.0");
        String file = System.getProperty("file", "openapi.yaml");
        String output = System.getProperty("output");
        int maxClassNameLength = Integer.parseInt(System.getProperty("maxClassNameLength", "255"));
        if (output == null) {
            output = Files.createTempDirectory("client").toFile().getCanonicalPath();
        }
        File out = new File(output);
        out.mkdirs();
        boolean clean = System.getProperty("clean", "false").equals("true");
        if (clean) {
            deleteContents(out);
        }
        ProjectGenerator.generate(file, groupId, artifactId, version, basePackage, true, false, out, maxClassNameLength);
        System.out.println(output);
    }

    private static void deleteContents(File file) {
        if (file == null) {
            return;
        }
        File[] list = file.listFiles();
        if (list != null) {
            for (File f : list) {
                if (f.isDirectory()) {
                    deleteContents(f);
                }
                f.delete();
            }
        }
    }

}
