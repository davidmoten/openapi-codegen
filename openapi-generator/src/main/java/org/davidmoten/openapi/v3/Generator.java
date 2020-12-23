package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
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
//        System.out.println(api);

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
            writeSchemaClass(names, entry.getKey(), entry.getValue());
        }
    }

    private static void writeSchemaClass(Names names, String schemaName, Schema<?> schema) {
        String className = names.schemaNameToClassName(schemaName);
        File file = names.schemaNameToJavaFile(schemaName);
        JavaClassWriter.write(file, className, (indent, imports, p) -> {
            String typ = writeClassForType(schema, indent, imports, p, Optional.of("value"));
        });
    }

    // is root schema with nameless content (so call the content `value`)
    // is root schema with
    //
//    private static String writeSchemaClassContent2(Schema<?> schema, boolean isRoot, Indent indent,
//            Imports imports, PrintWriter p) {
//        if (schema.getType() != null) {
//            String fullClassName = writeClassForType(schema, indent, imports, p);
//            Class<?> cls = toClass(schema.getType(), schema.getFormat());
//            final String t;
//            if (cls.equals(Byte.class)) {
//                t = "byte[]";
//            } else {
//                t = imports.add(cls);
//            }
//            p.format("\n%sprivate %s value;\n", indent, t);
//            p.format("\n%spublic %s getValue() {\n", indent, t);
//            p.format("%sreturn value;\n", indent.right());
//            p.format("%s}\n", indent.left());
//        } else if (schema.getProperties() != null) {
//            // type == object
//            for (@SuppressWarnings("rawtypes")
//            Entry<String, Schema> entry : schema.getProperties().entrySet()) {
//                Schema<?> sch = entry.getValue();
//                if (sch.get$ref() == null) {
//                    String fieldName = Names.propertyNameToFieldName(entry.getKey());
//                    if (sch.getType() != null && isPrimitive(sch.getType())) {
//                        Class<?> cls = toClass(sch.getType(), sch.getFormat());
//                        p.format("\n%sprivate %s %s;\n", indent, imports.add(cls), fieldName);
//                    } else {
//                        String memberClassSimpleName = Names
//                                .propertyNameToClassSimpleName(entry.getKey());
//                        p.format("\n%sprivate %s %s;\n", indent, memberClassSimpleName, fieldName);
//                        p.format("\n%spublic static final class %s {\n", indent,
//                                memberClassSimpleName);
//                        writeSchemaClassContent(sch, false, indent.right(), imports, p);
//                        p.format("%s}\n", indent.left());
//                    }
//                }
//            }
//        }
//    }

    private static String writeClassForType(Schema<?> schema, Indent indent, Imports imports,
            PrintWriter p, Optional<String> name) {
        if (schema.getType() != null) {
            if (isPrimitive(schema.getType())) {
                Class<?> cls = toClass(schema.getType(), schema.getFormat());
                p.format("\n%sprivate %s %s;\n", indent, imports.add(cls), Names.toFieldName(
                        name.orElse(Optional.ofNullable(schema.getName()).orElse("value"))));
                return cls.getName();
            } else if (isArray(schema.getType())) {
                ArraySchema as = (ArraySchema) schema;
                Schema<?> arraySchema = as.getItems();
                String type = writeClassForType(arraySchema, indent, imports, p, name);
                p.format("\n%sprivate %s<%s> %s;\n", indent, imports.add(List.class),
                        imports.add(type), Names.schemaNameToFieldName(schema.getName()));
                return imports.add(List.class) + "<" + imports.add(type) + ">";
            } else if (isObject(schema.getType())) {
                Preconditions.checkNotNull(schema.getProperties());
                // type == object
                p.format("\n%spublic static final class %s {\n", indent,
                        name.orElse(Optional.ofNullable(schema.getName()).orElse("Anon")));
                for (@SuppressWarnings("rawtypes")
                Entry<String, Schema> entry : schema.getProperties().entrySet()) {
                    Schema<?> sch = entry.getValue();
                    if (sch.get$ref() == null) {
                        String type = writeClassForType(sch, indent, imports, p,
                                Optional.of(entry.getKey()));
                        String fieldName = Names.propertyNameToFieldName(entry.getKey());
                        p.format("\n%sprivate %s %s;\n", indent, imports.add(type), fieldName);
                    }
                }
                p.format("%s}\n", indent.left());
            }
        } else {
            System.out.println("schema not implemented for " + schema);
            return "java.lang.String";
        }
        throw new RuntimeException("not implemented");
    }

    private static boolean isArray(String type) {
        Preconditions.checkNotNull(type);
        return "array".equals(type);
    }

    private static boolean isObject(String type) {
        return type == null || "object".equals(type);
    }

    private static void writeSchemaClassContent(Schema<?> schema, boolean isRoot, Indent indent,
            Imports imports, PrintWriter p) {
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
            // type == object
            for (@SuppressWarnings("rawtypes")
            Entry<String, Schema> entry : schema.getProperties().entrySet()) {
                Schema<?> sch = entry.getValue();
                if (sch.get$ref() == null) {
                    String fieldName = Names.propertyNameToFieldName(entry.getKey());
                    if (sch.getType() != null && isPrimitive(sch.getType())) {
                        Class<?> cls = toClass(sch.getType(), sch.getFormat());
                        p.format("\n%sprivate %s %s;\n", indent, imports.add(cls), fieldName);
                    } else {
                        String memberClassSimpleName = Names
                                .propertyNameToClassSimpleName(entry.getKey());
                        p.format("\n%sprivate %s %s;\n", indent, memberClassSimpleName, fieldName);
                        p.format("\n%spublic static final class %s {\n", indent,
                                memberClassSimpleName);
                        writeSchemaClassContent(sch, false, indent.right(), imports, p);
                        p.format("%s}\n", indent.left());
                    }
                }
            }
        }
    }

    private static boolean isPrimitive(String type) {
        Preconditions.checkNotNull(type);
        return !"array".equals(type) && !"object".equals(type);
    }

    private static Class<?> toClass(String type, String format) {
        Preconditions.checkNotNull(type);
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
