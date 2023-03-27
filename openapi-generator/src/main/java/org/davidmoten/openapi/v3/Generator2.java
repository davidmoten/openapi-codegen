package org.davidmoten.openapi.v3;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.davidmoten.openapi.v3.internal.ByteArrayPrintStream;
import org.davidmoten.openapi.v3.runtime.OneOfDeserializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
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

//        superClasses(api).entrySet().forEach(x -> System.out.println(x.getKey().get$ref() + "->"
//                + x.getValue().stream().map(y -> y.toString()).collect(Collectors.toList())));
    }

//    private static void writeClientClass(Names names) {
//        String className = names.clientClassName();
//        File file = names.clientClassJavaFile();
//        JavaClassWriter.write(file, className, ClassType.CLASS, (indent, imports, p) -> {
//            p.format("%s// TODO\n", indent);
//        });
//    }

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
        List<String> interfaceMethods = new ArrayList<>();
        String enumFullType;
        private int num = 0;
        private Set<String> fieldNames = new HashSet<String>();

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

        void addField(String fullType, String name, String fieldName, boolean required) {
            fields.add(new Field(fullType, name, fieldName, required));
        }

        public String pkg() {
            return Names.pkg(fullClassName);
        }

        public String simpleName() {
            return Names.simpleClassName(fullClassName);
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
        INTERFACE("interface"), CLASS("class"), ENUM("enum"), ONE_OR_ANY_OF("class");

        private final String word;

        ClassType(String word) {
            this.word = word;
        }

        String word() {
            return word;
        }
    }

    private static final Set<String> PRIMITIVE_CLASS_NAMES = Sets.newHashSet("int", "long", "byte", "float", "double",
            "boolean");

    private final static class Field {
        private String fullClassName;
        final String name;
        final String fieldName;
        final boolean required;

        public Field(String fullClassName, String name, String fieldName, boolean required) {
            this.fullClassName = fullClassName;
            this.name = name;
            this.fieldName = fieldName;
            this.required = required;
        }

        public String resolvedType(Imports imports) {
            return resolveType(this, imports);
        }

        public boolean isPrimitive() {
            return required && PRIMITIVE_CLASS_NAMES.contains(toPrimitive(fullClassName));
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
            }
            if (Apis.isComplexSchema(schema) || isEnum(schema) || isOneOf(schema) || isAnyOf(schema)) {
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
                    boolean required = fieldIsRequired(schemaPath, last);
                    previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required));
                } else if (isOneOf(schema) || isAnyOf(schema)) {
                    handleOneOrAnyOf(last, schema, cls);
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
                if (isPrimitive(schema.getType())) {
                    Class<?> c = toClass(schema.getType(), schema.getFormat());
                    String fieldName = current.nextFieldName(last.name);
                    boolean required = fieldIsRequired(schemaPath, last);
                    current.addField(c.getCanonicalName(), last.name, fieldName, required);
                } else if (isRef(schema)) {
                    String ref = schema.get$ref();
                    final String fullClassName;
                    if (!ref.startsWith("#")) {
                        fullClassName = names.externalRefClassName(ref);
                    } else {
                        String schemaName = ref.substring(ref.lastIndexOf("/") + 1);
                        fullClassName = names.schemaNameToClassName(schemaName);
                    }
                    String fieldName = current.nextFieldName(last.name);
                    boolean required = fieldIsRequired(schemaPath, last);
                    current.addField(fullClassName, last.name, fieldName, required);
                }
            }
        }

        @Override
        public void finishSchema(ImmutableList<SchemaWithName> schemaPath) {
            final Cls cls = stack.peek();
            if (Apis.isComplexSchema(schemaPath.last().schema) || isEnum(schemaPath.last().schema)) {
                stack.pop();
                if (stack.isEmpty()) {
                    Indent indent = new Indent();
                    out.format("package %s;\n", cls.pkg());
                    out.format("\nIMPORTS_HERE");
                    writeClass(out, imports, indent, cls);
                    System.out.println("////////////////////////////////////////////////");
                    System.out.println(out.text().replace("IMPORTS_HERE", imports.toString()));
                    out.close();
                }
            }
        }
    }

    private static boolean fieldIsRequired(ImmutableList<SchemaWithName> schemaPath, SchemaWithName last) {
        if (schemaPath.size() <= 1) {
            return false;
        } else {
            return contains(schemaPath.secondLast().schema.getRequired(), last.name);
        }
    }

    private static void handleOneOrAnyOf(SchemaWithName last, Schema<?> schema, Cls cls) {
        cls.classType = ClassType.ONE_OR_ANY_OF;
        if (schema.getDiscriminator() != null) {
            cls.interfaceMethods.add(Names.toFieldName(last.name));
        }
    }

    private static void handleEnum(Schema<?> schema, Cls cls) {
        cls.classType = ClassType.ENUM;
        Class<?> valueCls = toClass(schema.getType(), schema.getFormat());
        cls.enumFullType = valueCls.getCanonicalName();
        for (Object o : schema.getEnum()) {
            cls.enumMembers.add(new EnumMember(Names.enumNameToEnumConstant(o.toString()), o));
        }
        cls.addField(cls.enumFullType, "value", "value", true);
    }

    private static <T> boolean contains(Collection<? extends T> collection, T t) {
        return collection != null && t != null && collection.contains(t);
    }

    private static void writeClass(PrintStream out, Imports imports, Indent indent, Cls cls) {
        writeClassDeclaration(out, imports, indent, cls);
        indent.right();
        writeEnumMembers(out, imports, indent, cls);
        if (cls.classType == ClassType.ONE_OR_ANY_OF) {
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

    private static void writeOneOrAnyOfClassContent(PrintStream out, Imports imports, Indent indent, Cls cls) {
        out.format("\n%sprivate final %s %s;\n", indent, imports.add(Object.class), "value");
        // add constructor for each member of the oneOf (fieldTypes)

        out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
        out.format("%s%s(%s value) {\n", indent, cls.simpleName(), imports.add(Object.class));
        out.format("%sthis.value = value;\n", indent.right());
        out.format("%s}\n", indent.left());
        cls.fields.forEach(f -> {
            out.format("\n%spublic %s(%s value) {\n", indent, cls.simpleName(), imports.add(f.fullClassName));
            out.format("%sthis.value = value;\n", indent.right());
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
        String classes = cls.fields.stream().map(x -> imports.add(x.fullClassName) + ".class")
                .collect(Collectors.joining(", "));
        out.format("%ssuper(%s.class, %s.asList(%s));\n", indent, cls.simpleName(), imports.add(Arrays.class), classes);
        indent.left();
        out.format("%s}\n", indent);
        indent.left();
        out.format("%s}\n", indent);
    }

    private static void writeClassDeclaration(PrintStream out, Imports imports, Indent indent, Cls cls) {
        String modifier;
        if (cls.classType == ClassType.INTERFACE || cls.classType == ClassType.ENUM) {
            modifier = "";
        } else {
            modifier = "static final ";
        }
        if (cls.classType == ClassType.ONE_OR_ANY_OF) {
//            out.format("\n%s@%s(use = %s.DEDUCTION)\n", indent, imports.add(JsonTypeInfo.class), imports.add(Id.class));
//            indent.right().right();
//            String types = cls.fields.stream().map(x -> String.format("\n%s@%s(%s.class)", indent,
//                    imports.add(Type.class), imports.add(x.fullClassName))).collect(Collectors.joining(", "));
//            indent.left();
//            indent.left();
//            out.format("%s@%s({%s})\n", indent, imports.add(JsonSubTypes.class), types);
            out.format("\n%s@%s(using = %s.Deserializer.class)\n", indent, imports.add(JsonDeserialize.class),
                    cls.simpleName());
            out.format("%s@%s(fieldVisibility = %s.ANY, creatorVisibility = %s.ANY)\n", indent,
                    imports.add(JsonAutoDetect.class), imports.add(Visibility.class), imports.add(Visibility.class));
        } else {
            out.println();
        }
        out.format("%spublic %s%s %s {\n", indent, modifier, cls.classType.word(), cls.simpleName());
    }

    private static void writeEnumMembers(PrintStream out, Imports imports, Indent indent, Cls cls) {
        String text = cls.enumMembers.stream().map(x -> {
            String delim = x.parameter instanceof String ? "\"" : "";
            return String.format("%s%s(%s%s%s)", indent, x.name, delim, x.parameter, delim);
        }).collect(Collectors.joining(",\n"));
        if (!text.isEmpty()) {
            out.println("\n" + text + ";");
        }
    }

    private static void writeFields(PrintStream out, Imports imports, Indent indent, Cls cls) {
        if (!cls.fields.isEmpty()) {
            out.println();
        }
        cls.fields.forEach(f -> {
            if (cls.classType == ClassType.ENUM) {
                out.format("%s@%s\n", indent, imports.add(JsonValue.class));
            }
            out.format("%sprivate final %s %s;\n", indent, f.resolvedType(imports), f.fieldName);
        });
    }

    private static void writeConstructor(PrintStream out, Imports imports, Indent indent, Cls cls) {
        indent.right().right();
        final String parameters;
        if (cls.classType == ClassType.ENUM) {
            parameters = cls.fields.stream()
                    .map(x -> String.format("\n%s %s", indent, x.resolvedType(imports), x.fieldName))
                    .collect(Collectors.joining());
        } else {
            parameters = cls.fields.stream().map(x -> String.format("\n%s@%s(\"%s\") %s %s", indent,
                    imports.add(JsonProperty.class), x.name, x.resolvedType(imports), x.fieldName))
                    .collect(Collectors.joining(","));
        }
        indent.left().left();
        if (cls.classType != ClassType.ENUM) {
            out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
        } else {
            out.println();
        }
        final String visibility = cls.classType == ClassType.ENUM ? "private" : "public";
        out.format("%s%s %s(%s) {\n", indent, visibility, Names.simpleClassName(cls.fullClassName), parameters);
        indent.right();
        cls.fields.stream().forEach(x -> {
            if (!x.isPrimitive()) {
                out.format("%sthis.%s = %s.checkNotNull(%s);\n", indent, x.fieldName, imports.add(Preconditions.class),
                        x.fieldName);
            } else {
                out.format("%sthis.%s = %s;\n", indent, x.fieldName, x.fieldName);
            }
        });
        indent.left();
        out.format("%s}\n", indent);
    }

    private static void writeGetters(PrintStream out, Imports imports, Indent indent, Cls cls) {
        cls.fields.forEach(f -> {
            out.format("\n%spublic %s %s() {\n", indent, f.resolvedType(imports), f.fieldName);
            indent.right();
            out.format("%sreturn %s;\n", indent, f.fieldName);
            indent.left();
            out.format("%s}\n", indent);
        });
    }

    private static String resolveType(Field f, Imports imports) {
        if (f.required) {
            return imports.add(toPrimitive(f.fullClassName));
        } else {
            return imports.add(Optional.class) + "<" + imports.add(f.fullClassName) + ">";
        }
    }

    private static void writeMemberClasses(PrintStream out, Imports imports, Indent indent, Cls cls) {
        cls.classes.forEach(c -> writeClass(out, imports, indent, c));
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

    private static boolean isPrimitive(String type) {
        return type != null && !"array".equals(type) && !"object".equals(type);
    }

    private static ClassType classType(Schema<?> schema) {
        if (schema instanceof ComposedSchema
                && ((((ComposedSchema) schema).getOneOf() != null) || ((ComposedSchema) schema).getAnyOf() != null)) {
            return ClassType.ONE_OR_ANY_OF;
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
            // TODO long might be safer?
            return "int";
        } else {
            return c;
        }
    }
}
