package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.internal.ImmutableList;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

final class Names {

    // note that hashCode and toString added to this set so that generated getters
    // without a get prefix don't get into trouble
    private static final Set<String> javaReservedWords = Sets.newHashSet("abstract", "assert", "boolean", "break",
            "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends",
            "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while", "var", "hashCode", "toString");

    private static final boolean LOG_SCHEMA_PATHS = false;

    private final Definition definition;

    private final OpenAPI api;

    Names(Definition definition) {
        this.definition = definition;
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        OpenAPIParser parser = new OpenAPIParser();
        SwaggerParseResult result = parser.readLocation(definition.definition(), null, options);
        String errors = result.getMessages().stream().collect(Collectors.joining("\n"));
        if (!errors.isEmpty()) {
            if (definition.failOnParseErrors()) {
                throw new RuntimeException(errors);
            } else {
                // Destined for maven plugin output so following their logging format
                System.out.println("[WARNING] Swagger Parse Errors:\n" + errors);
            }
        }
        this.api = result.getOpenAPI();
        superSchemas(api);
        logSchemaFullClassNames(api);
    }

    private static void logSchemaFullClassNames(OpenAPI api) {
        if (LOG_SCHEMA_PATHS) {
            Apis.visitSchemas(api, (category, schemaPath) -> {
                if (!Apis.isComplexSchema(schemaPath.last().schema)) {
                    System.out.println(schemaPath);
                }
            });
            System.out.println("////////////////////////////////////////////////");
        }
    }

    OpenAPI api() {
        return api;
    }

    String schemaNameToClassName(SchemaCategory category, String schemaName) {
        return definition.packages().basePackage() + "." + category.getPackageFragment() + "."
                + schemaNameToSimpleClassName(schemaName);
    }

    String schemaNameToSimpleClassName(String schemaName) {
        return upperFirst(toIdentifier(schemaName));
    }

    File schemaNameToJavaFile(SchemaCategory category, String schemaName) {
        return fullClassNameToJavaFile(schemaNameToClassName(category, schemaName));
    }

    File fullClassNameToJavaFile(String fullClassName) {
        return new File(definition.generatedSourceDirectory(), fullClassName.replace(".", File.separator) + ".java");
    }

    String refToFullClassName(String ref) {
        Preconditions.checkNotNull(ref);
        final String fullClassName;
        if (!ref.startsWith("#")) {
            fullClassName = externalRefClassName(ref);
        } else {
            final SchemaCategory category;
            if (ref.startsWith("#/components/schemas")) {
                category = SchemaCategory.SCHEMA;
            } else if (ref.startsWith("#/components/schemas")) {
                category = SchemaCategory.SCHEMA;
            } else if (ref.startsWith("#/components/responses")) {
                category = SchemaCategory.SCHEMA;
            } else if (ref.startsWith("#/components/parameters")) {
                category = SchemaCategory.SCHEMA;
            } else if (ref.startsWith("#/components/requestBodies")) {
                category = SchemaCategory.SCHEMA;
            } else if (ref.startsWith("#/components/pathItems")) {
                category = SchemaCategory.SCHEMA;
            } else {
                throw new RuntimeException("unexpected ref: " + ref);
            }
            String schemaName = ref.substring(ref.lastIndexOf("/") + 1);
            fullClassName = schemaNameToClassName(category, schemaName);
        }
        return fullClassName;
    }

    static String simpleClassName(String fullClassName) {
        return getLastItemInDotDelimitedString(fullClassName);
    }

    static String pkg(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    static String toIdentifier(String s) {
        StringBuilder b = new StringBuilder();
        char lastCh = ' ';
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (i == 0 && !Character.isJavaIdentifierStart(ch)) {
                b.append("_");
            }
            if (Character.isJavaIdentifierPart(ch)) {
                b.append(ch);
            } else {
                ch = '_';
                if (lastCh != ch) {
                    b.append(ch);
                }
            }
            lastCh = ch;
        }
        String candidate = lowerFirst(b.toString());
        if (javaReservedWords.contains(candidate)) {
            return candidate + "_";
        } else {
            return candidate;
        }
    }

    static String propertyNameToClassSimpleName(String propertyName) {
        return upperFirst(toIdentifier(propertyName));
    }

    static String upperFirst(String name) {
        return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
    }

    private static String lowerFirst(String name) {
        return name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
    }

    private static String getLastItemInDotDelimitedString(String name) {
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return name;
        } else {
            return name.substring(i + 1);
        }
    }

    String clientClassName() {
        return definition.packages().basePackage() + "client.Client";
    }

    File clientClassJavaFile() {
        return new File(definition.generatedSourceDirectory(),
                clientClassName().replace(".", File.separator) + ".java");
    }

    static String propertyNameToFieldName(String propertyName) {
        return lowerFirst(toIdentifier(propertyName));
    }

    static String schemaNameToFieldName(String schemaName) {
        return lowerFirst(toIdentifier(schemaName));
    }

    static String toFieldName(String name) {
        return lowerFirst(toIdentifier(name));
    }

    static String simpleClassNameFromSimpleName(String name) {
        return upperFirst(toIdentifier(name));
    }

    static String enumNameToEnumConstant(String s) {
        return camelToUpper(toIdentifier(s));
    }

    static String camelToUpper(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && i < s.length() - 1 && Character.isUpperCase(s.charAt(i))
                    && Character.isLowerCase(s.charAt(i + 1)) && s.charAt(i - 1) != '_') {
                b.append("_");
            }
            b.append(Character.toUpperCase(s.charAt(i)));
        }
        return b.toString();
    }

    private static Map<Schema<?>, Set<Schema<?>>> superSchemas(OpenAPI api) {
        Predicate<Schema<?>> predicate = x -> x instanceof ComposedSchema && ((ComposedSchema) x).getOneOf() != null;
        Map<Schema<?>, Set<Schema<?>>> map = new HashMap<>();
        api.getComponents() //
                .getSchemas() //
                .entrySet() //
                .stream() //
                .flatMap(x -> findSchemas(SchemaCategory.SCHEMA, x.getKey(), x.getValue(), predicate).stream()) //
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

    private static List<Schema<?>> findSchemas(SchemaCategory category, String name, Schema<?> schema,
            Predicate<Schema<?>> predicate) {
        List<Schema<?>> list = new ArrayList<>();
        Apis.visitSchemas(category, ImmutableList.of(new SchemaWithName(name, schema)), (c, schemaPath) -> {
            if (predicate.test(schemaPath.last().schema)) {
                list.add(schemaPath.last().schema);
            }
        });
        return list;
    }

    String externalRefClassName(String ref) {
        return definition.externalRefClassName(ref);
    }

    String globalsFullClassName() {
        return definition.packages().basePackage() + ".Globals";
    }

    static Map<String, String> getEnumValueToIdentifierMap(List<?> values) {
        Map<String, String> map = new HashMap<>();
        Set<String> set = new LinkedHashSet<>();
        values.forEach(o -> set.add(o.toString()));
        for (String o : set) {
            int i = 0;
            String name = enumNameToEnumConstant(o);
            while (true) {
                String candidate = name + (i == 0 ? "" : "_" + i);
                if (!map.containsValue(candidate)) {
                    map.put(o, candidate);
                    break;
                }
                i++;
            }
        }
        return map;
    }

    public boolean mapIntegerToBigInteger() {
        return definition.mapIntegerToBigInteger();
    }

}
