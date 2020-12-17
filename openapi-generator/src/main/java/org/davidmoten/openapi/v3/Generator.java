package org.davidmoten.openapi.v3;

import java.util.Map;

import com.google.common.base.Function;

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
        SwaggerParseResult result = new OpenAPIParser().readContents(definition.definition(), null, null);
        result.getMessages().stream().forEach(System.out::println);
        OpenAPI api = result.getOpenAPI();
        System.out.println(api);

        // Names object for each Packages object
        Names names = new Names(definition);
        
        // generate methods on singleton client object in client package
        
        // generate model classes for schema definitions 
        Map<String, Schema> schemas = api.getComponents().getSchemas();
        for (String schemaName: schemas.keySet()) {
            String className = names.schemaNameToClassName(schemaName);
            String simpleClassName =  Names.simpleClassName(className);
        }
    }

}
