package org.davidmoten.openapi.v3;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.davidmoten.openapi.v3.internal.ByteArrayPrintStream;

import com.github.davidmoten.guavamini.Preconditions;

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

    private static class Cls {
        String fullClassName;
        ClassType classType;
        List<Field> fields = new ArrayList<>();
        List<EnumMember> enumMembers = new ArrayList<>();
        List<Cls> classes = new ArrayList<>();;
        String enumFullType;
        private int num = 0;

        String nextAnonymousFieldName() {
            num++;
            return "anonymous" + num;
        }

        void addField(String fullType, String name) {
            fields.add(new Field(fullType, name));
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
        INTERFACE("interface"), CLASS("class"), ENUM("enum");

        private final String word;

        ClassType(String word) {
            this.word = word;
        }

        String word() {
            return word;
        }
    }

    private final static class Field {
        final String fullClassName;
        final String name;

        public Field(String fullClassName, String name) {
            this.fullClassName = fullClassName;
            this.name = name;
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
        private boolean once = true;
        private LinkedStack<Cls> stack = new LinkedStack<>();
        private ByteArrayPrintStream out;
        private String fullClassName;

        public MyVisitor(Names names) {
            this.names = names;
            this.out = ByteArrayPrintStream.create();
        }

        @Override
        public void startSchema(ImmutableList<SchemaWithName> schemaPath) {
            SchemaWithName last = schemaPath.last();
//            System.out.println(last);
            Schema<?> schema = last.schema;
            Cls cls;
            if (once) {
                // should be top-level class
                once = false;
                cls = new Cls();
                stack.push(cls);
                cls.fullClassName = names.schemaNameToClassName(last.name);
                imports = new Imports(cls.fullClassName);
                cls.classType = classType(schema);
            } else if (Apis.isComplexSchema(schema)) {
                Cls previous = stack.peek();
                cls = new Cls();
                stack.push(cls);
                previous.classes.add(cls);
                if (isEnum(schema)) {
                    String fieldName = last.name == null ? previous.nextAnonymousFieldName()
                            : Names.toFieldName(last.name);
                    String fullClassName = previous.fullClassName + "."
                            + Names.simpleClassNameFromSimpleName(fieldName);
                    cls.fullClassName = fullClassName;
                    cls.classType = ClassType.ENUM;
                    previous.addField(fullClassName, fieldName);
                    Class<?> valueCls = toClass(schema.getType(), schema.getFormat());
                    cls.enumFullType = valueCls.getCanonicalName();
                    for (int i = 0; i < schema.getEnum().size(); i++) {
                        Object o = schema.getEnum().get(i);
                        cls.enumMembers.add(new EnumMember(Names.enumNameToEnumConstant(o.toString()), o));
                    }
                } else if (isObject(schema)) {
                    String fieldName = last.name == null ? previous.nextAnonymousFieldName()
                            : Names.toFieldName(last.name);
                    String fullClassName = previous.fullClassName + "."
                            + Names.simpleClassNameFromSimpleName(fieldName);
                    cls.fullClassName = fullClassName;
                    cls.classType = ClassType.CLASS;
                    previous.addField(fullClassName, fieldName);
                } else {
                    cls.fullClassName = previous + ".Unknown";
                    cls.classType = ClassType.CLASS;
                }
            } else {
                cls = stack.peek();
                if (isPrimitive(schema.getType())) {
                    Class<?> c = toClass(schema.getType(), schema.getFormat());
                    String fieldName = last.name == null ? cls.nextAnonymousFieldName() : Names.toFieldName(last.name);
                    cls.addField(c.getCanonicalName(), fieldName);
                }
            }
        }

        @Override
        public void finishSchema(ImmutableList<SchemaWithName> schemaPath) {
            System.out.println(schemaPath);
            final Cls cls = stack.peek();
            if (Apis.isComplexSchema(schemaPath.last().schema)) {
                stack.pop();
                if (stack.isEmpty()) {
                    Indent indent = new Indent();
                    out.format("package %s;\n", cls.pkg());
                    out.format("\nIMPORTS_HERE");
                    writeClassDeclaration(out, imports, indent, cls);
                    indent.right();
                    writeFields(out, imports, indent, cls);
                    cls.classes.forEach(c -> writeMemberClass(out, imports, indent, c));
                    indent.left();
                    out.format("}\n");
                    System.out.println(out.text().replace("IMPORTS_HERE", imports.toString()));
                    out.close();
                }
            }
        }
    }

    private static void writeClassDeclaration(PrintStream out, Imports imports, Indent indent, Cls cls) {
        String modifier;
        if (cls.classType == ClassType.INTERFACE || cls.classType == ClassType.ENUM) {
            modifier = "";
        } else {
            modifier = "static final ";
        }
        out.format("\n%spublic %s%s %s {\n", indent, modifier, cls.classType.word(), cls.simpleName());
    }

    private static void writeMemberClass(PrintStream out, Imports imports, Indent indent, Cls cls) {
        writeClassDeclaration(out, imports, indent, cls);
        indent.right();
        writeFields(out, imports, indent, cls);
        writeMemberClasses(out, imports, indent, cls);
        indent.left();
        out.format("%s}\n", indent);
    }

    private static void writeMemberClasses(PrintStream out, Imports imports, Indent indent, Cls cls) {
        cls.classes.forEach(c -> writeMemberClass(out, imports, indent, c));
    }

    private static void writeFields(PrintStream out, Imports imports, Indent indent, Cls cls) {
        cls.fields.forEach(f -> {
            out.format("%sfinal %s %s;\n", indent, imports.add(f.fullClassName), f.name);
        });
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

    private static boolean isPrimitive(String type) {
        return type != null && !"array".equals(type) && !"object".equals(type);
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
