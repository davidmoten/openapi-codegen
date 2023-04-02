package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.runtime.internal.Util.toPrimitive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import org.davidmoten.oa3.codegen.generator.internal.ByteArrayPrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Mutable;
import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.runtime.internal.PolymorphicDeserializer;
import org.davidmoten.oa3.codegen.runtime.internal.PolymorphicType;
import org.davidmoten.oa3.codegen.runtime.internal.Util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class Generator {

    private final Definition definition;

    private static String version = readVersion();

    public Generator(Definition definition) {
        this.definition = definition;
    }

    private static String readVersion() {
        Properties p = new Properties();
        try (InputStream in = Generator.class.getResourceAsStream("/application.properties")) {
            p.load(in);
            return p.get("groupId") + ":" + p.get("artifactId") + p.get("version");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void generate() {

        // Names object for each Packages object
        Names names = new Names(definition);

        // generate methods on singleton client object in client package
//        writeClientClass(names);

        // generate model classes for schema definitions
        writeSchemaClasses(definition, names);

        writeGlobalsClass(names);

    }

    private void writeGlobalsClass(Names names) {
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        Indent indent = new Indent();
        String fullClassName = names.globalsFullClassName();
        Imports imports = new Imports(fullClassName);
        out.format("package %s;\n", Names.pkg(fullClassName));
        out.format("\nIMPORTS_HERE");
        out.println();
        addGeneratedAnnotation(out, imports, indent);
        out.format("public final class %s {\n", Names.simpleClassName(fullClassName));
        indent.right();
        out.format("\n%sprivate static %s config = %s.builder().build();\n", indent, imports.add(Config.class),
                imports.add(Config.class));
        out.format("\n%spublic static void setConfig(%s configuration) {\n", indent, imports.add(Config.class));
        indent.right();
        out.format("%sconfig = configuration;\n", indent);
        indent.left();
        out.format("%s}\n", indent);
        out.format("\n%spublic static %s config() {\n", indent, imports.add(Config.class));
        indent.right();
        out.format("%sreturn config;\n", indent);
        indent.left();
        out.format("%s}\n", indent);
        indent.left();
        out.format("%s}\n", indent);
        System.out.println("////////////////////////////////////////////////");
        String content = out.text().replace("IMPORTS_HERE", imports.toString());
//        System.out.println(content);
        out.close();
        File file = names.fullClassNameToJavaFile(fullClassName);
        file.getParentFile().mkdirs();
        try {
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeSchemaClasses(Definition definition, Names names) {
//        Map<String, List<String>> fullClassNameInterfaces = new HashMap<>();
//        names.api().getComponents().getSchemas().entrySet().forEach(entry -> {
//            Visitor v = new Visitor();
//            Apis.visitSchemas(entry.getKey(), entry.getValue(), v);
//        });
        List<MyVisitor.Result> results = new ArrayList<>();
        names.api().getComponents().getSchemas().entrySet().forEach(entry -> {
            MyVisitor v = new MyVisitor(names);
            Apis.visitSchemas(entry.getKey(), entry.getValue(), v);
            results.add(v.result());
        });
        names.api().getPaths().forEach((pathName, pathItem) -> {
            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                if (operation.getResponses() != null) {
                    operation.getResponses().forEach((statusCode, response) -> {
                        String prefix = "Path " + pathName + " Method " + httpMethod + " StatusCode " + statusCode;
                        visitResponse(names, results, response, prefix);
                    });
                }
                if (operation.getParameters() != null) {
                    operation.getParameters().forEach(parameter -> {
                        String prefix = "Path " + pathName + " Method " + httpMethod;
                        visitParameter(names, results, parameter, prefix);
                    });
                }

            });
        });
        names.api().getPaths().forEach((pathName, pathItem) -> {
            if (pathItem.getParameters() != null) {
                pathItem.getParameters().forEach(parameter -> {
                    String prefix = "Path " + pathName;
                    visitParameter(names, results, parameter, prefix);
                });
            }
        });
        names.api().getPaths().forEach((pathName, pathItem) -> {
            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                String prefix = "Path" + pathName + " Method " + httpMethod;
                if (operation.getRequestBody() != null) {
                    if (operation.getRequestBody().getContent() != null) {
                        operation.getRequestBody().getContent().forEach((mimeType, mediaType) -> {
                            MyVisitor v = new MyVisitor(names);
                            Apis.visitSchemas(prefix + " RequestBody " + " Content " + mimeType, mediaType.getSchema(),
                                    v);
                            results.add(v.result());
                        });
                    }
                }
            });
        });
        names.api().getComponents().getParameters().forEach((parameterName, parameter) -> {
            if (parameter.getContent() != null) {
                parameter.getContent().forEach((mimeType, mediaType) -> {
                    MyVisitor v = new MyVisitor(names);
                    Apis.visitSchemas("Parameter " + parameterName + " Content " + mimeType, mediaType.getSchema(), v);
                    results.add(v.result());
                });
            }
        });
        names.api().getComponents().getResponses().forEach((responseName, response) -> {
            if (response.getContent() != null) {
                response.getContent().forEach((mimeType, mediaType) -> {
                    MyVisitor v = new MyVisitor(names);
                    Apis.visitSchemas("Response " + responseName + " Content " + mimeType, mediaType.getSchema(), v);
                    results.add(v.result());
                });
            }
        });

        Map<String, Set<Cls>> fullClassNameInterfaces = new HashMap<>();
        for (MyVisitor.Result result : results) {
            findFullClassNameInterfaces(result.cls, fullClassNameInterfaces);
        }
        for (MyVisitor.Result result : results) {
            ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
            Indent indent = new Indent();
            out.format("package %s;\n", result.cls.pkg());
            out.format("\nIMPORTS_HERE");
            writeClass(out, result.imports, indent, result.cls, fullClassNameInterfaces, names);
            System.out.println("////////////////////////////////////////////////");
            String content = out.text().replace("IMPORTS_HERE", result.imports.toString());
            System.out.println(content);
            out.close();
            File file = names.schemaNameToJavaFile(result.name);
            file.getParentFile().mkdirs();
            try {
                Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static void visitParameter(Names names, List<MyVisitor.Result> results, Parameter parameter,
            String prefix) {
        if (parameter.getContent() != null) {
            parameter.getContent().forEach((mimeType, mediaType) -> {
                MyVisitor v = new MyVisitor(names);
                Apis.visitSchemas(prefix + " Parameter " + parameter.getName() + " Content " + mimeType,
                        mediaType.getSchema(), v);
                results.add(v.result());
            });
        }
    }

    private static void visitResponse(Names names, List<MyVisitor.Result> results, ApiResponse response,
            String prefix) {
        if (response.getContent() != null) {
            response.getContent().forEach((mimeType, mediaType) -> {
                MyVisitor v = new MyVisitor(names);
                Apis.visitSchemas(prefix + " Response Content " + mimeType, mediaType.getSchema(), v);
                results.add(v.result());
            });
        }
    }

    private static void findFullClassNameInterfaces(Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces) {
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            cls.fields.forEach(x -> {
                Set<Cls> list = fullClassNameInterfaces.get(x.fullClassName);
                if (list == null) {
                    list = new HashSet<>();
                    fullClassNameInterfaces.put(x.fullClassName, list);
                }
                list.add(cls);
            });
        }
    }

    private static final class Cls {
        String fullClassName;
        ClassType classType;
        List<Field> fields = new ArrayList<>();
        List<EnumMember> enumMembers = new ArrayList<>();
        List<Cls> classes = new ArrayList<>();
        String description = null;
        Discriminator discriminator = null;
        String enumFullType;
        private int num = 0;
        private Set<String> fieldNames = new HashSet<String>();
        boolean topLevel = false;
        boolean hasProperties = false;
        PolymorphicType polymorphicType;

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

        String fieldName(Field f) {
            if (unwrapSingleField()) {
                return "value";
            } else {
                return f.fieldName;
            }
        }

        void addField(String fullType, String name, String fieldName, boolean required, boolean isArray) {
            fields.add(new Field(fullType, name, fieldName, required, isArray, Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false,
                    false, Encoding.DEFAULT));
        }

        void addField(String fullType, String name, String fieldName, boolean required, boolean isArray,
                Optional<Integer> minItems, Optional<Integer> maxItems, Optional<Integer> minLength,
                Optional<Integer> maxLength, Optional<String> pattern, Optional<BigDecimal> min,
                Optional<BigDecimal> max, boolean exclusiveMin, boolean exclusiveMax, Encoding encoding) {
            fields.add(new Field(fullType, name, fieldName, required, isArray, minItems, maxItems, minLength, maxLength,
                    pattern, min, max, exclusiveMin, exclusiveMax, encoding));
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
        final Optional<BigDecimal> min;
        final Optional<BigDecimal> max;
        final boolean isArray; // if a List to be used to represent
        final Encoding encoding;
        final boolean exclusiveMin;
        final boolean exclusiveMax;
        final Optional<Integer> minItems;
        final Optional<Integer> maxItems;

        public Field(String fullClassName, String name, String fieldName, boolean required, boolean isArray,
                Optional<Integer> minItems, Optional<Integer> maxItems, Optional<Integer> minLength,
                Optional<Integer> maxLength, Optional<String> pattern, Optional<BigDecimal> min,
                Optional<BigDecimal> max, boolean exclusiveMin, boolean exclusiveMax, Encoding encoding) {
            this.fullClassName = fullClassName;
            this.name = name;
            this.fieldName = fieldName;
            this.required = required;
            this.isArray = isArray;
            this.minItems = minItems;
            this.maxItems = maxItems;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.pattern = pattern;
            this.exclusiveMin = exclusiveMin;
            this.exclusiveMax = exclusiveMax;
            this.encoding = encoding;
            this.min = min;
            this.max = max;
        }

        public String fieldName(Cls cls) {
            return cls.fieldName(this);
        }

        public String resolvedType(Imports imports) {
            return Generator.resolvedType(this, imports);
        }

        public String resolvedTypeNullable(Imports imports) {
            return Generator.resolvedTypeNullable(this, imports);
        }

        public boolean isPrimitive() {
            return required && PRIMITIVE_CLASS_NAMES.contains(toPrimitive(fullClassName));
        }

        public boolean isOctets() {
            return encoding == Encoding.OCTET;
        }

        @Override
        public String toString() {
            return "Field [fullClassName=" + fullClassName + ", name=" + name + ", fieldName=" + fieldName
                    + ", required=" + required + ", minLength=" + minLength + ", maxLength=" + maxLength + "]";
        }

        public boolean isByteArray() {
            return fullClassName.equals("byte[]");
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
        private Result result;

        public MyVisitor(Names names) {
            this.names = names;
        }

        @Override
        public void startSchema(ImmutableList<SchemaWithName> schemaPath) {
            SchemaWithName last = schemaPath.last();
            Schema<?> schema = last.schema;
            final Cls cls = new Cls();
            cls.description = schema.getDescription();
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
            Optional<Integer> minItems = isArray ? Optional.ofNullable(schemaPath.secondLast().schema.getMinItems())
                    : Optional.empty();
            Optional<Integer> maxItems = isArray ? Optional.ofNullable(schemaPath.secondLast().schema.getMaxItems())
                    : Optional.empty();
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
                    handleObject(schemaPath, last, schema, cls, isArray, previous, fieldName);
                } else if (isOneOf(schema) || isAnyOf(schema)) {
                    handleOneOrAnyOf(schemaPath, cls, names, previous, fieldName, isArray);
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
                    String fieldName = schemaPath.size() == 1 ? "value" : current.nextFieldName(last.name);
                    boolean required = fieldIsRequired(schemaPath);
                    final Encoding encoding;
                    if ("binary".equals(schema.getFormat())) {
                        encoding = Encoding.OCTET;
                    } else {
                        encoding = Encoding.DEFAULT;
                    }
                    Optional<BigDecimal> min = Optional.ofNullable(schema.getMinimum());
                    Optional<BigDecimal> max = Optional.ofNullable(schema.getMaximum());
                    boolean exclusiveMin = orElse(schema.getExclusiveMinimum(), false);
                    boolean exclusiveMax = orElse(schema.getExclusiveMaximum(), false);
                    current.addField(fullClassName, last.name, fieldName, required, isArray, minItems, maxItems,
                            minLength, maxLength, pattern, min, max, exclusiveMin, exclusiveMax, encoding);
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
                    this.result = new Result(cls, imports, schemaPath.first().name);
                }
            }
        }

        public Result result() {
            return result;
        }

        public static final class Result {
            final Cls cls;
            final Imports imports;
            final String name;

            public Result(Cls cls, Imports imports, String name) {
                this.cls = cls;
                this.imports = imports;
                this.name = name;
            }
        }
    }

    private static void handleObject(ImmutableList<SchemaWithName> schemaPath, SchemaWithName last, Schema<?> schema,
            final Cls cls, boolean isArray, Optional<Cls> previous, final Optional<String> fieldName) {
        cls.classType = ClassType.CLASS;
        cls.hasProperties = isObject(schema);
        boolean required = fieldIsRequired(schemaPath);
        previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required, isArray));
    }

    private enum Encoding {
        DEFAULT, OCTET;
    }

    private static void writeClass(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces, Names names) {
        writeClassDeclaration(out, imports, indent, cls, fullClassNameInterfaces);
        indent.right();
        writeEnumMembers(out, imports, indent, cls);
        if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED
                || cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeOneOrAnyOfClassContent(out, imports, indent, cls, names);
        } else {
            writeFields(out, imports, indent, cls);
            writeConstructor(out, imports, indent, cls, fullClassNameInterfaces, names);
            writeGetters(out, imports, indent, cls, fullClassNameInterfaces);
        }
        writeEnumCreator(out, imports, indent, cls);
        writeMemberClasses(out, imports, indent, cls, fullClassNameInterfaces, names);
        indent.left();
        closeParen(out, indent);
    }

    private static void writeEnumCreator(PrintWriter out, Imports imports, Indent indent, Cls cls) {
        if (cls.classType == ClassType.ENUM) {
//            @JsonCreator
//            public static SubjectIndicator fromValue(String value) {
//              for (SubjectIndicator b : SubjectIndicator.values()) {
//                if (b.value.equals(value)) {
//                  return b;
//                }
//              }
//              throw new IllegalArgumentException("Unexpected value '" + value + "'");
//            }
            String simpleClassName = Names.simpleClassName(cls.fullClassName);
            out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
            out.format("%spublic static %s fromValue(%s value) {\n", indent, simpleClassName,
                    imports.add(Object.class));
            indent.right();
            out.format("%sfor (%s x: %s.values()) {\n", indent, simpleClassName, simpleClassName);
            indent.right();
            // be careful because x.value can be primitive
            out.format("%sif (value.equals(x.value)) {\n", indent);
            indent.right();
            out.format("%sreturn x;\n", indent);
            indent.left();
            closeParen(out, indent);
            indent.left();
            closeParen(out, indent);
            out.format("%sthrow new %s(\"unexpected enum value: '\" + value + \"'\");\n", indent,
                    imports.add(IllegalArgumentException.class));
            indent.left();
            closeParen(out, indent);
        }
    }

    private static void writeClassDeclaration(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        final String modifier;
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED || cls.classType == ClassType.ENUM) {
            modifier = "";
        } else {
            modifier = cls.topLevel ? "final " : "static final ";
        }
        Set<Cls> interfaces = fullClassNameInterfaces.get(cls.fullClassName);
        final String implemented;
        if (interfaces == null || interfaces.isEmpty()) {
            implemented = "";
        } else {
            implemented = " implements "
                    + interfaces.stream().map(x -> imports.add(x.fullClassName)).collect(Collectors.joining(", "));
        }
        if (cls.description != null) {
            System.out.println(cls.description);
            Javadoc.printJavadoc(out, indent, cls.description);
        }
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.format("\n%s@%s(use = %s.NAME, property = \"%s\", include = %s.EXISTING_PROPERTY, visible = true)\n",
                    indent, imports.add(JsonTypeInfo.class), imports.add(Id.class), cls.discriminator.propertyName,
                    imports.add(As.class));
            indent.right().right();
            String types = cls.fields.stream()
                    .map(x -> String.format("\n%s@%s(value = %s.class, name = \"%s\")", indent, imports.add(Type.class),
                            imports.add(x.fullClassName),
                            cls.discriminator.discriminatorValueFromFullClassName(x.fullClassName)))
                    .collect(Collectors.joining(", "));
            indent.left().left();
            out.format("%s@%s({%s})\n", indent, imports.add(JsonSubTypes.class), types);
        } else if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED) {
            writeOneOfDeserializerAnnotation(out, imports, indent, cls);
        } else {
            out.println();
        }
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.format("%s@%s(%s.NON_NULL)\n", indent, imports.add(JsonInclude.class), imports.add(Include.class));
            writeAutoDetectAnnotation(out, imports, indent);
        }
        if (cls.topLevel) {
            addGeneratedAnnotation(out, imports, indent);
        }
        out.format("%spublic %s%s %s%s {\n", indent, modifier, cls.classType.word(), cls.simpleName(), implemented);
    }

    private static void addGeneratedAnnotation(PrintWriter out, Imports imports, Indent indent) {
        out.format("%s@%s(value = \"%s\")\n", indent, imports.add(Generated.class), version);
    }

    private static void writeOneOfDeserializerAnnotation(PrintWriter out, Imports imports, Indent indent, Cls cls) {
        out.format("\n%s@%s(using = %s.Deserializer.class)\n", indent, imports.add(JsonDeserialize.class),
                cls.simpleName());
    }

    private static void writeAutoDetectAnnotation(PrintWriter out, Imports imports, Indent indent) {
        out.format("%s@%s(fieldVisibility = %s.ANY, creatorVisibility = %s.ANY)\n", indent,
                imports.add(JsonAutoDetect.class), imports.add(Visibility.class), imports.add(Visibility.class));
    }

    private static void writeEnumMembers(PrintWriter out, Imports imports, Indent indent, Cls cls) {
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
            return isPrimitive(last.schema) || isRef(last.schema);
        } else {
            return contains(schemaPath.secondLast().schema.getRequired(), last.name);
        }
    }

    private static void handleOneOrAnyOf(ImmutableList<SchemaWithName> schemaPath, Cls cls, Names names,
            Optional<Cls> previous, Optional<String> fieldName, boolean isArray) {
        SchemaWithName last = schemaPath.last();
        cls.polymorphicType = isOneOf(last.schema) ? PolymorphicType.ONE_OF : PolymorphicType.ANY_OF;
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
        boolean required = fieldIsRequired(schemaPath);
        previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required, isArray));
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

        public String discriminatorValueFromFullClassName(String fullClassName) {
            String value = fullClassNameToPropertyValue.get(fullClassName);
            if (value == null) {
                // TODO review using simple class name for value because collision risk
                return Names.simpleClassName(fullClassName);
            } else {
                return value;
            }
        }
    }

    private static void handleEnum(Schema<?> schema, Cls cls) {
        cls.classType = ClassType.ENUM;
        Class<?> valueCls = toClass(schema.getType(), schema.getFormat());
        cls.enumFullType = valueCls.getCanonicalName();
        Map<String, String> map = Names.getEnumValueToIdentifierMap(schema.getEnum());
        Set<String> used = new HashSet<>();
        for (Object o : schema.getEnum()) {
            if (!used.contains(o.toString())) {
                cls.enumMembers.add(new EnumMember(map.get(o.toString()), o));
                used.add(o.toString());
            }
        }
        cls.addField(cls.enumFullType, "value", "value", true, false);
    }

    private static <T> boolean contains(Collection<? extends T> collection, T t) {
        return collection != null && t != null && collection.contains(t);
    }

    private static void writeOneOrAnyOfClassContent(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Names names) {
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.format("\n%s%s %s();\n", indent, imports.add(String.class), cls.discriminator.fieldName);
        } else {
            out.format("\n%s@%s\n", indent, imports.add(JsonValue.class));
            out.format("%sprivate final %s %s;\n", indent, imports.add(Object.class), "value");

            // add constructor for each member of the oneOf (fieldTypes)
            out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
            out.format("%sprivate %s(%s value) {\n", indent, cls.simpleName(), imports.add(Object.class));
            out.format("%sthis.value = %s.checkNotNull(value, \"value\");\n", indent.right(),
                    imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class));
            out.format("%s}\n", indent.left());
            cls.fields.forEach(f -> {
                String className = toPrimitive(f.fullClassName);
                out.format("\n%spublic %s(%s value) {\n", indent, cls.simpleName(), imports.add(className));
                indent.right();
                if (Names.isPrimitiveFullClassName(className)) {
                    out.format("%sthis.value = value;\n", indent);
                } else {
                    out.format("%sthis.value = %s.checkNotNull(value, \"value\");\n", indent,
                            imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class));
                }
                out.format("%s}\n", indent.left());
            });

            out.format("\n%spublic Object value() {\n", indent);
            out.format("%sreturn value;\n", indent.right());
            out.format("%s}\n", indent.left());

            out.format("\n%s@%s(\"serial\")\n", indent, imports.add(SuppressWarnings.class));
            out.format("%spublic static final class Deserializer extends %s<%s> {\n", indent,
                    imports.add(PolymorphicDeserializer.class), cls.simpleName());
            indent.right();
            out.format("\n%spublic Deserializer() {\n", indent);
            indent.right();
            String classes = cls.fields.stream().map(x -> imports.add(toPrimitive(x.fullClassName)) + ".class")
                    .collect(Collectors.joining(", "));
            out.format("%ssuper(%s.config(), %s.%s, %s.class, %s);\n", indent,
                    imports.add(names.globalsFullClassName()), imports.add(PolymorphicType.class),
                    cls.polymorphicType.name(), cls.simpleName(), classes);
            indent.left();
            closeParen(out, indent);
            indent.left();
            closeParen(out, indent);
        }
    }

    private static void writeFields(PrintWriter out, Imports imports, Indent indent, Cls cls) {
        if (!cls.fields.isEmpty()) {
            out.println();
        }
        Mutable<Boolean> first = Mutable.create(true);
        cls.fields.forEach(f -> {
            if (!first.value) {
                out.println();
            }
            first.value = false;
            if (cls.unwrapSingleField()) {
                out.format("%s@%s\n", indent, imports.add(JsonValue.class));
            } else {
                out.format("%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class), f.name);
            }

            final String fieldType;
            if (f.encoding == Encoding.OCTET) {
                fieldType = imports.add(String.class);
            } else {
                fieldType = f.resolvedTypeNullable(imports);
            }
            out.format("%sprivate final %s %s;\n", indent, fieldType, cls.fieldName(f));
        });
    }

    private static void writeConstructor(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces, Names names) {
        // this code will write one public constructor or one private and one public.
        // The private one is to be annotated
        // with JsonCreator for use by Jackson.

        // TODO javadoc
        indent.right().right();
        final String parametersNullable;
        if (cls.unwrapSingleField()) {
            parametersNullable = cls.fields.stream()
                    .map(x -> String.format("\n%s%s %s", indent, x.resolvedTypeNullable(imports), x.fieldName(cls)))
                    .collect(Collectors.joining(","));
        } else {
            parametersNullable = cls.fields
                    .stream().map(x -> String.format("\n%s@%s(\"%s\") %s %s", indent, imports.add(JsonProperty.class),
                            x.name, x.resolvedTypeNullable(imports), x.fieldName(cls)))
                    .collect(Collectors.joining(","));
        }
        indent.left().left();
        Set<Cls> interfaces = orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());

        if (cls.classType != ClassType.ENUM) {
            out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
        } else {
            out.println();
        }
        boolean hasOptional = !cls.unwrapSingleField() && cls.fields.stream().anyMatch(f -> !f.required);
        boolean hasBinary = cls.fields.stream().anyMatch(Field::isOctets);
        // if has optional or other criteria then write a private constructor with
        // nullable parameters
        // and a public constructor with Optional parameters
        final String visibility = cls.classType == ClassType.ENUM || hasOptional || hasBinary || !interfaces.isEmpty()
                ? "private"
                : "public";
        out.format("%s%s %s(%s) {\n", indent, visibility, Names.simpleClassName(cls.fullClassName), parametersNullable);
        indent.right();
        ifValidate(out, indent, imports, names, //
                out2 -> cls.fields.stream().forEach(x -> {
                    if (!x.isPrimitive() && x.required && !visibility.equals("private")) {
                        out2.format("%s%s.checkNotNull(%s, \"%s\");\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                x.fieldName(cls), x.fieldName(cls));
                    }
                    validateMore(out2, imports, indent, cls, x, false);
                }));

        // assign
        cls.fields.stream().forEach(x -> {
            assignField(out, indent, cls, x);
        });
        indent.left();
        closeParen(out, indent);
        if (hasOptional || !interfaces.isEmpty() || hasBinary) {
            indent.right().right();
            String parametersOptional = cls.fields.stream().filter(
                    x -> !interfaces.stream().map(y -> y.discriminator.propertyName).anyMatch(y -> x.name.equals(y)))
                    .map(x -> String.format("\n%s%s %s", indent, x.resolvedType(imports), x.fieldName(cls)))
                    .collect(Collectors.joining(","));
            indent.left().left();
            out.format("\n%spublic %s(%s) {\n", indent, Names.simpleClassName(cls.fullClassName), parametersOptional);
            indent.right();
            // validate
            ifValidate(out, indent, imports, names, //
                    out2 -> cls.fields.stream().forEach(x -> {
                        Optional<Discriminator> disc = interfaces.stream()
                                .filter(y -> x.name.equals(y.discriminator.propertyName)).map(y -> y.discriminator)
                                .findFirst();
                        if (!disc.isPresent() && (x.isOctets() || !x.isPrimitive() && !x.isByteArray())) {
                            out2.format("%s%s.checkNotNull(%s, \"%s\");\n", indent,
                                    imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                    x.fieldName(cls), x.fieldName(cls));
                            validateMore(out2, imports, indent, cls, x, !x.required);
                        }
//                        f.pattern.ifPresent(
//                                x -> out.format("%s@%s(regexp = \"%s\")\n", indent, imports.add(Pattern.class), x));
                    }));

            // assign
            cls.fields.stream().forEach(x -> {
                Optional<Discriminator> disc = interfaces.stream()
                        .filter(y -> x.name.equals(y.discriminator.propertyName)).map(y -> y.discriminator).findFirst();
                if (disc.isPresent()) {
                    out.format("%sthis.%s = \"%s\";\n", indent, x.fieldName(cls),
                            disc.get().discriminatorValueFromFullClassName(cls.fullClassName));
                } else if (!x.isPrimitive() && !x.isByteArray()) {
                    if (x.required) {
                        assignField(out, indent, cls, x);
                    } else {
                        out.format("%sthis.%s = %s.orElse(null);\n", indent, x.fieldName(cls), x.fieldName(cls));
                    }
                } else if (x.isOctets()) {
                    out.format("%sthis.%s = %s.encodeOctets(%s);\n", indent, x.fieldName(cls), imports.add(Util.class),
                            x.fieldName(cls));
                } else {
                    assignField(out, indent, cls, x);
                }
            });
            indent.left();
            closeParen(out, indent);
        }
    }

    private static void validateMore(PrintWriter out, Imports imports, Indent indent, Cls cls, Field x,
            boolean useGet) {
        String raw = x.fieldName(cls) + (useGet ? ".get()" : "");
        if (x.minLength.isPresent()) {
            out.format("%s%s.checkMinLength(%s, %s, \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class), raw,
                    x.minLength.get(), x.fieldName(cls));
        }
        if (x.maxLength.isPresent()) {
            out.format("%s%s.checkMaxLength(%s, %s, \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class), raw,
                    x.maxLength.get(), x.fieldName(cls));
        }
        if (x.pattern.isPresent()) {
            out.format("%s%s.checkMatchesPattern(%s, \"%s\", \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class), raw, x.pattern.get(),
                    x.fieldName(cls));
        }
        if (x.min.isPresent()) {
            out.format("%s%s.checkMinimum(%s, \"%s\", \"%s\", %s);\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class), raw,
                    x.min.get().toString(), x.fieldName(cls), x.exclusiveMin);
        }
        if (x.max.isPresent()) {
            out.format("%s%s.checkMaximum(%s, \"%s\", \"%s\", %s);\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class), raw,
                    x.max.get().toString(), x.fieldName(cls), x.exclusiveMax);
        }
        if (x.isArray && x.minItems.isPresent()) {
            out.format("%s%s.checkMinSize(%s, %s, \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class), x.fieldName(cls),
                    x.minItems.get(), x.fieldName(cls));
        }
        if (x.isArray && x.maxItems.isPresent()) {
            out.format("%s%s.checkMaxSize(%s, %s, \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class), x.fieldName(cls),
                    x.maxItems.get(), x.fieldName(cls));
        }
    }

    private static void ifValidate(PrintWriter out, Indent indent, Imports imports, Names names,
            Consumer<PrintWriter> r) {
        ByteArrayPrintWriter b = ByteArrayPrintWriter.create();
        indent.right();
        r.accept(b);
        indent.left();
        b.close();
        String text = b.text();
        if (text.isEmpty()) {
            return;
        } else {
            out.format("%sif (%s.config().validateInConstructor()) {\n", indent,
                    imports.add(names.globalsFullClassName()));
            out.print(text);
            closeParen(out, indent);
        }
    }

    private static PrintWriter closeParen(PrintWriter out, Indent indent) {
        return out.format("%s}\n", indent);
    }

    private static void assignField(PrintWriter out, Indent indent, Cls cls, Field x) {
        out.format("%sthis.%s = %s;\n", indent, x.fieldName(cls), x.fieldName(cls));
    }

    private static <T> T orElse(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static void writeGetters(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        Set<Cls> interfaces = orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());
        cls.fields.forEach(f -> {
            if (interfaces.stream().anyMatch(c -> c.discriminator.propertyName.equals(f.name))) {
                out.format("\n%s@%s\n", indent, imports.add(Override.class));
            } else {
                out.println();
            }
            out.format("%spublic %s %s() {\n", indent, f.resolvedType(imports), f.fieldName(cls));
            indent.right();
            if (!f.isOctets() && !f.required && !cls.unwrapSingleField()) {
                out.format("%sreturn %s.ofNullable(%s);\n", indent, imports.add(Optional.class), f.fieldName(cls));
            } else if (f.isOctets()) {
                out.format("%sreturn %s.decodeOctets(%s);\n", indent, imports.add(Util.class), f.fieldName(cls));
            } else {
                out.format("%sreturn %s;\n", indent, f.fieldName(cls));
            }
            indent.left();
            closeParen(out, indent);
        });
    }

    private static void writeMemberClasses(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces, Names names) {
        cls.classes.forEach(c -> writeClass(out, imports, indent, c, fullClassNameInterfaces, names));
    }

    private static String resolvedTypeNullable(Field f, Imports imports) {
        if (f.encoding == Encoding.OCTET) {
            return imports.add(String.class);
        } else if (f.isArray) {
            return toList(f, imports);
        } else if (f.required) {
            return imports.add(toPrimitive(f.fullClassName));
        } else {
            return imports.add(f.fullClassName);
        }
    }

    private static String toList(Field f, Imports imports) {
        return imports.add(List.class) + "<" + imports.add(f.fullClassName) + ">";
    }

    private static String resolvedType(Field f, Imports imports) {
        if (f.isOctets()) {
            return "byte[]";
        } else if (f.isArray) {
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
                return LocalDate.class;
            } else if ("time".equals(format)) {
                return OffsetTime.class;
            } else if ("byte".equals(format)) {
                return byte[].class;
            } else if ("binary".equals(format)) {
                return byte[].class;
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
                return Definition.MAP_INTEGER_TO_BIG_INTEGER ? BigInteger.class : Long.class;
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
            throw new RuntimeException("unexpected type and format: " + type + ", " + format);
        }
    }

}
