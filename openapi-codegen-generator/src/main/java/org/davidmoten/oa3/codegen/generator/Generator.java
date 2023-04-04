package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.generator.internal.Util.orElse;
import static org.davidmoten.oa3.codegen.runtime.internal.Util.toPrimitive;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import java.util.Set;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.internal.ImmutableList;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.LinkedStack;
import org.davidmoten.oa3.codegen.runtime.internal.PolymorphicType;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
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

        CodeWriter.writeGlobalsClass(names);

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
            Imports imports = result.imports;
            String schemaName = result.name;
            CodeWriter.writeSchemaClass(names, fullClassNameInterfaces, cls, imports, schemaName);
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

        public String pkg() {
            return Names.pkg(fullClassName);
        }

        public String simpleName() {
            return Names.simpleClassName(fullClassName);
        }

        public boolean unwrapSingleField() {
            return !hasProperties && (classType == ClassType.ENUM || classType == ClassType.ARRAY_WRAPPER
                    || (topLevel && fields.size() == 1));
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
        String fullClassName;
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

    static final class MyVisitor implements Visitor {
        private final Names names;
        private Imports imports;
        private LinkedStack<Cls> stack = new LinkedStack<>();
        private List<Result> results = new ArrayList<>();

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
                Optional<Cls> previous = Optional.ofNullable(stack.peek());
                previous.ifPresent(c -> c.classes.add(cls));
                if (previous.isPresent()) {
                    Optional<String> fieldName = Optional.of(previous.get().nextFieldName(last.name));
                    String fullClassName = previous.get().fullClassName + "."
                            + Names.simpleClassNameFromSimpleName(fieldName.get());
                    cls.fullClassName = fullClassName;
                    boolean required = fieldIsRequired(schemaPath);
                    previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required,
                            previous.get().classType == ClassType.ARRAY_WRAPPER));
                } else {
                    cls.fullClassName = names.schemaNameToClassName(last.name);
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
            if (isObject(schema) || isMap(schema) || isEnum(schema) || isOneOf(schema) || isAnyOf(schema)
                    || isAllOf(schema)) {
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
                    handleEnum(schemaPath, cls, previous, isArray, fieldName);
                } else if (isObject(schema)) {
                    handleObject(schemaPath, last, schema, cls, isArray, previous, fieldName);
                } else if (isOneOf(schema) || isAnyOf(schema) || isAllOf(schema)) {
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
                    final String fieldNameCandidate;
                    if (last.name == null) {
                        fieldNameCandidate = Names.simpleClassName(fullClassName);
                    } else {
                        fieldNameCandidate = last.name;
                    }
                    String fieldName = current.nextFieldName(fieldNameCandidate);
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
                    this.results.add(new Result(cls, imports, schemaPath.first().name));
                }
            }
        }

        public List<Result> results() {
            return results;
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

    enum Encoding {
        DEFAULT, OCTET;
    }

    private static boolean isString(Schema<?> schema) {
        return "string".equals(schema.getType());
    }

    private static boolean fieldIsRequired(ImmutableList<SchemaWithName> schemaPath) {
        SchemaWithName last = schemaPath.last();
        if (schemaPath.size() <= 1) {
            return isPrimitive(last.schema) || isRef(last.schema) || isArray(last.schema);
        } else {
            return contains(schemaPath.secondLast().schema.getRequired(), last.name)
                    || isAllOf(schemaPath.secondLast().schema) || isArray(schemaPath.secondLast().schema);
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
        if (isOneOf(schema)) {
            pt = PolymorphicType.ONE_OF;
        } else if (isAnyOf(schema)) {
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
            boolean isArray, Optional<String> fieldName) {
        Schema<?> schema = schemaPath.last().schema;
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
            return imports.add(toPrimitive(f.fullClassName));
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
            return imports.add(toPrimitive(f.fullClassName));
        } else {
            return imports.add(Optional.class) + "<" + imports.add(f.fullClassName) + ">";
        }
    }

    private static boolean isEnum(Schema<?> schema) {
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

    private static boolean isOneOf(Schema<?> schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        ComposedSchema sch = (ComposedSchema) schema;
        return sch.getOneOf() != null && !sch.getOneOf().isEmpty();
    }

    private static boolean isAnyOf(Schema<?> schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        ComposedSchema sch = (ComposedSchema) schema;
        return sch.getAnyOf() != null && !sch.getAnyOf().isEmpty();
    }

    private static boolean isAllOf(Schema<?> schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        ComposedSchema sch = (ComposedSchema) schema;
        return sch.getAllOf() != null && !sch.getAllOf().isEmpty();
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
