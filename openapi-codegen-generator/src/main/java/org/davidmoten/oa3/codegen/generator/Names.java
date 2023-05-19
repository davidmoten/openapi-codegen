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

import org.davidmoten.oa3.codegen.util.ImmutableList;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Names {

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

    private final ServerGeneratorType generatorType;

    Names(Definition definition) {
        this.definition = definition;
        this.generatorType = definition.generator().map(x -> ServerGeneratorType.valueOf(x.toUpperCase(Locale.ENGLISH)))
                .orElse(ServerGeneratorType.SPRING2);
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

    public OpenAPI api() {
        return api;
    }

    public String schemaNameToFullClassName(SchemaCategory category, String schemaName) {
        return definition.packages().basePackage() + "." + category.getPackageFragment() + "."
                + schemaNameToSimpleClassName(schemaName);
    }

    public String schemaNameToSimpleClassName(String schemaName) {
        return simpleClassNameFromSimpleName(schemaName);
    }

    public File fullClassNameToJavaFile(String fullClassName) {
        return new File(definition.generatedSourceDirectory(), fullClassName.replace(".", File.separator) + ".java");
    }

    public String refToFullClassName(String ref) {
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
            fullClassName = schemaNameToFullClassName(category, schemaName);
        }
        return fullClassName;
    }

    public static String simpleClassName(String fullClassName) {
        return getLastItemInDotDelimitedString(fullClassName);
    }

    public static String pkg(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String toIdentifier(String s) {
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

    public static String propertyNameToClassSimpleName(String propertyName) {
        return upperFirst(toIdentifier(propertyName));
    }

    public static String upperFirst(String name) {
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

    public String clientClassName() {
        return definition.packages().basePackage() + "client.Client";
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
        return upperFirst(underscoreToCamel(skipUnderscoresAtStart(toIdentifier(name))));
    }

    @VisibleForTesting
    static String underscoreToCamel(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length() - 1; i++) {
            char ch = s.charAt(i);
            char next = s.charAt(i + 1);
            if (ch == '_') {
                if (next != '_') {
                    b.append(Character.toUpperCase(next));
                    i++;
                }
            } else {
                b.append(ch);
            }
        }
        char ch = s.charAt(s.length() - 1);
        if (ch != '_') {
            b.append(ch);
        }
        return b.toString();
    }

    private static String skipUnderscoresAtStart(String s) {
        int i = 0;
        while (s.charAt(i) == '_' && i < s.length() - 1) {
            i++;
        }
        return s.substring(i);
    }

    public static String enumNameToEnumConstant(String s) {
        return camelToUpper(toIdentifier(s));
    }

    public static String camelToUpper(String s) {
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

    public String externalRefClassName(String ref) {
        return definition.externalRefClassName(ref);
    }

    public String globalsFullClassName() {
        return definition.packages().basePackage() + ".Globals";
    }

    static Map<String, String> getEnumValueToIdentifierMap(List<?> values) {
        Map<String, String> map = new HashMap<>();
        Set<String> set = new LinkedHashSet<>();
        values.forEach(o -> set.add(String.valueOf(o)));
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

    public static String toIdentifier(ImmutableList<String> list) {
        StringBuilder b = new StringBuilder();
        for (String s : list) {
            b.append(Names.upperFirst(camelifyOnSeparatorCharacters(s)));
        }
        return Names.toIdentifier(b.toString());
    }

    private static String camelifyOnSeparatorCharacters(String s) {
        StringBuilder b = new StringBuilder();
        boolean start = true;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '/' || ch == '{' || ch == '}' || ch == '-' || ch == '_' || ch == '.') {
                start = true;
            } else {
                if (start) {
                    b.append(Character.toUpperCase(ch));
                } else {
                    b.append(ch);
                }
                start = false;
            }
        }
        return b.toString();
    }

    public String serviceControllerFullClassName() {
        return definition.packages().basePackage() + ".service.ServiceController";
    }

    public String serviceInterfaceFullClassName() {
        return definition.packages().basePackage() + ".service.Service";
    }

    public String applicationFullClassName() {
        return definition.packages().basePackage() + ".Application";
    }

    public String clientFullClassName() {
        return definition.packages().basePackage() + ".client.Client";
    }

    public String jacksonConfigurationFullClassName() {
        return definition.packages().basePackage() + ".service.JacksonConfiguration";
    }

    public Parameter lookupParameter(String name) {
        return api.getComponents().getParameters().get(lastComponent(name));
    }

    public static String lastComponent(String ref) {
        int i = ref.lastIndexOf('/');
        return ref.substring(i + 1);
    }

    public RequestBody lookupRequestBody(String ref) {
        return api.getComponents().getRequestBodies().get(lastComponent(ref));
    }

    public ApiResponse lookupResponse(String ref) {
        return api.getComponents().getResponses().get(lastComponent(ref));
    }

    public ServerGeneratorType generatorType() {
        return generatorType;
    }

}
