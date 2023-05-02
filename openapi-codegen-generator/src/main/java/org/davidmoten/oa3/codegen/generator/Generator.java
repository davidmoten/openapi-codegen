package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.util.Util.orElse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.LinkedStack;
import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.runtime.internal.PolymorphicType;
import org.davidmoten.oa3.codegen.util.ImmutableList;

import com.github.davidmoten.guavamini.Sets;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

public class Generator {

    private final Definition definition;

    public Generator(Definition definition) {
        this.definition = definition;
    }

    public void generate() {

        // Names object for each Packages object
        Names names = new Names(definition);

        // generate model classes for schema definitions
        writeSchemaClasses(definition, names);

        SchemasCodeWriter.writeGlobalsClass(names);

    }

    private static void writeSchemaClasses(Definition definition, Names names) {
        MyVisitor v = new MyVisitor(names);
        Apis.visitSchemas(names.api(), v);

        Map<String, Set<Cls>> fullClassNameInterfaces = new HashMap<>();
        for (MyVisitor.Result result : v.results()) {
            findFullClassNameInterfaces(result.cls, fullClassNameInterfaces);
        }
        for (MyVisitor.Result result : v.results()) {
            Cls cls = result.cls;
            String schemaName = result.name;
            if ((definition.includeSchemas().isEmpty() || definition.includeSchemas().contains(schemaName))
                    && !definition.excludeSchemas().contains(schemaName)) {
                SchemasCodeWriter.writeSchemaClass(names, fullClassNameInterfaces, cls, schemaName);
            }
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

    static final class Cls {
        SchemaCategory category;
        String fullClassName;
        Optional<String> description = Optional.empty();
        ClassType classType;
        List<Field> fields = new ArrayList<>();
        List<EnumMember> enumMembers = new ArrayList<>();
        List<Cls> classes = new ArrayList<>();
        Discriminator discriminator = null;
        String enumFullType;
        private int num = 0;
        private Set<String> fieldNames = new HashSet<>();
        boolean topLevel = false;
        boolean hasProperties = false;
        PolymorphicType polymorphicType;
        Optional<Cls> owner = Optional.empty(); // the owning heirarchy, we cannot name our class any one of
                                                // these classes (disallowed by java)
        Optional<String> name = Optional.empty();
        Optional<Schema<?>> schema = Optional.empty();

        String nextAnonymousFieldName() {
            num++;
            return "object" + num;
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

        String pkg() {
            return Names.pkg(fullClassName);
        }

        String simpleName() {
            return Names.simpleClassName(fullClassName);
        }

        boolean unwrapSingleField() {
            return !hasProperties
                    && (classType == ClassType.ENUM || classType == ClassType.ARRAY_WRAPPER
                            || topLevel && fields.size() == 1)
                    || classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED;
        }

        Set<String> ownersAndSiblingsSimpleNames() {
            Cls c = this;
            Set<String> set = new HashSet<>();
            while (c.owner.isPresent()) {
                set.add(c.owner.get().simpleName());
                c = c.owner.get();
            }
            classes.forEach(x -> set.add(x.simpleName()));
            return set;
        }
    }

    static class EnumMember {
        String name;
        Object parameter;

        EnumMember(String name, Object parameter) {
            this.name = name;
            this.parameter = parameter;
        }
    }

    enum ClassType {
        CLASS("class"), //
        ENUM("enum"), //
        ONE_OR_ANY_OF_DISCRIMINATED("interface"), //
        ONE_OR_ANY_OF_NON_DISCRIMINATED("class"), //
        ALL_OF("class"), //
        ARRAY_WRAPPER("class");

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

    final static class Field {
        final String fullClassName;
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

        Field(String fullClassName, String name, String fieldName, boolean required, boolean isArray,
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

        String fieldName(Cls cls) {
            return cls.fieldName(this);
        }

        String resolvedType(Imports imports) {
            return Generator.resolvedType(this, imports);
        }

        String resolvedTypeNullable(Imports imports) {
            return Generator.resolvedTypeNullable(this, imports);
        }

        boolean isPrimitive() {
            return required && PRIMITIVE_CLASS_NAMES.contains(Util.toPrimitive(fullClassName));
        }

        boolean isOctets() {
            return encoding == Encoding.OCTET;
        }

        boolean isByteArray() {
            return fullClassName.equals("byte[]");
        }

        @Override
        public String toString() {
            return "Field [fullClassName=" + fullClassName + ", name=" + name + ", fieldName=" + fieldName
                    + ", required=" + required + ", minLength=" + minLength + ", maxLength=" + maxLength + "]";
        }
    }

    static final class MyVisitor implements Visitor {
        private final Names names;
        private final LinkedStack<Cls> stack = new LinkedStack<>();
        private final List<Result> results = new ArrayList<>();

        MyVisitor(Names names) {
            this.names = names;
        }

        @Override
        public void startSchema(SchemaCategory category, ImmutableList<SchemaWithName> schemaPath) {
            SchemaWithName last = schemaPath.last();
            Schema<?> schema = last.schema;
            final Cls cls = new Cls();
            cls.category = category;
            cls.description = Optional.ofNullable(schema.getDescription());
            if (stack.isEmpty()) {
                // should be top-level class
                cls.fullClassName = names.schemaNameToFullClassName(cls.category, last.name);
                cls.name = Optional.of(last.name);
                cls.schema = Optional.of(schema);
                cls.classType = classType(schema);
                cls.topLevel = true;
            }
            if (Util.isArray(schema)) {
                Optional<Cls> previous = Optional.ofNullable(stack.peek());
                updateLinks(cls, previous);
                if (previous.isPresent()) {
                    Optional<String> fieldName = Optional.of(previous.get().nextFieldName(last.name));
                    String candidate = previous.get().fullClassName + "."
                            + Names.simpleClassNameFromSimpleName(fieldName.get());
                    cls.fullClassName = resolveCandidateFullClassName(cls, candidate);
                    boolean required = fieldIsRequired(schemaPath);
                    previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required,
                            previous.get().classType == ClassType.ARRAY_WRAPPER));

                } else {
                    cls.fullClassName = names.schemaNameToFullClassName(cls.category, last.name);
                }
                cls.classType = ClassType.ARRAY_WRAPPER;
                stack.push(cls);
                return;
            }
            boolean isArray = schemaPath.size() >= 2 && schemaPath.secondLast().schema instanceof ArraySchema;
            Optional<Integer> minItems = isArray ? Optional.ofNullable(schemaPath.secondLast().schema.getMinItems())
                    : Optional.empty();
            Optional<Integer> maxItems = isArray ? Optional.ofNullable(schemaPath.secondLast().schema.getMaxItems())
                    : Optional.empty();
            if (Util.isObject(schema) || Util.isMap(schema) || Util.isEnum(schema) || Util.isOneOf(schema)
                    || Util.isAnyOf(schema) || Util.isAllOf(schema)) {
                Optional<Cls> previous = Optional.ofNullable(stack.peek());
                stack.push(cls);
                updateLinks(cls, previous);
                final Optional<String> fieldName;
                if (previous.isPresent()) {
                    fieldName = Optional.of(previous.get().nextFieldName(last.name));
                    // Now get the wrapping class name using the field name and avoiding collisions
                    // both with the owning class heirarchy and with siblings
                    String candidate = previous.get().fullClassName + "."
                            + Names.simpleClassNameFromSimpleName(fieldName.get());
                    cls.fullClassName = resolveCandidateFullClassName(cls, candidate);
                } else {
                    fieldName = Optional.empty();
                    String candidate = names.schemaNameToFullClassName(cls.category, last.name);
                    cls.fullClassName = candidate;
                }
                if (Util.isEnum(schema)) {
                    handleEnum(schemaPath, cls, previous, isArray, fieldName, names);
                } else if (Util.isObject(schema)) {
                    handleObject(schemaPath, last, schema, cls, isArray, previous, fieldName);
                } else if (Util.isOneOf(schema) || Util.isAnyOf(schema) || Util.isAllOf(schema)) {
                    handlePolymorphism(schemaPath, cls, names, previous, fieldName, isArray);
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
                if (Util.isPrimitive(schema)) {
                    Class<?> c = Util.toClass(schema.getType(), schema.getFormat(), names.mapIntegerToBigInteger());
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
                    Encoding encoding = encoding(schema);
                    Optional<BigDecimal> min = Optional.ofNullable(schema.getMinimum());
                    Optional<BigDecimal> max = Optional.ofNullable(schema.getMaximum());
                    boolean exclusiveMin = orElse(schema.getExclusiveMinimum(), false);
                    boolean exclusiveMax = orElse(schema.getExclusiveMaximum(), false);
                    current.addField(fullClassName, last.name, fieldName, required, isArray, minItems, maxItems,
                            minLength, maxLength, pattern, min, max, exclusiveMin, exclusiveMax, encoding);
                } else if (Util.isRef(schema)) {
                    fullClassName = names.refToFullClassName(schema.get$ref());
                    final String fieldNameCandidate = orElse(last.name, Names.simpleClassName(fullClassName));
                    String fieldName = current.nextFieldName(fieldNameCandidate);
                    boolean required = fieldIsRequired(schemaPath);
                    current.addField(fullClassName, last.name, fieldName, required, isArray);
                } else {
                    throw new RuntimeException("unexpected");
                }
            }
        }

        @Override
        public void finishSchema(SchemaCategory category, ImmutableList<SchemaWithName> schemaPath) {
            final Cls cls = stack.peek();
            if (Apis.isComplexSchema(schemaPath.last().schema) || Util.isEnum(schemaPath.last().schema)
                    || schemaPath.size() == 1) {
                stack.pop();
                if (stack.isEmpty()) {
                    this.results.add(new Result(cls, schemaPath.first().name));
                }
            }
        }

        List<Result> results() {
            return results;
        }

        static final class Result {
            final Cls cls;
            final String name;

            Result(Cls cls, String name) {
                this.cls = cls;
                this.name = name;
            }
        }
    }

    private static Encoding encoding(Schema<?> schema) {
        if ("binary".equals(schema.getFormat())) {
            return Encoding.OCTET;
        } else {
            return Encoding.DEFAULT;
        }
    }

    private static void updateLinks(final Cls cls, Optional<Cls> previous) {
        previous.ifPresent(p -> {
            p.classes.add(cls);
            cls.owner = Optional.of(p);
        });
    }

    private static void handleObject(ImmutableList<SchemaWithName> schemaPath, SchemaWithName last, Schema<?> schema,
            final Cls cls, boolean isArray, Optional<Cls> previous, final Optional<String> fieldName) {
        cls.classType = ClassType.CLASS;
        cls.hasProperties = Util.isObject(schema);
        boolean required = fieldIsRequired(schemaPath);
        previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required, isArray));
    }

    enum Encoding {
        DEFAULT, OCTET;
    }

    private static boolean isString(Schema<?> schema) {
        return "string".equals(schema.getType());
    }

    private static boolean fieldIsRequired(ImmutableList<SchemaWithName> schemaPath) {
        SchemaWithName last = schemaPath.last();
        if (schemaPath.size() <= 1) {
            return Util.isPrimitive(last.schema) || Util.isRef(last.schema) || Util.isArray(last.schema);
        } else {
            return contains(schemaPath.secondLast().schema.getRequired(), last.name)
                    || Util.isAllOf(schemaPath.secondLast().schema) || Util.isArray(schemaPath.secondLast().schema);
        }
    }

    private static void handlePolymorphism(ImmutableList<SchemaWithName> schemaPath, Cls cls, Names names,
            Optional<Cls> previous, Optional<String> fieldName, boolean isArray) {
        SchemaWithName last = schemaPath.last();
        cls.polymorphicType = polymorphicType(last.schema);
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
        }
        if (cls.polymorphicType == PolymorphicType.ONE_OF || cls.polymorphicType == PolymorphicType.ANY_OF) {
            if (discriminator != null) {
                cls.classType = ClassType.ONE_OR_ANY_OF_DISCRIMINATED;
            } else {
                cls.classType = ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED;
            }
        } else {
            cls.classType = ClassType.ALL_OF;
        }
        boolean required = fieldIsRequired(schemaPath);
        previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required, isArray));
    }

    private static PolymorphicType polymorphicType(Schema<?> schema) {
        final PolymorphicType pt;
        if (Util.isOneOf(schema)) {
            pt = PolymorphicType.ONE_OF;
        } else if (Util.isAnyOf(schema)) {
            pt = PolymorphicType.ANY_OF;
        } else {
            pt = PolymorphicType.ALL_OF;
        }
        return pt;
    }

    static final class Discriminator {
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

    private static void handleEnum(ImmutableList<SchemaWithName> schemaPath, Cls cls, Optional<Cls> previous,
            boolean isArray, Optional<String> fieldName, Names names) {
        Schema<?> schema = schemaPath.last().schema;
        cls.classType = ClassType.ENUM;
        Class<?> valueCls = Util.toClass(schema.getType(), schema.getFormat(), names.mapIntegerToBigInteger());
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
        boolean required = fieldIsRequired(schemaPath);
        previous.ifPresent(
                p -> p.addField(cls.fullClassName, schemaPath.last().name, fieldName.get(), required, isArray));
    }

    private static <T> boolean contains(Collection<? extends T> collection, T t) {
        return collection != null && t != null && collection.contains(t);
    }

    private static String resolvedTypeNullable(Field f, Imports imports) {
        if (f.encoding == Encoding.OCTET) {
            return imports.add(String.class);
        } else if (f.isArray) {
            return toList(f.fullClassName, imports, false);
        } else if (f.required) {
            return imports.add(Util.toPrimitive(f.fullClassName));
        } else {
            return imports.add(f.fullClassName);
        }
    }

    private static String toList(String fullClassName, Imports imports, boolean useOptional) {
        if (useOptional) {
            return String.format("%s<%s<%s>>", imports.add(Optional.class), imports.add(List.class),
                    imports.add(fullClassName));
        } else {
            return String.format("%s<%s>", imports.add(List.class), imports.add(fullClassName));
        }
    }

    private static String resolvedType(Field f, Imports imports) {
        if (f.isOctets()) {
            return "byte[]";
        } else if (f.isArray) {
            return toList(f.fullClassName, imports, !f.required);
        } else if (f.required) {
            return imports.add(Util.toPrimitive(f.fullClassName));
        } else {
            return imports.add(Optional.class) + "<" + imports.add(f.fullClassName) + ">";
        }
    }

    private static String resolveCandidateFullClassName(Cls cls, String candidateFullClassName) {
        String s = candidateFullClassName;
        Set<String> ownersAndSiblings = cls.ownersAndSiblingsSimpleNames();
        if (ownersAndSiblings.contains(Names.simpleClassName(s)) || s.equals(cls.fullClassName)) {
            int i = 2;
            while (ownersAndSiblings.contains(Names.simpleClassName(s + i))) {
                i++;
            }
            s = s + i;
        }
        return s;
    }

    private static ClassType classType(Schema<?> schema) {
        if (Util.isOneOf(schema) || Util.isAnyOf(schema)) {
            return ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED;
        } else if (Util.isEnum(schema)) {
            return ClassType.ENUM;
        } else if (Util.isArray(schema)) {
            return ClassType.ARRAY_WRAPPER;
        } else {
            return ClassType.CLASS;
        }
    }

}
