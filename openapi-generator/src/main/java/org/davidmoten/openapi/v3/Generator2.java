package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.davidmoten.openapi.v3.internal.ByteArrayPrintStream;
import org.davidmoten.openapi.v3.runtime.OneOfDeserializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
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
//        writeClientClass(names);

        // generate model classes for schema definitions
        writeSchemaClasses(definition, names);

    }

    private static void writeSchemaClasses(Definition definition, Names names) {
        names.api().getComponents().getSchemas().entrySet().forEach(entry -> {
            MyVisitor v = new MyVisitor(names);
            Apis.visitSchemas(entry.getKey(), entry.getValue(), v);
        });
    }

    private static final class Cls {
        String fullClassName;
        ClassType classType;
        List<Field> fields = new ArrayList<>();
        List<EnumMember> enumMembers = new ArrayList<>();
        List<Cls> classes = new ArrayList<>();
        Discriminator discriminator = null;
        String enumFullType;
        private int num = 0;
        private Set<String> fieldNames = new HashSet<String>();
        boolean topLevel = false;
        boolean hasProperties = false;

        private String nextAnonymousFieldName() {
            num++;
            return "anonymous" + num;
        }

        String nextFieldName(String name) {
            final String next;
            if (name == null) {
                next = nextAnonymousFieldName();
            } else {
                String s = Names.toFieldName(name);
                String a;
                int i = 0;
                while (true) {
                    if (i > 0) {
                        a = s + i;
                    } else {
                        a = s;
                    }
                    if (!fieldNames.contains(s)) {
                        break;
                    }
                    i++;
                }
                next = a;
            }
            fieldNames.add(next);
            return next;
        }

        void addField(String fullType, String name, String fieldName, boolean required, boolean isArray) {
            fields.add(new Field(fullType, name, fieldName, required, isArray, Optional.empty(), Optional.empty(),
                    Optional.empty()));
        }

        void addField(String fullType, String name, String fieldName, boolean required, boolean isArray,
                Optional<Integer> minLength, Optional<Integer> maxLength, Optional<String> pattern) {
            fields.add(new Field(fullType, name, fieldName, required, isArray, minLength, maxLength, pattern));
        }

        public String pkg() {
            return Names.pkg(fullClassName);
        }

        public String simpleName() {
            return Names.simpleClassName(fullClassName);
        }

        public boolean unwrapSingleField() {
            return !hasProperties && (classType == ClassType.ENUM || (topLevel && fields.size() == 1));
        }

        public String discriminatorValueFromFullClassName(String fullClassName) {
            String value = discriminator.fullClassNameToPropertyValue.get(fullClassName);
            if (value == null) {
                // TODO review using simple class name for value because collision risk
                return Names.simpleClassName(fullClassName);
            } else {
                return value;
            }
        }
    }

    private static class EnumMember {
        String name;
        Object parameter;

        EnumMember(String name, Object parameter) {
            this.name = name;
            this.parameter = parameter;
        }
    }

    private enum ClassType {
        CLASS("class"), //
        ENUM("enum"), //
        ONE_OR_ANY_OF_DISCRIMINATED("interface"), //
        ONE_OR_ANY_OF_NON_DISCRIMINATED("class");

        private final String word;

        ClassType(String word) {
            this.word = word;
        }

        String word() {
            return word;
        }
    }

    private static final Set<String> PRIMITIVE_CLASS_NAMES = Sets.newHashSet("int", "long", "byte", "float", "double",
            "boolean", "short");

    private final static class Field {
        private String fullClassName;
        final String name;
        final String fieldName;
        final boolean required;
        final Optional<Integer> minLength;
        final Optional<Integer> maxLength;
        final Optional<String> pattern;
        final boolean isArray;

        public Field(String fullClassName, String name, String fieldName, boolean required, boolean isArray,
                Optional<Integer> minLength, Optional<Integer> maxLength, Optional<String> pattern) {
            this.fullClassName = fullClassName;
            this.name = name;
            this.fieldName = fieldName;
            this.required = required;
            this.isArray = isArray;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.pattern = pattern;

        }

        public String resolvedType(Imports imports) {
            return Generator2.resolvedType(this, imports);
        }

        public String resolvedTypeNullable(Imports imports) {
            return Generator2.resolvedTypeNullable(this, imports);
        }

        public boolean isPrimitive() {
            return required && PRIMITIVE_CLASS_NAMES.contains(toPrimitive(fullClassName));
        }

        @Override
        public String toString() {
            return "Field [fullClassName=" + fullClassName + ", name=" + name + ", fieldName=" + fieldName
                    + ", required=" + required + ", minLength=" + minLength + ", maxLength=" + maxLength + "]";
        }
    }

    private static final class LinkedStack<T> {
        LinkedStackNode<T> last;

        void push(T value) {
            LinkedStackNode<T> node = new LinkedStackNode<>(value);
            if (last == null) {
                last = node;
            } else {
                node.previous = last;
                last = node;
            }
        }

        T pop() {
            if (last == null) {
                return null;
            } else {
                T v = last.value;
                last = last.previous;
                return v;
            }
        }

        T peek() {
            if (last == null) {
                return null;
            } else {
                return last.value;
            }
        }

        public boolean isEmpty() {
            return last == null;
        }
    }

    private static final class LinkedStackNode<T> {
        final T value;
        LinkedStackNode<T> previous;

        LinkedStackNode(T value) {
            this.value = value;
        }
    }

    private static final class MyVisitor implements Visitor {
        private final Names names;
        private Imports imports;
        private LinkedStack<Cls> stack = new LinkedStack<>();
        private ByteArrayPrintStream out;

        public MyVisitor(Names names) {
            this.names = names;
            this.out = ByteArrayPrintStream.create();
        }

        @Override
        public void startSchema(ImmutableList<SchemaWithName> schemaPath) {
            SchemaWithName last = schemaPath.last();
            Schema<?> schema = last.schema;
            final Cls cls = new Cls();
            if (stack.isEmpty()) {
                // should be top-level class
                cls.fullClassName = names.schemaNameToClassName(last.name);
                imports = new Imports(cls.fullClassName);
                cls.classType = classType(schema);
                cls.topLevel = true;
            }
            if (isArray(schema)) {
                stack.push(cls);
                return;
            }
            boolean isArray = schemaPath.size() >= 2 && schemaPath.secondLast().schema instanceof ArraySchema;
            if (isObject(schema) || isMap(schema) || isEnum(schema) || isOneOf(schema) || isAnyOf(schema)) {
                Optional<Cls> previous = Optional.ofNullable(stack.peek());
                stack.push(cls);
                previous.ifPresent(p -> p.classes.add(cls));
                final Optional<String> fieldName;
                if (previous.isPresent()) {
                    fieldName = Optional.of(previous.get().nextFieldName(last.name));
                    String fullClassName = previous.get().fullClassName + "."
                            + Names.simpleClassNameFromSimpleName(fieldName.get());
                    cls.fullClassName = fullClassName;
                } else {
                    cls.fullClassName = names.schemaNameToClassName(last.name);
                    fieldName = Optional.empty();
                }
                if (isEnum(schema)) {
                    handleEnum(schema, cls);
                } else if (isObject(schema)) {
                    cls.classType = ClassType.CLASS;
                    cls.hasProperties = true;
                    boolean required = fieldIsRequired(schemaPath);
                    previous.ifPresent(
                            p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required, isArray));
                } else if (isOneOf(schema) || isAnyOf(schema)) {
                    handleOneOrAnyOf(last, cls, names);
                } else {
                    // TODO
                    cls.fullClassName = previous + ".Unknown";
                    cls.classType = ClassType.CLASS;
                }
            } else {
                if (stack.isEmpty()) {
                    stack.push(cls);
                }
                Cls current = stack.peek();
                final String fullClassName;
                if (isPrimitive(schema)) {
                    Class<?> c = toClass(schema.getType(), schema.getFormat());
                    fullClassName = c.getCanonicalName();
                    final Optional<Integer> minLength;
                    final Optional<Integer> maxLength;
                    final Optional<String> pattern;
                    if (isString(schema)) {
                        minLength = Optional.ofNullable(schema.getMinLength());
                        maxLength = Optional.ofNullable(schema.getMaxLength());
                        pattern = Optional.ofNullable(schema.getPattern());
                    } else {
                        minLength = Optional.empty();
                        maxLength = Optional.empty();
                        pattern = Optional.empty();
                    }
                    String fieldName = current.nextFieldName(last.name);
                    boolean required = fieldIsRequired(schemaPath);
                    current.addField(fullClassName, last.name, fieldName, required, isArray, minLength, maxLength,
                            pattern);
                } else if (isRef(schema)) {
                    fullClassName = names.refToFullClassName(schema.get$ref());
                    String fieldName = current.nextFieldName(last.name);
                    boolean required = fieldIsRequired(schemaPath);
                    current.addField(fullClassName, last.name, fieldName, required, isArray);
                } else {
                    throw new RuntimeException("unexpected");
                }
            }
        }

        @Override
        public void finishSchema(ImmutableList<SchemaWithName> schemaPath) {
            final Cls cls = stack.peek();
            if (Apis.isComplexSchema(schemaPath.last().schema) || isEnum(schemaPath.last().schema)
                    || (schemaPath.size() == 1)) {
                stack.pop();
                if (stack.isEmpty()) {
                    Indent indent = new Indent();
                    out.format("package %s;\n", cls.pkg());
                    out.format("\nIMPORTS_HERE");
                    writeClass(out, imports, indent, cls);
                    System.out.println("////////////////////////////////////////////////");
                    String content = out.text().replace("IMPORTS_HERE", imports.toString());
                    System.out.println(content);
                    out.close();
                    File file = names.schemaNameToJavaFile(schemaPath.first().name);
                    file.getParentFile().mkdirs();
                    try {
                        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        }
    }

    private static void writeClass(PrintStream out, Imports imports, Indent indent, Cls cls) {
        writeClassDeclaration(out, imports, indent, cls);
        indent.right();
        writeEnumMembers(out, imports, indent, cls);
        if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED
                || cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeOneOrAnyOfClassContent(out, imports, indent, cls);
        } else {
            writeFields(out, imports, indent, cls);
            writeConstructor(out, imports, indent, cls);
            writeGetters(out, imports, indent, cls);
        }
        writeMemberClasses(out, imports, indent, cls);
        indent.left();
        out.format("%s}\n", indent);
    }

    private static void writeClassDeclaration(PrintStream out, Imports imports, Indent indent, Cls cls) {
        String modifier;
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED || cls.classType == ClassType.ENUM) {
            modifier = "";
        } else {
            modifier = cls.topLevel ? "final " : "static final ";
        }

        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.format("\n%s@%s(use = %s.NAME, property = \"%s\")\n", indent, 
                    imports.add(JsonTypeInfo.class), imports.add(Id.class), cls.discriminator.propertyName);
            indent.right().right();
            String types = cls.fields.stream()
                    .map(x -> String.format("\n%s@%s(value = %s.class, name = \"%s\")", indent, imports.add(Type.class),
                            imports.add(x.fullClassName), cls.discriminatorValueFromFullClassName(x.fullClassName)))
                    .collect(Collectors.joining(", "));
            indent.left();
            indent.left();
            out.format("%s@%s({%s})\n", indent, imports.add(JsonSubTypes.class), types);
        } else if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED) {
            writeOneOfDeserializerAnnotation(out, imports, indent, cls);
        } else {
            out.println();
        }
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.format("%s@%s(%s.NON_NULL)\n", indent, imports.add(JsonInclude.class), imports.add(Include.class));
        }
        out.format("%spublic %s%s %s {\n", indent, modifier, cls.classType.word(), cls.simpleName());
    }

    private static void writeOneOfDeserializerAnnotation(PrintStream out, Imports imports, Indent indent, Cls cls) {
        out.format("\n%s@%s(using = %s.Deserializer.class)\n", indent, imports.add(JsonDeserialize.class),
                cls.simpleName());
    }

//    private static void writeAutoDetectAnnotation(PrintStream out, Imports imports, Indent indent) {
//        out.format("%s@%s(fieldVisibility = %s.ANY)\n", indent, imports.add(JsonAutoDetect.class),
//                imports.add(Visibility.class), imports.add(Visibility.class));
//    }

    private static void writeEnumMembers(PrintStream out, Imports imports, Indent indent, Cls cls) {
        String text = cls.enumMembers.stream().map(x -> {
            String delim = x.parameter instanceof String ? "\"" : "";
            return String.format("%s%s(%s%s%s)", indent, x.name, delim, x.parameter, delim);
        }).collect(Collectors.joining(",\n"));
        if (!text.isEmpty()) {
            out.println("\n" + text + ";");
        }
    }

    private static boolean isString(Schema<?> schema) {
        return "string".equals(schema.getType());
    }

    private static boolean fieldIsRequired(ImmutableList<SchemaWithName> schemaPath) {
        SchemaWithName last = schemaPath.last();
        if (schemaPath.size() <= 1) {
            return isPrimitive(last.schema);
        } else {
            return contains(schemaPath.secondLast().schema.getRequired(), last.name);
        }
    }

    private static void handleOneOrAnyOf(SchemaWithName last, Cls cls, Names names) {

        io.swagger.v3.oas.models.media.Discriminator discriminator = last.schema.getDiscriminator();
        if (discriminator != null) {
            String propertyName = discriminator.getPropertyName();
            final Map<String, String> map;
            if (discriminator.getMapping() != null) {
                map = discriminator.getMapping().entrySet().stream()
                        .collect(Collectors.toMap(x -> names.refToFullClassName(x.getValue()), x -> x.getKey()));
            } else {
                map = Collections.emptyMap();
            }
            cls.discriminator = new Discriminator(propertyName, Names.toFieldName(propertyName), map);
            cls.classType = ClassType.ONE_OR_ANY_OF_DISCRIMINATED;
        } else {
            cls.classType = ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED;
        }
    }

    private static final class Discriminator {
        final String propertyName;
        final String fieldName;
        final Map<String, String> fullClassNameToPropertyValue;

        Discriminator(String propertyName, String fieldName, Map<String, String> fullClassNameToPropertyValue) {
            this.propertyName = propertyName;
            this.fieldName = fieldName;
            this.fullClassNameToPropertyValue = fullClassNameToPropertyValue;
        }
    }

    private static void handleEnum(Schema<?> schema, Cls cls) {
        cls.classType = ClassType.ENUM;
        Class<?> valueCls = toClass(schema.getType(), schema.getFormat());
        cls.enumFullType = valueCls.getCanonicalName();
        for (Object o : schema.getEnum()) {
            cls.enumMembers.add(new EnumMember(Names.enumNameToEnumConstant(o.toString()), o));
        }
        cls.addField(cls.enumFullType, "value", "value", true, false);
    }

    private static <T> boolean contains(Collection<? extends T> collection, T t) {
        return collection != null && t != null && collection.contains(t);
    }

    private static void writeOneOrAnyOfClassContent(PrintStream out, Imports imports, Indent indent, Cls cls) {
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.format("\n%s%s %s();\n", indent, imports.add(String.class), cls.discriminator.fieldName);
        } else {
            out.format("\n%s@%s\n", indent, imports.add(JsonValue.class));
            out.format("%sprivate final %s %s;\n", indent, imports.add(Object.class), "value");

            // add constructor for each member of the oneOf (fieldTypes)
            out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
            out.format("%sprivate %s(%s value) {\n", indent, cls.simpleName(), imports.add(Object.class));
            out.format("%sthis.value = %s.checkNotNull(value);\n", indent.right(), imports.add(Preconditions.class));
            out.format("%s}\n", indent.left());
            cls.fields.forEach(f -> {
                String className = toPrimitive(f.fullClassName);
                out.format("\n%spublic %s(%s value) {\n", indent, cls.simpleName(), imports.add(className));
                indent.right();
                if (Names.isPrimitiveFullClassName(className)) {
                    out.format("%sthis.value = value;\n", indent);
                } else {
                    out.format("%sthis.value = %s.checkNotNull(value);\n", indent, imports.add(Preconditions.class));
                }
                out.format("%s}\n", indent.left());
            });

            out.format("\n%spublic Object value() {\n", indent);
            out.format("%sreturn value;\n", indent.right());
            out.format("%s}\n", indent.left());

            out.format("\n%s@%s(\"serial\")\n", indent, imports.add(SuppressWarnings.class));
            out.format("%spublic static final class Deserializer extends %s<%s> {\n", indent,
                    imports.add(OneOfDeserializer.class), cls.simpleName());
            indent.right();
            out.format("\n%spublic Deserializer() {\n", indent);
            indent.right();
            String classes = cls.fields.stream().map(x -> imports.add(toPrimitive(x.fullClassName)) + ".class")
                    .collect(Collectors.joining(", "));
            out.format("%ssuper(%s.class, %s);\n", indent, cls.simpleName(), classes);
            indent.left();
            out.format("%s}\n", indent);
            indent.left();
            out.format("%s}\n", indent);
        }
    }

    private static void writeFields(PrintStream out, Imports imports, Indent indent, Cls cls) {
        if (!cls.fields.isEmpty()) {
            out.println();
        }
        cls.fields.forEach(f -> {
            if (f.minLength.isPresent() || f.maxLength.isPresent()) {
                String minParameters = f.minLength.map(x -> "min = " + x + ", ").orElse("");
                String maxParameters = f.maxLength.map(x -> "max = " + x + ", ").orElse("");
                String params = minParameters + maxParameters;
                out.format("%s@%s(%smessage = \"%s\")\n", indent, imports.add(Size.class), params,
                        "size constraint not met: " + params);
            }
            f.pattern.ifPresent(x -> out.format("%s@%s(regexp = \"%s\")\n", indent, imports.add(Pattern.class), x));
            if (cls.unwrapSingleField()) {
                out.format("%s@%s\n", indent, imports.add(JsonValue.class));
            }
            out.format("%sprivate final %s %s;\n", indent, f.resolvedTypeNullable(imports), f.fieldName);
        });
    }

    private static void writeConstructor(PrintStream out, Imports imports, Indent indent, Cls cls) {
        indent.right().right();
        final String parametersNullable;
        if (cls.unwrapSingleField()) {
            parametersNullable = cls.fields.stream()
                    .map(x -> String.format("\n%s%s %s", indent, x.resolvedTypeNullable(imports), x.fieldName))
                    .collect(Collectors.joining(","));
        } else {
            parametersNullable = cls.fields.stream().map(x -> String.format("\n%s@%s(\"%s\") %s %s", indent,
                    imports.add(JsonProperty.class), x.name, x.resolvedTypeNullable(imports), x.fieldName))
                    .collect(Collectors.joining(","));
        }
        indent.left().left();
        if (cls.classType != ClassType.ENUM) {
            out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
        } else {
            out.println();
        }
        boolean hasOptional = !cls.unwrapSingleField() && cls.fields.stream().anyMatch(f -> !f.required);
        // if has optional then write a private constructor with nullable parameters
        // and a public constructor with Optional parameters
        final String visibility = cls.classType == ClassType.ENUM || hasOptional ? "private" : "public";
        out.format("%s%s %s(%s) {\n", indent, visibility, Names.simpleClassName(cls.fullClassName), parametersNullable);
        indent.right();
        cls.fields.stream().forEach(x -> {
            if (!x.isPrimitive() && !x.required) {
                out.format("%sthis.%s = %s.checkNotNull(%s);\n", indent, x.fieldName, imports.add(Preconditions.class),
                        x.fieldName);
            } else {
                out.format("%sthis.%s = %s;\n", indent, x.fieldName, x.fieldName);
            }
        });
        indent.left();
        out.format("%s}\n", indent);
        if (hasOptional) {
            indent.right().right();
            String parametersOptional = cls.fields.stream()
                    .map(x -> String.format("\n%s%s %s", indent, x.resolvedType(imports), x.fieldName))
                    .collect(Collectors.joining(","));
            indent.left().left();
            out.format("\n%spublic %s(%s) {\n", indent, Names.simpleClassName(cls.fullClassName), parametersOptional);
            indent.right();
            cls.fields.stream().forEach(x -> {
                if (!x.isPrimitive()) {
                    if (x.required) {
                        out.format("%sthis.%s = %s.checkNotNull(%s);\n", indent, x.fieldName,
                                imports.add(Preconditions.class), x.fieldName, x.fieldName);
                    } else {
                        out.format("%sthis.%s = %s.checkNotNull(%s).orElse(null);\n", indent, x.fieldName,
                                imports.add(Preconditions.class), x.fieldName, x.fieldName);
                    }
                } else {
                    out.format("%sthis.%s = %s;\n", indent, x.fieldName, x.fieldName);
                }
            });
            indent.left();
            out.format("%s}\n", indent);
        }
    }

    private static void writeGetters(PrintStream out, Imports imports, Indent indent, Cls cls) {
        cls.fields.forEach(f -> {
            out.format("\n%spublic %s %s() {\n", indent, f.resolvedType(imports), f.fieldName);
            indent.right();
            if (!f.required && !cls.unwrapSingleField()) {
                out.format("%sreturn %s.ofNullable(%s);\n", indent, imports.add(Optional.class), f.fieldName);
            } else {
                out.format("%sreturn %s;\n", indent, f.fieldName);
            }
            indent.left();
            out.format("%s}\n", indent);
        });
    }

    private static void writeMemberClasses(PrintStream out, Imports imports, Indent indent, Cls cls) {
        cls.classes.forEach(c -> writeClass(out, imports, indent, c));
    }

    private static String resolvedTypeNullable(Field f, Imports imports) {
        if (f.isArray) {
            return toList(f, imports);
        }
        if (f.required) {
            return imports.add(toPrimitive(f.fullClassName));
        } else {
            return imports.add(f.fullClassName);
        }
    }

    private static String toList(Field f, Imports imports) {
        return imports.add(List.class) + "<" + imports.add(f.fullClassName) + ">";
    }

    private static String resolvedType(Field f, Imports imports) {
        if (f.isArray) {
            return toList(f, imports);
        } else if (f.required) {
            return imports.add(toPrimitive(f.fullClassName));
        } else {
            return imports.add(Optional.class) + "<" + imports.add(f.fullClassName) + ">";
        }
    }

    static boolean isEnum(Schema<?> schema) {
        return schema.getEnum() != null && !schema.getEnum().isEmpty();
    }

    private static boolean isRef(Schema<?> schema) {
        return schema.get$ref() != null;
    }

    private static boolean isObject(Schema<?> schema) {
        return (schema.getType() == null && schema.getProperties() != null) || "object".equals(schema.getType());
    }

    private static boolean isArray(Schema<?> schema) {
        return schema instanceof ArraySchema;
    }

    static boolean isOneOf(Schema<?> schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        ComposedSchema sch = (ComposedSchema) schema;
        return sch.getOneOf() != null && !sch.getOneOf().isEmpty();
    }

    static boolean isAnyOf(Schema<?> schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        ComposedSchema sch = (ComposedSchema) schema;
        return sch.getAnyOf() != null && !sch.getAnyOf().isEmpty();
    }

    private static boolean isPrimitive(Schema<?> schema) {
        String type = schema.getType();
        return type != null && !"array".equals(type) && !"object".equals(type);
    }

    private static boolean isMap(Schema<?> schema) {
        return schema instanceof MapSchema;
    }

    public static boolean MAP_INTEGER_TO_BIG_INTEGER = false;

    private static ClassType classType(Schema<?> schema) {
        if (schema instanceof ComposedSchema
                && ((((ComposedSchema) schema).getOneOf() != null) || ((ComposedSchema) schema).getAnyOf() != null)) {
            return ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED;
        } else if (schema.getEnum() != null) {
            return ClassType.ENUM;
        } else {
            return ClassType.CLASS;
        }
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
                return MAP_INTEGER_TO_BIG_INTEGER ? BigInteger.class : Long.class;
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

    private static String toPrimitive(String c) {
        if (c.equals(Integer.class.getCanonicalName())) {
            return "int";
        } else if (c.equals(Long.class.getCanonicalName())) {
            return "long";
        } else if (c.equals(Float.class.getCanonicalName())) {
            return "float";
        } else if (c.equals(Boolean.class.getCanonicalName())) {
            return "boolean";
        } else if (c.equals(BigInteger.class.getCanonicalName())) {
            return c;
        } else {
            return c;
        }
    }
}
