package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.internal.EnhancedOpenAPIV3Parser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class SchemaSearch {

    public static OpenAPI parse(String url) {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        // github api goes over snake yaml parser max code points for 3.0
        System.setProperty("maxYamlCodePoints", "999999999");
        OpenAPIV3Parser parser = new EnhancedOpenAPIV3Parser();
        SwaggerParseResult result = parser.readLocation(url, null, options);
        String errors = result.getMessages().stream().collect(Collectors.joining("\n"));
        if (!errors.isEmpty()) {
            throw new RuntimeException(errors);
        }
        return result.getOpenAPI();
    }

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        File f = new File("/home/dave/workspace/openapi-directory");
        List<File> files = new ArrayList<>();
        findFiles(f, files);
        System.out.println(files.size() + " definitions found");
        int erroredFiles = 0;
        AtomicLong found = new AtomicLong();
        Set<File> foundFiles = new HashSet<>(); 
        int fileCount = 0;
        for (File file : files) {
            String uri = file.toPath().toUri().toString();
            try {
                OpenAPI a = parse(uri);
                Paths paths = a.getPaths();
                if (paths != null) {
                    paths.forEach((p, item) -> {
                        if (emptyIfNull(item.getParameters()).size() == 0) {
                            emptyIfNull(item.readOperations()) //
                                    .forEach(op -> {
                                        if (emptyIfNull(op.getParameters()).size() == 0
                                                && op.getRequestBody() == null) {
                                            found.incrementAndGet();
                                            foundFiles.add(file);
                                            System.out.println(file);
                                            System.out.println("    " + p);
                                            System.out.println();
                                        }
                                    });
                        }
                        ;
                    });
                }
            } catch (RuntimeException e) {
                erroredFiles++;
            }
            if (++fileCount % 10 == 0) {
                System.out.println("processed " + fileCount + " files");
            }
        }
        System.out.println("errored files count=" + erroredFiles);
        System.out.println("found=" + found.get() + ", foundFiles=" + foundFiles.size());
        System.out.println("time=" + (System.currentTimeMillis() - time) + "ms");
    }

    private static <T> List<T> emptyIfNull(List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        } else {
            return list;
        }
    }

    private static void findFiles(File f, List<File> files) {
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                findFiles(file, files);
            }
        } else if (f.getName().equals("openapi.yaml")) {
            files.add(f);
        }
    }

}
