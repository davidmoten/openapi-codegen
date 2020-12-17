package org.davidmoten.openapi.v3;

import com.google.common.base.Function;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Generator {

    private final Definition definition;
    private final Function<String, String> externalRefToClassName;

    public Generator(Definition definition, Function<String, String> externalRefToClassName) {
        this.definition = definition;
        this.externalRefToClassName = externalRefToClassName;
    }

    public void generate() {
        SwaggerParseResult result = new OpenAPIParser().readContents(definition.definition(), null, null);
        result.getMessages().stream().forEach(System.out::println);
        OpenAPI api = result.getOpenAPI();
        System.out.println(api);

        // Names object for each Packages object
        
        // generate methods on singleton client object in client package
        
        // generate model classes for schema definitions 

    }

}
