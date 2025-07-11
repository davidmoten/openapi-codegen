package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.Generator.Cls;
import org.davidmoten.oa3.codegen.generator.internal.EnhancedOpenAPIV3Parser;
import org.davidmoten.oa3.codegen.util.ImmutableList;
import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.github.davidmoten.guavamini.Maps;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

import io.swagger.v3.core.util.ObjectMapperFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Names {

    // note that all Object methods added to this set so that generated getters
    // without a get prefix don't get into trouble
    private static final Set<String> javaReservedWords = Sets.of("abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends",
            "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while", "var", "hashCode", "toString", "notify", "clone", "equals",
            "finalize", "getClass", "notifyAll", "wait", "builder", "enum");

    // TODO remove this now that Imports is aware of package contents
    // avoid class names that might clash with generated classes in the same package
    private static final Set<String> reservedSimpleClassNames = Sets.of("Builder");

    private static final boolean LOG_SCHEMA_PATHS = false;

    private final Definition definition;

    private final OpenAPI api;

    private final ServerGeneratorType generatorType;

    private final int maxClassNameLength;

    Names(Definition definition) {
        StreamReadConstraints streamReadConstraints = StreamReadConstraints.builder().maxStringLength(Integer.MAX_VALUE)
                .build();
        ObjectMapperFactory.createJson31().getFactory().setStreamReadConstraints(streamReadConstraints);
        this.definition = definition;
        this.generatorType = definition.generator().map(x -> ServerGeneratorType.from(x.toUpperCase(Locale.ENGLISH)))
                .orElse(ServerGeneratorType.SPRING_BOOT_2);
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        // github api goes over snake yaml parser max code points for 3.0
        System.setProperty("maxYamlCodePoints", "999999999");
        OpenAPIV3Parser parser = new EnhancedOpenAPIV3Parser();
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
        if (this.api == null) {
            throw new IllegalStateException("OpenAPI object could not be parsed");
        }
        this.maxClassNameLength = definition.maxClassNameLength();
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

    public List<Server> servers() {
        return Util.<List<io.swagger.v3.oas.models.servers.Server>>nvl(api.getServers(), Collections.emptyList()) //
                .stream() //
                .map(x -> new Server(x.getUrl(), Optional.ofNullable(x.getDescription()))) //
                .collect(Collectors.toList());
    }

    public static final class Server {
        public final String url;
        public final Optional<String> description;

        Server(String url, Optional<String> description) {
            this.url = url;
            this.description = description;
        }
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
        return getLastItemInDotDelimitedString(stripGenerics(fullClassName));
    }

    private static String stripGenerics(String name) {
        int i = name.indexOf("<");
        if (i == -1) {
            return name;
        } else {
            return name.substring(0, i);
        }
    }

    public static String pkg(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String toIdentifier(String s) {
        Preconditions.checkArgument(!s.isEmpty(), "string is empty");
        String candidate = lowerFirst(identifierCandidate(s));
        return adjustIfReservedWord(candidate);
    }

    private static String adjustIfReservedWord(String candidate) {
        if (javaReservedWords.contains(candidate)) {
            return candidate + "_";
        } else {
            return candidate;
        }
    }

    public static String toEnumIdentifier(String s) {
        String candidate = identifierCandidate(s);
        if (javaReservedWords.contains(candidate.toUpperCase(Locale.ENGLISH))) {
            return candidate + "_";
        } else {
            return candidate;
        }
    }

    private static String identifierCandidate(String s) {
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
        return b.toString();
    }

    public static String propertyNameToClassSimpleName(String propertyName) {
        return upperFirst(toIdentifier(propertyName));
    }

    public static String upperFirst(String name) {
        if (name.isEmpty()) {
            return name;
        } else {
            return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
        }
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

    public String simpleClassNameFromSimpleName(String name) {
        String s = upperFirst(underscoreToCamel(toIdentifier(skipUnderscoresAtStart(name))));
        s = removeLowerCaseVowels(s, maxClassNameLength);
        return fixReservedSimpleClassName(s);
    }
    
    public static String removeLowerCaseVowels(String s, int maxLength) {
        if (s.length() <= maxLength) {
            return s;
        }
        StringBuilder b = new StringBuilder();
        // trim lowercase vowels from right to left
        for (int i = s.length() - 1; i >= 0; i--) {
            char ch = s.charAt(i);
            if (!isLowerCaseVowel(ch) || i + b.length() < maxLength) {
                b.append(ch);
            }
        }
        return b.reverse().toString();
    }

    private static boolean isLowerCaseVowel(char ch) {
        return ch == 'a'|| ch == 'e'||ch=='i'||ch == 'o'|| ch == 'u';
    }

    private static String fixReservedSimpleClassName(String simpleClassName) {
        if (reservedSimpleClassNames.contains(simpleClassName)) {
            return simpleClassName + "_";
        } else {
            return simpleClassName;
        }
    }

    @VisibleForTesting
    static String underscoreToCamel(String s) {
        StringBuilder b = new StringBuilder();
        b.append(s.charAt(0));
        for (int i = 1; i < s.length() - 1; i++) {
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
        if (s.length() > 1) {
            char ch = s.charAt(s.length() - 1);
            if (ch != '_') {
                b.append(ch);
            }
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
        if (s.isEmpty()) {
            return "BLANK";
        } else {
            return camelToUpper(toEnumIdentifier(s));
        }
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
        schemas(api) //
                .entrySet() //
                .stream() //
                .flatMap(x -> findSchemas(SchemaCategory.SCHEMA, x.getKey(), x.getValue(), predicate).stream()) //
                .map(x -> (ComposedSchema) x) //
                .forEach(x -> {
                    for (Schema<?> sch : x.getOneOf()) {
                        Set<Schema<?>> set = map.computeIfAbsent(sch, k -> new HashSet<>());
                        set.add(x);
                    }
                });
        return map;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Schema<?>> schemas(OpenAPI api) {
        if (api.getComponents() == null || api.getComponents().getSchemas() == null) {
            return Collections.emptyMap();
        } else {
            return (Map<String, Schema<?>>) (Map<?, ?>) api.getComponents() //
                    .getSchemas();
        }
    }

    private static List<Schema<?>> findSchemas(SchemaCategory category, String name, Schema<?> schema,
            Predicate<Schema<?>> predicate) {
        List<Schema<?>> list = new ArrayList<>();
        Apis.visitSchemas(category, ImmutableList.of(new SchemaWithName(name, schema)), Maps.empty(),
                (c, schemaPath) -> {
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
            b.append(upperFirst(camelifyOnSeparatorCharacters(s)));
        }
        return toIdentifier(b.toString());
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
        return parameters(api).get(lastComponent(name));
    }

    private static Map<String, Parameter> parameters(OpenAPI api) {
        if (api.getComponents() == null || api.getComponents().getParameters() == null) {
            return Collections.emptyMap();
        } else {
            return api.getComponents().getParameters();
        }
    }

    public static String lastComponent(String ref) {
        int i = ref.lastIndexOf('/');
        return ref.substring(i + 1);
    }

    public RequestBody lookupRequestBody(String ref) {
        return requestBodies(api).get(lastComponent(ref));
    }

    private static Map<String, RequestBody> requestBodies(OpenAPI api) {
        if (api.getComponents() == null || api.getComponents().getRequestBodies() == null) {
            return Collections.emptyMap();
        } else {
            return api.getComponents().getRequestBodies();
        }
    }

    public ApiResponse lookupResponse(String ref) {
        return responses(api).get(lastComponent(ref));
    }

    private Map<String, ApiResponse> responses(OpenAPI api) {
        if (api.getComponents() == null || api.getComponents().getResponses() == null) {
            return Collections.emptyMap();
        } else {
            return api.getComponents().getResponses();
        }
    }

    public ServerGeneratorType generatorType() {
        return generatorType;
    }

    public boolean mapNumberToBigDecimal() {
        return definition.mapNumberToBigDecimal();
    }

    public boolean generateService() {
        return definition.generateService();
    }

    public Predicate<String> simpleNameInPackage(String fullClassName) {
        return x -> {
            String pkg = pkg(fullClassName);
            String simple = simpleClassName(x);
            Set<String> set = packageSimpleClassNames.get(pkg);
            return set != null && set.contains(simple);
        };
    }
    
    private final Map<String, Set<String>> packageSimpleClassNames = new HashMap<>();
    private final Map<String, Cls> classes = new HashMap<>();

    private void registerFullClassName(String fullClassName) {
        String pkg = pkg(fullClassName);
        String simple = simpleClassName(fullClassName);
        Set<String> set = packageSimpleClassNames.computeIfAbsent(pkg, k -> new HashSet<>());
        set.add(simple);
    }

    public void registerCls(Cls cls) {
        registerFullClassName(cls.fullClassName);
        registerTree(cls);
    }

    private void registerTree(Cls cls) {
        classes.put(cls.fullClassName, cls);
        cls.classes.forEach(this::registerTree);
    }

    public Optional<Cls> cls(String fullClassName) {
        return Optional.ofNullable(classes.get(fullClassName));
    }

    public boolean applyReadOnly() {
        return definition.applyReadOnly();
    }

    public boolean applyWriteOnly() {
        return definition.applyWriteOnly();
    }

}
