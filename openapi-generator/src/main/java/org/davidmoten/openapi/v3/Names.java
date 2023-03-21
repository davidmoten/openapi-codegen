package org.davidmoten.openapi.v3;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Sets;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Names {

    private static final Set<String> javaReservedWords = Sets.newHashSet("abstract", "assert", "boolean", "break",
            "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends",
            "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while", "var");

    private final Definition definition;

    private final OpenAPI api;
    private final Map<Schema<?>, Set<Schema<?>>> superSchemas;
    private final Map<Schema<?>, String> schemaFullClassNames;

    public Names(Definition definition) {
        this.definition = definition;
        SwaggerParseResult result = new OpenAPIParser().readContents(definition.definition(), null, null);
        result.getMessages().stream().forEach(System.out::println);
        this.api = result.getOpenAPI();
        this.superSchemas = superSchemas(api);
        this.schemaFullClassNames = schemaFullClassNames(api);
    }

    @SuppressWarnings("unchecked")
    private final static List<Class<? extends Schema<?>>> GROUPING_SCHEMA_CLASSES = Lists.newArrayList( //
            ObjectSchema.class, MapSchema.class, ComposedSchema.class, ArraySchema.class);

    private static Map<Schema<?>, String> schemaFullClassNames(OpenAPI api) {
        visitSchemas(api, schemas -> {
            if (!isGroupingSchema(schemas.last().schema)) {
                System.out.println(schemas);
            }
        });
        return null;
    }

    private static final boolean isGroupingSchema(Schema<?> schema) {
        for (Class<? extends Schema<?>> cls : GROUPING_SCHEMA_CLASSES) {
            if (cls.isAssignableFrom(schema.getClass())) {
                return true;
            }
        }
        return schema.getProperties() != null;
    }

    public OpenAPI api() {
        return api;
    }

    public String schemaNameToClassName(String schemaName) {
        return definition.packages().modelPackage() + "." + schemaNameToSimpleClassName(schemaName);
    }

    public String schemaNameToSimpleClassName(String schemaName) {
        return upperFirst(toIdentifier(schemaName));
    }

    public File schemaNameToJavaFile(String schemaName) {
        return new File(definition.generatedSourceDirectory(),
                schemaNameToClassName(schemaName).replace(".", File.separator) + ".java");
    }

    public static String simpleClassName(String className) {
        return getLastItemInDotDelimitedString(className);
    }

    public static String pkg(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String toIdentifier(String s) {
        if (Character.isDigit(s.charAt(0))) {
            return "_" + s;
        } else if (javaReservedWords.contains(s.toLowerCase())) {
            return s.toLowerCase() + "_";
        } else if (s.toUpperCase().equals(s)) {
            return s;
        } else {
            return lowerFirst(s);
        }
    }

    public static String propertyNameToClassSimpleName(String propertyName) {
        return upperFirst(toIdentifier(propertyName));
    }

    public static String upperFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private static String lowerFirst(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private static String getLastItemInDotDelimitedString(String name) {
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return name;
        } else {
            return name.substring(i + 1);
        }
    }

    public String clientClassName() {
        return definition.packages().clientPackage() + ".Client";
    }

    public File clientClassJavaFile() {
        return new File(definition.generatedSourceDirectory(),
                clientClassName().replace(".", File.separator) + ".java");
    }

    public static String propertyNameToFieldName(String propertyName) {
        return lowerFirst(toIdentifier(propertyName));
    }

    public static String schemaNameToFieldName(String schemaName) {
        return lowerFirst(toIdentifier(schemaName));
    }

    public static String toFieldName(String name) {
        return lowerFirst(toIdentifier(name));
    }

    public static String simpleClassNameFromSimpleName(String name) {
        return upperFirst(toIdentifier(name));
    }

    public static String enumNameToEnumConstant(String s) {
        return toIdentifier(s);
    }

    private static Map<Schema<?>, Set<Schema<?>>> superSchemas(OpenAPI api) {
        Predicate<Schema<?>> predicate = x -> x instanceof ComposedSchema && ((ComposedSchema) x).getOneOf() != null;
        Map<Schema<?>, Set<Schema<?>>> map = new HashMap<>();
        api.getComponents() //
                .getSchemas() //
                .entrySet() //
                .stream() //
                .flatMap(x -> findSchemas(x.getKey(), x.getValue(), predicate).stream()) //
                .map(x -> (ComposedSchema) x) //
                .forEach(x -> {
                    for (Schema<?> sch : x.getOneOf()) {
                        Set<Schema<?>> set = map.get(sch);
                        if (set == null) {
                            set = new HashSet<>();
                            map.put(sch, set);
                        }
                        set.add(x);
                    }
                });
        return map;
    }

    private static List<Schema<?>> findSchemas(String name, Schema<?> schema, Predicate<Schema<?>> predicate) {
        List<Schema<?>> list = new ArrayList<>();
        visitSchemas(ImmutableList.of(new SchemaWithName(name, schema)), (schemas) -> {
            if (predicate.test(schemas.last().schema)) {
                list.add(schemas.last().schema);
            }
        });
        return list;
    }

    static final class ImmutableList<T> implements Iterable<T> {

        private final List<T> list;

        ImmutableList() {
            this(new ArrayList<>());
        }

        ImmutableList(List<T> list) {
            this.list = list;
        }

        ImmutableList<T> add(T value) {
            List<T> list2 = new ArrayList<>(list);
            list2.add(value);
            return new ImmutableList<T>(list2);
        }

        @SafeVarargs
        static <T> ImmutableList<T> of(T... values) {
            List<T> list = Arrays.asList(values);
            return new ImmutableList<>(list);
        }

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }

        @Override
        public String toString() {
            return list.toString();
        }

        public T last() {
            return list.get(list.size() - 1);
        }

    }

    private static void visitSchemas(OpenAPI api, Consumer<ImmutableList<SchemaWithName>> consumer) {
        api //
                .getComponents() //
                .getSchemas() //
                .entrySet() //
                .stream() //
                .forEach(entry -> visitSchemas(ImmutableList.of(new SchemaWithName(entry.getKey(), entry.getValue())),
                        consumer));
    }

    private static final class SchemaWithName {
        final String name;
        final Schema<?> schema;

        SchemaWithName(String name, Schema<?> schema) {
            this.name = name;
            this.schema = schema;
        }

        @Override
        public String toString() {
            final String s;
            if (schema.get$ref() != null) {
                s = schema.get$ref();
            } else {
                s = schema.getClass().getSimpleName();
            }
            return "(" + name + ": " + s + ")";
        }

    }

    private static void visitSchemas(ImmutableList<SchemaWithName> schemaPath,
            Consumer<ImmutableList<SchemaWithName>> consumer) {
        Schema<?> schema = schemaPath.last().schema;
        consumer.accept(schemaPath);
        if (schema.getAdditionalProperties() instanceof Schema) {
            visitSchemas(
                    schemaPath.add(
                            new SchemaWithName("additionalProperties", (Schema<?>) schema.getAdditionalProperties())),
                    consumer);
        }
        if (schema.getNot() != null) {
            visitSchemas(schemaPath.add(new SchemaWithName("not", schema.getNot())), consumer);
        }
        if (schema.getProperties() != null) {
            schema.getProperties().entrySet().forEach(
                    x -> visitSchemas(schemaPath.add(new SchemaWithName(x.getKey(), x.getValue())), consumer));
        }
        if (schema instanceof ArraySchema) {
            ArraySchema a = (ArraySchema) schema;
            if (a.getItems() != null) {
                visitSchemas(schemaPath.add(new SchemaWithName(null, a.getItems())), consumer);
            }
        } else if (schema instanceof ComposedSchema) {
            ComposedSchema a = (ComposedSchema) schema;
            if (a.getAllOf() != null) {
                a.getAllOf().forEach(x -> visitSchemas(schemaPath.add(new SchemaWithName(null, x)), consumer));
            }
            if (a.getOneOf() != null) {
                a.getOneOf().forEach(x -> visitSchemas(schemaPath.add(new SchemaWithName(null, x)), consumer));
            }
            if (a.getAnyOf() != null) {
                a.getAnyOf().forEach(x -> visitSchemas(schemaPath.add(new SchemaWithName(null, x)), consumer));
            }
        } else if (schema instanceof MapSchema) {
            // nothing to add here
        } else if (schema instanceof ObjectSchema) {
            // nothing to add here
        }
    }
}
