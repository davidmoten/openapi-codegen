package org.davidmoten.openapi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class Generator {

    public static void main(String[] args) throws IOException {
        String definition = new String(Files.readAllBytes(new File("src/test/resources/openapi.yml").toPath()),
                StandardCharsets.UTF_8);

        SwaggerParseResult result = new OpenAPIParser().readContents(definition, null, null);
        result.getMessages().stream().forEach(System.out::println);
        OpenAPI a = result.getOpenAPI();
        System.out.println(a);
        a.getComponents().getSchemas().keySet().forEach(System.out::println);;
    }

}
