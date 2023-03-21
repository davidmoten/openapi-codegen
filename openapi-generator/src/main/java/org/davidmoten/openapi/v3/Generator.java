package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.Preconditions;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

public final class Generator {

    private final Definition definition;

    public Generator(Definition definition) {
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

    // returns an imported type (using imports.add), type may include generics
    private static String writeClassContentForType(Schema<?> schema, Indent indent, Imports imports, PrintWriter p,
            Optional<String> name, boolean isRoot, boolean isArrayItem, String parentFullClassName,
            Definition definition, Names names) {
        if (isEnum(schema)) {
            Preconditions.checkArgument(isPrimitive(schema.getType()));
            String clsName;
            if (isRoot) {
                clsName = Names.simpleClassName(parentFullClassName);
            } else {
                clsName = Names.simpleClassNameFromSimpleName(
                        name.orElse(Optional.ofNullable(schema.getName()).orElse("Enum")));
                p.format("\n%spublic enum %s {\n", indent, clsName);
                indent.right();
            }
            Class<?> valueCls = toClass(schema.getType(), schema.getFormat());
            String valueType = imports.add(valueCls);

            p.format("%sprivate %s value;\n", indent, valueType);
            p.format("\n");
            for (int i = 0; i < schema.getEnum().size(); i++) {
                if (i > 0 && i < schema.getEnum().size()) {
                    p.format(",\n");
                }
                Object o = schema.getEnum().get(i);
                String delim = o instanceof String ? "\"" : "";
                p.format("%s%s(%s%s%s)", indent, Names.enumNameToEnumConstant(o.toString()), delim, o, delim);
            }
            p.format(";\n");
            indent.left();
            p.format("\n%sprivate %s(%s value) {\n", indent.right(), clsName, valueType);
            p.format("%sthis.value = value;\n", indent.right());
            p.format("%s}\n", indent.left());

            p.format("\n%spublic %s value() {\n", indent, valueType);
            p.format("%sreturn value;\n", indent.right());
            p.format("%s}\n", indent.left());

            if (!isRoot) {
                String fullClsName = parentFullClassName + "." + clsName;
                p.format("%s}\n", indent.left());
                if (!isArrayItem) {
                    String fieldName = Names
                            .toFieldName(name.orElse(Optional.ofNullable(schema.getName()).orElse("value")));
                    p.format("\n%sprivate final %s %s;\n", indent, imports.add(fullClsName), fieldName);
                    p.format("\n%spublic %s %s() {\n", indent, imports.add(fullClsName), fieldName);
                    indent.right();
                    p.format("%sreturn %s;\n", indent, fieldName);
                    indent.left();
                    p.format("%s}\n", indent);
                }
                return imports.add(fullClsName);
            } else {
                return imports.add(parentFullClassName);
            }
        } else if (isPrimitive(schema.getType())) {
            Class<?> cls = toClass(schema.getType(), schema.getFormat());
            if (!isArrayItem) {
                String fieldName = Names
                        .toFieldName(name.orElse(Optional.ofNullable(schema.getName()).orElse("value")));
                String fieldType = imports.add(cls);
                p.format("%sprivate final %s %s;\n", indent, fieldType, fieldName);

                addConstructorAndGetterForSingleFieldType(indent, p, isRoot, parentFullClassName, fieldName, fieldType);
            }
            return imports.add(cls);
        } else if (isArray(schema.getType())) {
            ArraySchema as = (ArraySchema) schema;
            Schema<?> itemSchema = as.getItems();
            Optional<String> nm = Optional.of(name.orElse("") + "Item");
            String type = writeClassContentForType(itemSchema, indent, imports, p, nm, false, true, parentFullClassName,
                    definition, names);
            if (!isArrayItem) {
                String fieldName = Names.toFieldName(name.orElse("value"));
                p.format("%sprivate final %s<%s> %s;\n", indent, imports.add(List.class), type, fieldName);
                if (isRoot) {
                    p.format("\n%spublic %s(%s<%s> %s) {\n", indent, simpleClassName(parentFullClassName),
                            imports.add(List.class), type, fieldName);
                    indent.right();
                    p.format("%sthis.%s = %s;\n", indent, fieldName, fieldName);
                    indent.left();
                    p.format("%s}\n", indent);

                    p.format("\n%spublic %s<%s> %s() {\n", indent, imports.add(List.class), type, fieldName);
                    indent.right();
                    p.format("%sreturn %s;\n", indent, fieldName);
                    indent.left();
                    p.format("%s}\n", indent);
                }
            }
            return imports.add(List.class) + "<" + type + ">";
        } else if (isRef(schema)) {
            String ref = schema.get$ref();
            final String type;
            if (!ref.startsWith("#")) {
                type = definition.externalRefClassName(ref);
            } else {
                String schemaName = ref.substring(ref.lastIndexOf("/") + 1);
                type = names.schemaNameToClassName(schemaName);
            }
            String importedType = imports.add(type);
            if (!isArrayItem) {
                String fieldName = Names
                        .toFieldName(name.orElse(Optional.ofNullable(schema.getName()).orElse("value")));
                p.format("%sprivate final %s %s;\n", indent, importedType, fieldName);
                addConstructorAndGetterForSingleFieldType(indent, p, isRoot, parentFullClassName, fieldName,
                        importedType);
            }
            return importedType;
        } else if (isObject(schema)) {
            // type == object
            Preconditions.checkNotNull(schema.getProperties());
            String fullClsName;
            if (isRoot) {
                fullClsName = parentFullClassName;
            } else {
                String clsName = Names.simpleClassNameFromSimpleName(
                        name.orElse(Optional.ofNullable(schema.getName()).orElse("Anon")));
                fullClsName = parentFullClassName + "." + clsName;
                p.format("\n%spublic static final class %s {\n", indent, clsName);
                indent.right();
            }
            List<Field> fields = new ArrayList<>();
            for (@SuppressWarnings("rawtypes")
            Entry<String, Schema> entry : schema.getProperties().entrySet()) {
                Schema<?> sch = entry.getValue();
                String fieldName = Names.propertyNameToFieldName(entry.getKey());
                String t = writeClassContentForType(sch, indent, imports, p, Optional.of(fieldName), false, false,
                        fullClsName, definition, names);
                fields.add(new Field(entry.getKey(), fieldName, t));
            }

            // add constructor with fields
            indent.right();
            indent.right();
            String constructorArgs = fields.stream().map(x -> String.format("\n%s%s %s", indent, x.type, x.fieldName))
                    .collect(Collectors.joining(","));
            indent.left();
            indent.left();
            p.format("\n%spublic %s(%s) {\n", indent, simpleClassName(fullClsName), constructorArgs);
            indent.right();
            fields.forEach(x -> {
                boolean required = schema.getRequired() != null && schema.getRequired().contains(x.propertyName);
                if (required) {
                    p.format("%sif (%s == null) {\n", indent, x.fieldName);
                    indent.right();
                    p.format("%sthrow new %s(\"%s cannot be null\");\n", indent,
                            imports.add(IllegalArgumentException.class), x.fieldName);
                    indent.left();
                    p.format("%s}\n", indent);
                }
                p.format("%sthis.%s = %s;\n", indent, x.fieldName, x.fieldName);
            });
            indent.left();
            p.format("%s}\n", indent);

            fields.forEach(x -> {
                p.format("\n%spublic %s %s() {\n", indent, x.type, x.fieldName);
                indent.right();
                p.format("%sreturn %s;\n", indent, x.fieldName);
                indent.left();
                p.format("%s}\n", indent);
            });

            if (!isRoot) {
                p.format("%s}\n", indent.left());
            }
            return imports.add(fullClsName);
        }
        if (isOneOf(schema)) {
            final String fullClassName;
            if (!isRoot) {
                String clsName = Names.simpleClassNameFromSimpleName(name.orElse("Anon"));
                fullClassName = parentFullClassName + "." + clsName;
                p.format("%spublic interface %s {\n", indent, clsName);
                indent.right();
            } else {
                fullClassName = parentFullClassName;
            }
            if (schema.getDiscriminator() != null) {
                // TODO confirm String discriminator is reasonable assumption
                // TODO use discriminator property mapping
                p.format("%s%s %s();\n", indent, imports.add(String.class),
                        Names.propertyNameToFieldName(schema.getDiscriminator().getPropertyName()));
            }
            if (!isRoot) {
                indent.left();
                p.format("%s}\n", indent);
            }
            return imports.add(fullClassName);
        } else {
            System.out.println("schema not implemented for " + schema);
            throw new RuntimeException("not implemented");
        }
    }

    private static void addConstructorAndGetterForSingleFieldType(Indent indent, PrintWriter p, boolean isRoot,
            String parentFullClassName, String fieldName, String fieldType) {
        if (isRoot) {
            p.format("\n%spublic %s(%s %s) {\n", indent, simpleClassName(parentFullClassName), fieldType, fieldName);
            indent.right();
            p.format("%sthis.%s = %s;\n", indent, fieldName, fieldName);
            indent.left();
            p.format("%s}\n", indent);

            p.format("\n%spublic %s %s() {\n", indent, fieldType, fieldName);
            indent.right();
            p.format("%sreturn %s;\n", indent, fieldName);
            indent.left();
            p.format("%s}\n", indent);
        }
    }

    private static String simpleClassName(String fullClassName) {
        int i = fullClassName.lastIndexOf(".");
        if (i == -1) {
            return fullClassName;
        } else {
            return fullClassName.substring(i + 1);
        }
    }

    private static final class Field {
        final String propertyName;
        final String fieldName;
        final String type;

        public Field(String propertyName, String fieldName, String type) {
            this.propertyName = propertyName;
            this.fieldName = fieldName;
            this.type = type;
        }

    }

    static boolean isEnum(Schema<?> schema) {
        return schema.getEnum() != null && !schema.getEnum().isEmpty();
    }

    private static boolean isRef(Schema<?> schema) {
        return schema.get$ref() != null;
    }

    private static boolean isArray(String type) {
        return "array".equals(type);
    }

    private static boolean isObject(Schema<?> schema) {
        return (schema.getType() == null && schema.getProperties() != null) || "object".equals(schema.getType());
    }

    static boolean isOneOf(Schema<?> schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        ComposedSchema sch = (ComposedSchema) schema;
        return sch.getOneOf() != null && !sch.getOneOf().isEmpty();
    }

    private static boolean isPrimitive(String type) {
        return type != null && !"array".equals(type) && !"object".equals(type);
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

    private static ClassType classType(Schema<?> schema) {
        if (schema instanceof ComposedSchema && ((ComposedSchema) schema).getOneOf() != null) {
            return ClassType.INTERFACE;
        } else if (schema.getEnum() != null) {
            return ClassType.ENUM;
        } else {
            return ClassType.CLASS;
        }
    }

}
