package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
        SwaggerParseResult result = new OpenAPIParser().readContents(definition.definition(), null,
                null);
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
        JavaClassWriter.write(file, className, (indent, imports, p) -> {
            p.format("%s// TODO\n", indent);
        });
    }

    private static void writeSchemaClasses(OpenAPI api, Names names) {
        @SuppressWarnings("unchecked")
        Map<String, Schema<?>> schemas = (Map<String, Schema<?>>) (Map<String, ?>) api
                .getComponents().getSchemas();
        for (Entry<String, Schema<?>> entry : schemas.entrySet()) {
            writeSchemaClass(names, entry.getKey(), 0, entry.getValue());
        }
    }

    private static void writeSchemaClass(Names names, String schemaName, int number,
            Schema<?> schema) {
        String className = names.schemaNameToClassName(schemaName);
        File file = names.schemaNameToJavaFile(schemaName + (number == 0 ? "" : "_" + number + ""));
        JavaClassWriter.write(file, className, (indent, imports, p) -> {
            writeSchemaClassContent(schema, indent, imports, p);
        });
    }

    private static void writeSchemaClassContent(Schema<?> schema, Indent indent, Imports imports,
            PrintWriter p) {
        if (schema.getType() != null) {
            Class<?> cls = toClass(schema.getType(), schema.getFormat());
            final String t;
            if (cls.equals(Byte.class)) {
                t = "byte[]";
            } else {
                t = imports.add(cls);
            }
            p.format("\n%sprivate %s value;\n", indent, t);
            p.format("\n%spublic %s getValue() {\n", indent, t);
            p.format("%sreturn value;\n", indent.right());
            p.format("%s}\n", indent.left());
        } else if (schema.getProperties() != null) {
            for (@SuppressWarnings("rawtypes")
            Entry<String, Schema> entry : schema.getProperties().entrySet()) {
                if (entry.getValue().get$ref() == null) {
                    String memberClassSimpleName = Names
                            .propertyNameToClassSimpleName(entry.getKey());
                    String fieldName = Names.propertyNameToFieldName(entry.getKey());
                    p.format("\n%sprivate %s %s;\n", indent, memberClassSimpleName, fieldName);
                    p.format("\n%spublic static final class %s {\n", indent, memberClassSimpleName);
                    writeSchemaClassContent(entry.getValue(), indent.right(), imports, p);
                    p.format("%s}\n", indent.left());
                }
            }
        }
    }

    private static Class<?> toClass(String type, String format) {
        if ("string".equals(type)) {
            if ("date-time".equals(format)) {
                return OffsetDateTime.class;
            } else if ("date".equals(format)) {
                return null;
            } else if ("byte".equals(format)) {
                return Byte.class;
            } else if ("binary".equals(format)) {
                return Byte.class;
            } else {
                return String.class;
            }
        } else if ("boolean".equals(type)) {
            return Boolean.class;
        } else if ("integer".equals(type)) {
            if ("int32".equals(format)) {
                return Integer.class;
            } else if ("int64".equals(format)) {
                return Long.class;
            } else {
                return BigInteger.class;
            }
        } else if ("number".equals(type)) {
            if ("float".equals(format)) {
                return Float.class;
            } else if ("double".equals(format)) {
                return Double.class;
            } else {
                return BigDecimal.class;
            }
        } else if ("array".equals(type)) {
            return List.class;
        } else if ("object".equals(type)) {
            return Object.class;
        } else {
            return null;
        }
    }

}
