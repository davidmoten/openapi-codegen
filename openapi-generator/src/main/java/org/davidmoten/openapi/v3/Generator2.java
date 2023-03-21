package org.davidmoten.openapi.v3;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import io.swagger.v3.oas.models.media.Schema;

public class Generator2 {
    
    private final Definition definition;

    public Generator2(Definition definition) {
        this.definition = definition;
    }

    public void generate() {

        // Names object for each Packages object
        Names names = new Names(definition);

        // generate methods on singleton client object in client package
        writeClientClass(names);

        // generate model classes for schema definitions
        writeSchemaClasses(definition, names);

//        superClasses(api).entrySet().forEach(x -> System.out.println(x.getKey().get$ref() + "->"
//                + x.getValue().stream().map(y -> y.toString()).collect(Collectors.toList())));
    }

    private static void writeClientClass(Names names) {
        String className = names.clientClassName();
        File file = names.clientClassJavaFile();
        JavaClassWriter.write(file, className, ClassType.CLASS, (indent, imports, p) -> {
            p.format("%s// TODO\n", indent);
        });
    }

    private static void writeSchemaClasses(Definition definition, Names names) {
        @SuppressWarnings("unchecked")
        Map<String, Schema<?>> schemas = (Map<String, Schema<?>>) (Map<String, ?>) names.api().getComponents()
                .getSchemas();
        for (Entry<String, Schema<?>> entry : schemas.entrySet()) {
            writeSchemaClass(names, entry.getKey(), entry.getValue(), definition);
        }
    }

    private static void writeSchemaClass(Names names, String schemaName, Schema<?> schema, Definition definition) {
        String className = names.schemaNameToClassName(schemaName);
        File file = names.schemaNameToJavaFile(schemaName);
        JavaClassWriter.write(file, className, classType(schema), (indent, imports, p) -> {
            writeClassContentForType(schema, indent, imports, p, Optional.empty(), true, false, className, definition,
                    names);
        });
    }

}
