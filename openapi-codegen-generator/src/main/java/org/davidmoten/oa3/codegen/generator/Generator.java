package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.util.Util.orElse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
import org.davidmoten.oa3.codegen.generator.writer.SchemasCodeWriter;
import org.davidmoten.oa3.codegen.runtime.PolymorphicType;
import org.davidmoten.oa3.codegen.util.ImmutableList;
import org.openapitools.jackson.nullable.JsonNullable;

import com.github.davidmoten.guavamini.Preconditions;
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

        for (MyVisitor.Result result : v.results()) {
            Cls cls = result.cls;
            String schemaName = result.name;
            if ((definition.includeSchemas().isEmpty() || definition.includeSchemas().contains(schemaName))
                    && !definition.excludeSchemas().contains(schemaName)) {
                names.registerCls(cls);
            }
        }
        
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

    public static final class Cls {
        public SchemaCategory category;
        public String fullClassName;
        public Optional<String> description = Optional.empty();
        public ClassType classType;
        public List<Field> fields = new ArrayList<>();
        public List<EnumMember> enumMembers = new ArrayList<>();
        public List<String> enumNames = Collections.emptyList();
        public List<Cls> classes = new ArrayList<>();
        public Discriminator discriminator = null;
        public String enumValueFullType;
        public boolean topLevel = false;
        public boolean hasProperties = false;
        public PolymorphicType polymorphicType;
        public Optional<Cls> owner = Optional.empty(); // the owning heirarchy, we cannot name our class any one of
        // these classes (disallowed by java)
        public Optional<String> name = Optional.empty();
        public Optional<Schema<?>> schema = Optional.empty();

        public String nextAnonymousFieldName() {
            num++;
            return "option" + num;
        }

        private int num = 0;
        private Set<String> fieldNames = new HashSet<>();

        public String nextFieldName(String name, Schema<?> schema) {
            Optional<String> nameOverride = extensionString(schema, ExtensionKeys.NAME);
            if (nameOverride.isPresent()) {
            	name = nameOverride.get();
            }
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
                    if (!fieldNames.contains(a)) {
                        break;
                    }
                    i++;
                }
                next = a;
            }
            fieldNames.add(next);
            return next;
        }

        public String fieldName(Field f) {
            if (unwrapSingleField()) {
                return "value";
            } else {
                return f.fieldName;
            }
        }

        void addField(String fullType, String name, String fieldName, boolean required, boolean isArray,
                Optional<MapType> mapType, boolean nullable, boolean readOnly, boolean writeOnly) {
            addField(fullType, name, fieldName, required, isArray, Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, false,
                    Encoding.DEFAULT, mapType, nullable, readOnly, writeOnly);
        }
        
        void addField(String fullType, String name, String fieldName, boolean required, boolean isArray,
                Optional<MapType> mapType, boolean nullable) {
            addField(fullType, name, fieldName, required, isArray, Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, false,
                    Encoding.DEFAULT, mapType, nullable, false, false);
        }

        void addField(String fullType, String name, String fieldName, boolean required, boolean isArray,
                Optional<Integer> minItems, Optional<Integer> maxItems, Optional<Integer> minLength,
                Optional<Integer> maxLength, Optional<String> pattern, Optional<BigDecimal> min,
                Optional<BigDecimal> max, boolean exclusiveMin, boolean exclusiveMax, Encoding encoding,
                Optional<MapType> mapType, boolean nullable, boolean readOnly, boolean writeOnly) {
            fields.add(new Field(fullType, name, fieldName, required, isArray, minItems, maxItems, minLength, maxLength,
                    pattern, min, max, exclusiveMin, exclusiveMax, encoding, mapType, nullable, readOnly, writeOnly));
//            if (WriterUtil.DEBUG && mapType.isPresent()) {
//                System.err.println(fullClassName);
//                System.err.println(fields.get(fields.size() - 1));
//                Thread.dumpStack();
//            }
        }

        public String pkg() {
            return Names.pkg(fullClassName);
        }

        public String simpleName() {
            return Names.simpleClassName(fullClassName);
        }

        public boolean unwrapSingleField() {
            return !hasProperties
                    && (classType == ClassType.ENUM || classType == ClassType.ARRAY_WRAPPER
                            || topLevel && fields.size() == 1)
                    || classType == ClassType.ONE_OF_NON_DISCRIMINATED;
        }

        public Set<String> ownersAndSiblingsSimpleNames() {
            Cls c = this;
            Set<String> set = new HashSet<>();
            while (c.owner.isPresent()) {
                set.add(c.owner.get().simpleName());
                c = c.owner.get();
            }
            classes.stream().filter(x -> x.fullClassName != null).forEach(x -> set.add(x.simpleName()));
            return set;
        }

        public boolean isNullableEnum() {
            return !enumMembers.isEmpty() && enumMembers.get(0).nullable;
        }

        public boolean hasEnumNullValue() {
            return enumMembers.stream().anyMatch(x -> x.parameter == null);
        }
        
        public boolean hasEncoding() {
            return schema.isPresent() //
                    && schema.get().getExtensions() != null //
                    && Boolean.TRUE.equals(extension(schema.get(), ExtensionKeys.HAS_ENCODING).orElse(null));
        }
    }
    
    private static Optional<Object> extension(Schema<?> schema, String key) {
    	Preconditions.checkNotNull(key);
    	Map<String, Object> map = schema.getExtensions();
    	if (map == null) {
    		return Optional.empty();
    	} else {
    		return Optional.ofNullable(map.get(key));
    	}
    }
    
    @SuppressWarnings("unchecked")
	public static Optional<String> extensionString(Schema<?> schema, String key) {
    	return (Optional<String>) (Optional<?>) extension(schema, key);
    }

    public static class EnumMember {
        public final String name;
        public final Object parameter;
        public final boolean nullable;

        public EnumMember(String name, Object parameter, boolean nullable) {
            this.name = name;
            this.parameter = parameter;
            this.nullable = nullable;
        }
    }

    public enum ClassType {
        CLASS("class"), //
        ENUM("enum"), //
        ONE_OR_ANY_OF_DISCRIMINATED("interface"), //
        ONE_OF_NON_DISCRIMINATED("class"), //
        ANY_OF_NON_DISCRIMINATED("class"), //
        ALL_OF("class"), //
        ARRAY_WRAPPER("class");

        private final String word;

        ClassType(String word) {
            this.word = word;
        }

        public String word() {
            return word;
        }
    }

    private static final Set<String> PRIMITIVE_CLASS_NAMES = Sets.of("int", "long", "byte", "float", "double",
            "boolean", "short");

    public enum MapType {
        ADDITIONAL_PROPERTIES, FIELD;
    }

    
    
    public final static class Field {
        public final String fullClassName;
        public final String name;
        public final String fieldName;

        private static final Set<String> NUMERIC_CLASS_NAMES = Sets.of("int", "long", "float", "double", "short", //
                Integer.class.getCanonicalName(), //
                Long.class.getCanonicalName(), //
                Float.class.getCanonicalName(), //
                Double.class.getCanonicalName(), //
                Short.class.getCanonicalName(), //
                BigInteger.class.getCanonicalName(), //
                BigDecimal.class.getCanonicalName());
        
        // note that when isArray is true, required does not apply to the arrray item
        // but rather to the array itself (as an object property for example)
        public final boolean required;
        public final Optional<Integer> minLength;
        public final Optional<Integer> maxLength;
        public final Optional<String> pattern;
        public final Optional<BigDecimal> min;
        public final Optional<BigDecimal> max;
        public final boolean isArray; // if a List to be used to represent
        public final Encoding encoding;
        public final boolean exclusiveMin;
        public final boolean exclusiveMax;
        public final Optional<Integer> minItems;
        public final Optional<Integer> maxItems;
        public final Optional<MapType> mapType;
        public final boolean readOnly;
        public final boolean writeOnly;

        // note that when isArray is true, nullable refers to the array item
        public final boolean nullable;

        Field(String fullClassName, String name, String fieldName, boolean required, boolean isArray,
                Optional<Integer> minItems, Optional<Integer> maxItems, Optional<Integer> minLength,
                Optional<Integer> maxLength, Optional<String> pattern, Optional<BigDecimal> min,
                Optional<BigDecimal> max, boolean exclusiveMin, boolean exclusiveMax, Encoding encoding,
                Optional<MapType> mapType, boolean nullable, boolean readOnly, boolean writeOnly) {
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
            this.mapType = mapType;
            this.nullable = nullable;
            this.readOnly = readOnly;
            this.writeOnly = writeOnly;
        }

        public String fieldName(Cls cls) {
            return cls.fieldName(this);
        }

        public Optional<String> resolvedTypePublicConstructorNonOptional(Imports imports) {
            if (isOctets()) {
                if (required) {
                    return Optional.empty();
                } else {
                    return Optional.of("byte[]");
                }
            } else if (isArray) {
                return Optional.empty();
            } else if (nullable) {
                return Optional.of(imports.add(fullClassName));
            } else if (required) {
                return Optional.empty();
            } else {
                return Optional.of(imports.add(Util.toPrimitive(fullClassName)));
            }
        }
        
        public String resolvedType(Imports imports) {
            if (mapType.isPresent()) {
                return resolvedTypeMapPublic(imports);
            } else {
                return resolvedTypePublicConstructor(imports);
            }
        }
        
        public String resolvedTypePublicConstructor(Imports imports) {
            if (isOctets()) {
                if (isArray) {
                    if (required) {
                        return String.format("%s<byte[]>", imports.add(List.class));
                    } else {
                        return String.format("%s<%s<byte[]>>",imports.add(Optional.class), imports.add(List.class));
                    }
                } else if (!required && nullable) {
                    return String.format("%s<%s>", imports.add(JsonNullable.class), "byte[]");
                } else if (!required || nullable) {
                    return String.format("%s<%s>", imports.add(Optional.class), "byte[]");
                } else {
                    return "byte[]";
                }  
            } else if (isArray) {
                if (nullable) {
                    return String.format("%s<%s<%s>>", imports.add(List.class), imports.add(JsonNullable.class),
                            imports.add(fullClassName));
                } else {
                    return toList(fullClassName, imports, !required);
                }
            } else if (nullable) {
                if (required) {
                    return String.format("%s<%s>", imports.add(Optional.class), imports.add(fullClassName));
                } else {
                    return String.format("%s<%s>", imports.add(JsonNullable.class), imports.add(fullClassName));
                }
            } else if (required) {
                return imports.add(Util.toPrimitive(fullClassName));
            } else {
                return imports.add(Optional.class) + "<" + imports.add(fullClassName) + ">";
            }
        }

        private String resolvedTypeMapIsArray(Imports imports, final String t) {
            if (nullable) {
                return String.format("%s<%s<%s<%s, %s>>>", imports.add(List.class), imports.add(JsonNullable.class),
                        imports.add(Map.class), imports.add(String.class), t);
            } else {
                return String.format("%s<%s<%s, %s>>", imports.add(List.class), imports.add(Map.class),
                        imports.add(String.class), t);
            }
        }

        public String resolvedTypeMapPublic(Imports imports) {
            final String t;
            if (isOctets()) {
                t = "byte[]";
            } else {
                if (isMapType(MapType.ADDITIONAL_PROPERTIES) && nullable) {
                    t = String.format("%s<%s>", imports.add(JsonNullable.class), imports.add(fullClassName));
                } else {
                    t = imports.add(fullClassName);
                }
            }
            if (isArray) {
                return resolvedTypeMapIsArray(imports, t);
            } else {
                if (nullable && !isMapType(MapType.ADDITIONAL_PROPERTIES)) {
                    if (required) {
                        return String.format("%s<%s<%s, %s>>", imports.add(Optional.class), imports.add(Map.class),
                                imports.add(String.class), t);
                    } else {
                        return String.format("%s<%s<%s, %s>>", imports.add(JsonNullable.class), imports.add(Map.class),
                                imports.add(String.class), t);
                    }
                } else if (required) {
                    return String.format("%s<%s, %s>", imports.add(Map.class), imports.add(String.class), t);
                } else {
                    return String.format("%s<%s<%s, %s>>", imports.add(Optional.class), imports.add(Map.class),
                            imports.add(String.class), t);
                }
            }
        }

        public boolean isPrimitive() {
            return required && PRIMITIVE_CLASS_NAMES.contains(Util.toPrimitive(fullClassName));
        }

        public boolean isOctets() {
            return encoding == Encoding.OCTET;
        }

        public boolean isByteArray() {
            return fullClassName.equals("byte[]");
        }

        public boolean isAdditionalProperties() {
            return mapType.equals(Optional.of(MapType.ADDITIONAL_PROPERTIES));
        }

        public boolean isMapType(MapType mt) {
            Preconditions.checkNotNull(mt);
            return this.mapType.equals(Optional.of(mt));
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Field [fullClassName=");
            builder.append(fullClassName);
            builder.append(", name=");
            builder.append(name);
            builder.append(", fieldName=");
            builder.append(fieldName);
            builder.append(", required=");
            builder.append(required);
            builder.append(", isArray=");
            builder.append(isArray);
            builder.append(", mapType=");
            builder.append(mapType);
            builder.append(", nullable=");
            builder.append(nullable);
            builder.append("]");
            return builder.toString();
        }

        public boolean isDateOrTime() {
            return fullClassName.equals(LocalDate.class.getCanonicalName())
                    || fullClassName.equals(OffsetDateTime.class.getCanonicalName());
        }

        public boolean isNumber() {
            return NUMERIC_CLASS_NAMES.contains(fullClassName);
        }
    }
    
    public static final class MyVisitor implements Visitor {
        private final Names names;
        private final LinkedStack<Cls> stack = new LinkedStack<>();
        private final List<Result> results = new ArrayList<>();

        public MyVisitor(Names names) {
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
                    Optional<String> fieldName = Optional.of(previous.get().nextFieldName(last.name, schema));
                    String candidate = previous.get().fullClassName + "."
                            + Names.simpleClassNameFromSimpleName(fieldName.get());
                    cls.fullClassName = resolveCandidateFullClassName(cls, candidate);
                    boolean required = fieldIsRequired(schemaPath);
                    previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required,
                            previous.get().classType == ClassType.ARRAY_WRAPPER, mapType(schemaPath),
                            isNullable(schema)));
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
                    fieldName = Optional.of(previous.get().nextFieldName(last.name, schema));
                    // Now get the wrapping class name using the field name and avoid collisions
                    // both with the owning class heirarchy and with siblings
                    String candidate = previous.get().fullClassName + "."
                            + Names.simpleClassNameFromSimpleName(fieldName.get());
                    String candidate2 = resolveCandidateFullClassName(cls, candidate);
                    cls.fullClassName = resolveCandidateFullClassName(cls.owner.get(), candidate2);
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
                boolean readOnly = Boolean.TRUE.equals(schema.getReadOnly());
                boolean writeOnly = Boolean.TRUE.equals(schema.getWriteOnly());
                if (Util.isPrimitive(schema)) {
                    Class<?> c = Util.toClass(Util.getTypeOrThrow(schema), schema.getFormat(), schema.getExtensions(),
                            names.mapIntegerToBigInteger(), names.mapNumberToBigDecimal());
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
                    String fieldName = schemaPath.size() == 1 ? "value" : current.nextFieldName(last.name, schema);
                    boolean required = fieldIsRequired(schemaPath);
                    Encoding encoding = encoding(schema);
                    Optional<BigDecimal> min = Optional.ofNullable(schema.getMinimum());
                    Optional<BigDecimal> max = Optional.ofNullable(schema.getMaximum());
                    boolean exclusiveMin = orElse(schema.getExclusiveMinimum(), false);
                    boolean exclusiveMax = orElse(schema.getExclusiveMaximum(), false);
                    current.addField(fullClassName, last.name, fieldName, required, isArray, minItems, maxItems,
                            minLength, maxLength, pattern, min, max, exclusiveMin, exclusiveMax, encoding,
                            mapType(schemaPath), isNullable(schema), readOnly, writeOnly);
                } else if (Util.isRef(schema)) {
                    fullClassName = names.refToFullClassName(schema.get$ref());
                    final String fieldNameCandidate = orElse(last.name, Names.simpleClassName(fullClassName));
                    String fieldName = current.nextFieldName(fieldNameCandidate, schema);
                    boolean required = fieldIsRequired(schemaPath);
                    // TODO pick up other constraints
                    current.addField(fullClassName, last.name, fieldName, required, isArray, minItems, maxItems,
                            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                            false, false, Encoding.DEFAULT, mapType(schemaPath), isNullable(schema), false, false);
                } else {
                    // any object
                    String fieldName = current.nextFieldName(last.name, schema);
                    boolean required = fieldIsRequired(schemaPath);
                    current.addField(Object.class.getCanonicalName(), last.name, fieldName, required, isArray,
                            mapType(schemaPath), isNullable(schema));
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

    private static boolean isNullable(Schema<?> schema) {
        return Boolean.TRUE.equals(schema.getNullable())
                || schema.getTypes() != null && schema.getTypes().contains("null");
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
        if (!cls.schema.isPresent()) {
            // make sure that cls.schema.extensions is available for inspection
            cls.schema = Optional.of(schema);
        }
        boolean required = fieldIsRequired(schemaPath);
        Optional<MapType> mt = mapType(schemaPath);
        if (mt.isPresent() && mt.get() == MapType.FIELD) {
            mt = Optional.empty();
        }
        Optional<MapType> mt2 = mt;
        previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required, isArray, mt2,
                isNullable(schema)));
    }

    public enum Encoding {
        DEFAULT, OCTET;
    }

    private static boolean isString(Schema<?> schema) {
        return "string".equals(Util.getType(schema).orElse("object"));
    }

    private static boolean fieldIsRequired(ImmutableList<SchemaWithName> schemaPath) {
        SchemaWithName last = schemaPath.last();
        if (schemaPath.size() <= 1) {
            return Util.isPrimitive(last.schema) || Util.isRef(last.schema) || Util.isArray(last.schema);
        } else {
            return contains(schemaPath.secondLast().schema.getRequired(), last.name)
                    || Util.isAllOf(schemaPath.secondLast().schema) || Util.isArray(schemaPath.secondLast().schema)
                    // or is additional properties schema
                    || mapType(schemaPath).equals(Optional.of(MapType.ADDITIONAL_PROPERTIES));
        }
    }

    private static Optional<MapType> mapType(ImmutableList<SchemaWithName> schemaPath) {
        Schema<?> schema = schemaPath.last().schema;
        if (schemaPath.size() > 1
                && schemaPath.secondLast().schema.getAdditionalProperties() == schemaPath.last().schema) {
            return Optional.of(MapType.ADDITIONAL_PROPERTIES);
        } else if (Util.isMap(schema) || allNulls(schema)) {
            return Optional.of(MapType.FIELD);
        } else {
            return Optional.empty();
        }
    }

    private static boolean allNulls(Schema<?> s) {
        return s.getClass().equals(Schema.class) && !Util.getType(s).isPresent() && s.getProperties() == null
                && s.getAdditionalProperties() == null && s.get$ref() == null && s.getAdditionalItems() == null;
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
                if (cls.polymorphicType == PolymorphicType.ONE_OF) {
                    cls.classType = ClassType.ONE_OF_NON_DISCRIMINATED;
                } else {
                    cls.classType = ClassType.ANY_OF_NON_DISCRIMINATED;
                }
            }
        } else {
            cls.classType = ClassType.ALL_OF;
        }
        boolean required = fieldIsRequired(schemaPath);
        previous.ifPresent(p -> p.addField(cls.fullClassName, last.name, fieldName.get(), required, isArray,
                mapType(schemaPath), isNullable(schemaPath.last().schema)));
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

    public static final class Discriminator {
        public final String propertyName;
        public final String fieldName;
        public final Map<String, String> fullClassNameToPropertyValue;

        public Discriminator(String propertyName, String fieldName, Map<String, String> fullClassNameToPropertyValue) {
            this.propertyName = propertyName;
            this.fieldName = fieldName;
            this.fullClassNameToPropertyValue = fullClassNameToPropertyValue;
        }

        public String discriminatorValueFromFullClassName(String fullClassName) {
            String value = fullClassNameToPropertyValue.get(fullClassName);
            if (value == null) {
                // TODO review using simple class name for value because collision risk, better
                // to use $ref value like '#/components/schemas/Oval'
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
        if (!cls.schema.isPresent()) {
            // ensure schema extensions visible
            cls.schema = Optional.of(schema);
        }
        
        Class<?> valueCls = Util.toClass(Util.getTypeOrThrow(schema), schema.getFormat(), schema.getExtensions(),
                names.mapIntegerToBigInteger(), names.mapNumberToBigDecimal());
        cls.enumValueFullType = valueCls.getCanonicalName();
        Map<String, String> map = Names.getEnumValueToIdentifierMap(schema.getEnum());
        Set<String> used = new HashSet<>();
        for (Object o : schema.getEnum()) {
            if (!used.contains(String.valueOf(o))) {
                cls.enumMembers.add(new EnumMember(map.get(String.valueOf(o)), o, isNullable(schema)));
                used.add(String.valueOf(o));
            }
        }
        if (schema.getExtensions() != null && schema.getExtensions().get(ExtensionKeys.NAMES) != null) {
            @SuppressWarnings("unchecked")
            List<String> a = (List<String>) schema.getExtensions().get(ExtensionKeys.NAMES);
            if (a.size() == cls.enumMembers.size()) {
                cls.enumNames = a;
            } else {
                System.out.println("[WARN] " + ExtensionKeys.NAMES + " array length must match number of enum members");
            }
        }
        cls.addField(cls.enumValueFullType, "value", "value", true, false, mapType(schemaPath), isNullable(schema));
        boolean required = fieldIsRequired(schemaPath);
        previous.ifPresent(p -> p.addField(cls.fullClassName, schemaPath.last().name, fieldName.get(), required,
                isArray, mapType(schemaPath), isNullable(schema)));
    }

    private static <T> boolean contains(Collection<? extends T> collection, T t) {
        return collection != null && t != null && collection.contains(t);
    }

    private static String toList(String fullClassName, Imports imports, boolean useOptional) {
        if (useOptional) {
            return String.format("%s<%s<%s>>", imports.add(Optional.class), imports.add(List.class),
                    imports.add(fullClassName));
        } else {
            return String.format("%s<%s>", imports.add(List.class), imports.add(fullClassName));
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
        if (Util.isOneOf(schema)) {
            return ClassType.ONE_OF_NON_DISCRIMINATED;
        } if (Util.isAnyOf(schema)) {
            return ClassType.ANY_OF_NON_DISCRIMINATED;
        } else if (Util.isEnum(schema)) {
            return ClassType.ENUM;
        } else if (Util.isArray(schema)) {
            return ClassType.ARRAY_WRAPPER;
        } else {
            return ClassType.CLASS;
        }
    }

}
