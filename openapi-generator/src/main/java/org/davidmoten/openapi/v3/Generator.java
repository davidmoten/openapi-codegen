package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Generator {

    private final Definition definition;

    public Generator(Definition definition) {
        this.definition = definition;
    }

    public void generate() {
        try {
            SwaggerParseResult result = new OpenAPIParser().readContents(definition.definition(), null, null);
            result.getMessages().stream().forEach(System.out::println);
            OpenAPI api = result.getOpenAPI();
            System.out.println(api);

            // Names object for each Packages object
            Names names = new Names(definition);

            // generate methods on singleton client object in client package

            // generate model classes for schema definitions
            @SuppressWarnings("unchecked")
            Map<String, Schema<?>> schemas = (Map<String, Schema<?>>) (Map<String, ?>) api.getComponents().getSchemas();
            for (String schemaName : schemas.keySet()) {
                String className = names.schemaNameToClassName(schemaName);
                String simpleClassName = Names.simpleClassName(className);
                File file = names.schemaNameToJavaFile(schemaName);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
