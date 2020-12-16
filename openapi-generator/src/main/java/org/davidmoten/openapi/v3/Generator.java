package org.davidmoten.openapi.v3;

import java.util.List;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Generator {

    private final List<Definition> definitions;

    public Generator(List<Definition> definitions) {
        this.definitions = definitions;
    }

    public void generate() {
        for (Definition definition : definitions) {
            SwaggerParseResult result = new OpenAPIParser().readContents(definition.definition(), null, null);
            result.getMessages().stream().forEach(System.out::println);
            OpenAPI api = result.getOpenAPI();
            System.out.println(api);

            // generate methods on singleton client object

        }
    }

}
