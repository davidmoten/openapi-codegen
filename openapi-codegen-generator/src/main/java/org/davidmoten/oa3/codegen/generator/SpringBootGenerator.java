package org.davidmoten.oa3.codegen.generator;

import org.davidmoten.oa3.codegen.generator.internal.ImmutableList;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;

public class SpringBootGenerator {

    private final Names names;

    public SpringBootGenerator(Names names) {
        this.names = names;
    }

    public void generate() {
        names.api().getPaths().forEach((pathName, pathItem) -> {
            ImmutableList<String> list = ImmutableList.of(pathName);
            handlePathItem(list, pathItem);
        });
    }

    private void handlePathItem(ImmutableList<String> list, PathItem pathItem) {
        pathItem.get$ref(); // TODO
        pathItem.readOperationsMap() //
                .forEach((method, operation) -> handleOperation(list.add(method.name()), operation));
    }

    private void handleOperation(ImmutableList<String> list, Operation operation) {
        if (operation.getParameters() != null) {
            operation.getParameters() //
                    .forEach(parameter -> handleParameter(list, parameter));
        }
    }

    private void handleParameter(ImmutableList<String> list, Parameter parameter) {
        parameter.get$ref();
        parameter.getSchema();
        parameter.getContent().forEach((mimeType, mediaType) -> {
        });
    }

    private static final class Method {

    }

}
