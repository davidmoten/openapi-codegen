package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.common.io.Files;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Generator {

    private static final String IMPORTS_TOKEN = "<<IMPORTS>>";
    private final Definition definition;

    public Generator(Definition definition) {
        this.definition = definition;
    }

    public void generate() {
        SwaggerParseResult result = new OpenAPIParser().readContents(definition.definition(), null, null);
        result.getMessages().stream().forEach(System.out::println);
        OpenAPI api = result.getOpenAPI();
        System.out.println(api);

        // Names object for each Packages object
        Names names = new Names(definition);

        // generate methods on singleton client object in client package
        writeClientClass(api, names);

        // generate model classes for schema definitions
        writeSchemaClasses(api, names);
    }

    private static void writeClientClass(OpenAPI api, Names names) {
        String className = names.clientClassName();
        File file = names.clientClassJavaFile();
        write(file, className, (indent, imports, p) -> {
            p.format("%s// TODO\n", indent);
        });
    }

    private static void writeSchemaClasses(OpenAPI api, Names names) {
        @SuppressWarnings("unchecked")
        Map<String, Schema<?>> schemas = (Map<String, Schema<?>>) (Map<String, ?>) api.getComponents().getSchemas();
        for (String schemaName : schemas.keySet()) {
            String className = names.schemaNameToClassName(schemaName);
            File file = names.schemaNameToJavaFile(schemaName);
            write(file, className, (indent, imports, p) -> {
                p.format("%s// TODO\n", indent);
            });
        }
    }

    private static void write(File file, String className, JavaClassWriter writer) {
        file.getParentFile().mkdirs();
        Imports imports = new Imports(className);
        StringWriter w = new StringWriter();
        String simpleClassName = Names.simpleClassName(className);
        try (PrintWriter p = new PrintWriter(w)) {
            Indent indent = new Indent();
            p.format("%spackage %s;", indent, Names.pkg(className));
            addImportsToken(p);
            p.format("\npublic final class %s {\n", simpleClassName);
            writer.write(indent.right(), imports, p);
            p.format("}");
        }
        writeToFile(w, imports, file);
    }

    private static void addImportsToken(PrintWriter p) {
        p.format("\n" + IMPORTS_TOKEN);
    }

    private static void writeToFile(StringWriter s, Imports imports, File file) {
        try {
            Files.write(s.toString().replace(IMPORTS_TOKEN, imports.toString()).getBytes(StandardCharsets.UTF_8), file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
