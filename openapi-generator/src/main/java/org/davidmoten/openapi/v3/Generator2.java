package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;

import org.davidmoten.openapi.v3.internal.ByteArrayPrintStream;

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

    private static void writeClientClass(Names names) {
        String className = names.clientClassName();
        File file = names.clientClassJavaFile();
        JavaClassWriter.write(file, className, ClassType.CLASS, (indent, imports, p) -> {
            p.format("%s// TODO\n", indent);
        });
    }

    private static void writeSchemaClasses(Definition definition, Names names) {
        names.api().getComponents().getSchemas().entrySet().forEach(entry -> {
            MyVisitor v = new MyVisitor(names);
            Apis.visitSchemas(entry.getKey(), entry.getValue(), v);
        });
    }

    private static final class State {

    }

    private static final class MyVisitor implements Visitor {
        private final Names names;
        private Imports imports;
        private boolean once = true;
        private Deque<State> stack = new LinkedList<>();
        private Indent indent;
        private ByteArrayPrintStream out;
        private String fullClassName;
        private String classType;

        public MyVisitor(Names names) {
            this.names = names;
            this.indent = new Indent();
            this.out = ByteArrayPrintStream.create();
        }

        @Override
        public void startSchema(ImmutableList<SchemaWithName> schemaPath) {
            if (once) {
                SchemaWithName first = schemaPath.first();
                fullClassName = names.schemaNameToClassName(first.name);
                imports = new Imports(fullClassName);
                once = false;
                this.classType = classType(first.schema);
            }
            indent.right();
            State state = new State();
            stack.push(state);
        }

        @Override
        public void finishSchema() {
            stack.pop();
            indent.left();
            if (stack.isEmpty()) {
                System.out.println("////////////////////////////////////////");
                String prefix = String.format("package %s;\n\n", Names.pkg(fullClassName)) + imports.toString();
                try (ByteArrayPrintStream o = ByteArrayPrintStream.create()) {
                    o.print(prefix);
                    o.format("public final %s %s {\n", classType, Names.simpleClassName(fullClassName));
                    o.print(out.text());
                    o.format("}\n");
                    o.flush();
                    System.out.println(o.text());
                } finally {
                    out.close();
                }
            }
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

    private static String classType(Schema<?> schema) {
        if (schema instanceof ComposedSchema && ((ComposedSchema) schema).getOneOf() != null) {
            return "interface";
        } else if (schema.getEnum() != null) {
            return "enum";
        } else {
            return "class";
        }
    }

}
