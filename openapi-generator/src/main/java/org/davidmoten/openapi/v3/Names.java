package org.davidmoten.openapi.v3;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

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

    private final Map<Schema<?>, List<Schema<?>>> superSchemas = new HashMap<>();

    private final OpenAPI api;

    private final Map<Schema<?>, Set<Schema<?>>> superClasses;

    public Names(Definition definition) {
        this.definition = definition;
        SwaggerParseResult result = new OpenAPIParser().readContents(definition.definition(), null, null);
        result.getMessages().stream().forEach(System.out::println);
        this.api = result.getOpenAPI();
        this.superClasses = superClasses(api);
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

    private static Map<Schema<?>, Set<Schema<?>>> superClasses(OpenAPI api) {
        Predicate<Schema<?>> predicate = x -> x instanceof ComposedSchema && ((ComposedSchema) x).getOneOf() != null;
        Map<Schema<?>, Set<Schema<?>>> map = new HashMap<>();
        api.getComponents() //
                .getSchemas() //
                .values() //
                .stream() //
                .flatMap(x -> findSchemas(x, predicate).stream()) //
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

    private static List<Schema<?>> findSchemas(Schema<?> schema, Predicate<Schema<?>> predicate) {
        List<Schema<?>> list = new ArrayList<>();
        Set<Schema<?>> visited = new HashSet<>();
        findSchemas(schema, predicate, list, visited);
        return list;
    }

    private static void findSchemas(Schema<?> schema, Predicate<Schema<?>> predicate, List<Schema<?>> list,
            Set<Schema<?>> visited) {
        if (!visited.add(schema))
            return;
        if (predicate.test(schema)) {
            list.add(schema);
        }
        if (schema instanceof ArraySchema) {
            ArraySchema a = (ArraySchema) schema;
            if (a.getProperties() != null) {
                a.getProperties().values().forEach(x -> findSchemas(x, predicate, list, visited));
            }
        } else if (schema instanceof ComposedSchema) {
            ComposedSchema a = (ComposedSchema) schema;
            if (a.getAllOf() != null) {
                a.getAllOf().forEach(x -> findSchemas(x, predicate, list, visited));
            }
            if (a.getOneOf() != null) {
                a.getOneOf().forEach(x -> findSchemas(x, predicate, list, visited));
            }
            if (a.getAnyOf() != null) {
                a.getAnyOf().forEach(x -> findSchemas(x, predicate, list, visited));
            }
        } else if (schema instanceof MapSchema) {
            MapSchema a = (MapSchema) schema;
            Object o = a.getAdditionalProperties();
            if (o != null && o instanceof Schema) {
                findSchemas((Schema<?>) o, predicate, list, visited);
            }
        } else if (schema instanceof ObjectSchema) {
            ObjectSchema a = (ObjectSchema) schema;
            @SuppressWarnings("rawtypes")
            Map<String, Schema> o = a.getProperties();
            if (o != null) {
                o.values().forEach(x -> findSchemas(x, predicate, list, visited));
            }
        }
    }
}
