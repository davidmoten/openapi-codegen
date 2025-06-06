package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.davidmoten.oa3.codegen.generator.internal.WriterUtil;
import org.davidmoten.oa3.codegen.util.Util;

public final class ProjectGenerator {

    private static final String START_SERVER = "<!-- serverStart -->\n";
    private static final String END_SERVER = "<!-- serverEnd -->\n";

    private ProjectGenerator() {
        // prevent instantiation
    }

    public static void generateZipped(String openapiFilename, String groupId, String artifactId, String version,
            String basePackage, boolean generateClient, boolean generateServer, OutputStream zip, int maxClassNameLength, boolean failOnParseErrors) {
        try {
            File directory = Files.createTempDirectory("openapi-codegen").toFile();
            generate(openapiFilename, groupId, artifactId, version, basePackage, generateClient, generateServer,
                    directory, maxClassNameLength, failOnParseErrors);
            zipDirectory(directory, zip);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void generate(String openapiFilename, String groupId, String artifactId, String version,
            String basePackage, boolean generateClient, boolean generateServer, File directory, int maxClassNameLength, boolean failOnParseErrors) throws IOException {
        File generatedSourceDirectory = new File(directory, "src/main/java");
        generatedSourceDirectory.mkdirs();
        Definition definition = new Definition(openapiFilename, new Packages(basePackage), generatedSourceDirectory,
                x -> x, Collections.emptySet(), Collections.emptySet(), false, false, failOnParseErrors, Optional.empty(), generateServer, true, true, maxClassNameLength);
        Generator g = new Generator(definition);
        g.generate();

        ClientServerGenerator g2 = new ClientServerGenerator(definition);
        if (generateClient) {
            g2.generateClient();
        }
        if (generateServer) {
            g2.generateServer();
        }
        writePom(groupId, artifactId, version, basePackage, generateServer, directory);
    }

    private static void writePom(String groupId, String artifactId, String version, String basePackage,
            boolean generateServer, File directory) throws IOException {
        String generatorVersion = WriterUtil.readVersion();
        try (InputStream in = ProjectGenerator.class.getResourceAsStream("/generated-pom.xml")) {
            String pom = new String(Util.read(in), StandardCharsets.UTF_8) //
                    .replace("${generator.version}", generatorVersion) //
                    .replace("${base.package}", basePackage) //
                    .replace("${groupId}", groupId) //
                    .replace("${artifactId}", artifactId) //
                    .replace("${version}", version);

            if (!generateServer) {
                int i;
                while ((i = pom.indexOf(START_SERVER)) != -1) {
                    int j = pom.indexOf(END_SERVER);
                    if (j == -1) {
                        throw new RuntimeException(END_SERVER + " marker not found");
                    } else if (i > j) {
                        throw new RuntimeException(START_SERVER + " marker not found, check does not have trailing spaces");
                    }
                    int k = i;
                    while (k > 0 && pom.charAt(k - 1) == ' ') {
                        k--;
                    }
                    pom = pom.substring(0, k) + pom.substring(j + END_SERVER.length());
                }
            } else {
                pom = pom //
                        .replaceAll(START_SERVER, "") //
                        .replaceAll(END_SERVER, "");
            }
            File pomFile = new File(directory, "pom.xml");
            Files.write(pomFile.toPath(), pom.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void zipDirectory(File sourceDirectory, OutputStream out) throws IOException {
        try (ZipOutputStream zs = new ZipOutputStream(out)) {
            Path pp = sourceDirectory.toPath();
            try (Stream<Path> paths = Files.walk(pp)) {
                paths.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                    try {
                        zs.putNextEntry(zipEntry);
                        Files.copy(path, zs);
                        zs.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    }
                });
            }
        }
    }

}
