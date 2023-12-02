package org.davidmoten.oa3.codegen.generator.writer;

import static org.davidmoten.oa3.codegen.generator.internal.Util.toPrimitive;
import static org.davidmoten.oa3.codegen.generator.internal.WriterUtil.IMPORTS_HERE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.davidmoten.oa3.codegen.generator.Generator.ClassType;
import org.davidmoten.oa3.codegen.generator.Generator.Cls;
import org.davidmoten.oa3.codegen.generator.Generator.Discriminator;
import org.davidmoten.oa3.codegen.generator.Generator.Field;
import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.SchemaCategory;
import org.davidmoten.oa3.codegen.generator.ServerGeneratorType;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.generator.internal.Javadoc;
import org.davidmoten.oa3.codegen.generator.internal.Mutable;
import org.davidmoten.oa3.codegen.generator.internal.WriterUtil;
import org.davidmoten.oa3.codegen.http.HasEncoding;
import org.davidmoten.oa3.codegen.http.HasStringValue;
import org.davidmoten.oa3.codegen.runtime.AnyOfDeserializer;
import org.davidmoten.oa3.codegen.runtime.AnyOfMember;
import org.davidmoten.oa3.codegen.runtime.AnyOfSerializer;
import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.runtime.DiscriminatorHelper;
import org.davidmoten.oa3.codegen.runtime.JsonNullableOctetsDeserializer;
import org.davidmoten.oa3.codegen.runtime.JsonNullableOctetsSerializer;
import org.davidmoten.oa3.codegen.runtime.NullEnumDeserializer;
import org.davidmoten.oa3.codegen.runtime.OctetsDeserializer;
import org.davidmoten.oa3.codegen.runtime.OctetsSerializer;
import org.davidmoten.oa3.codegen.runtime.OptionalOctetsDeserializer;
import org.davidmoten.oa3.codegen.runtime.OptionalOctetsSerializer;
import org.davidmoten.oa3.codegen.runtime.PolymorphicDeserializer;
import org.davidmoten.oa3.codegen.runtime.PolymorphicType;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.runtime.RuntimeUtil;
import org.davidmoten.oa3.codegen.util.Util;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.boot.context.properties.ConstructorBinding;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.davidmoten.guavamini.Maps;

public final class SchemasCodeWriter {

    private SchemasCodeWriter() {
        // prevent instantiation
    }

    public static void writeSchemaClass(Names names, Map<String, Set<Cls>> fullClassNameInterfaces, Cls cls,
            String schemaName) {
        if ((cls.category == SchemaCategory.PATH || cls.category == SchemaCategory.RESPONSE) && cls.schema.isPresent()
                && cls.schema.get().get$ref() != null) {
            // when a cls has a ref and is used with a Path or Response then the ref class
            // is used in generated code
            return;
        }
        CodePrintWriter out = CodePrintWriter.create(cls.fullClassName, names.simpleNameInPackage(cls.fullClassName));
        if (cls.fullClassName.equals("test.schema.StoredValueBalanceMergeRequest")) {
            System.out.println("here");
        }
        SchemasCodeWriter.writeClass(out, cls, fullClassNameInterfaces, names);
        WriterUtil.writeContent(names, out);
    }

    public static void writeGlobalsClass(Names names) {
        String fullClassName = names.globalsFullClassName();
        CodePrintWriter out = CodePrintWriter.create(fullClassName, names.simpleNameInPackage(fullClassName));
        out.line("package %s;", Names.pkg(fullClassName));
        out.println();
        out.format("%s", IMPORTS_HERE);
        WriterUtil.addGeneratedAnnotation(out);
        out.line("public final class %s {", Names.simpleClassName(fullClassName));
        out.println();
        out.line("private static volatile %s config = %s.builder().build();", Config.class, Config.class);
        out.println();
        out.line("public static void setConfig(%s configuration) {", Config.class);
        out.line("config = configuration;");
        out.closeParen();
        out.println();
        out.line("public static %s config() {", Config.class);
        out.line("return config;");
        out.closeParen();
        out.closeParen();
        WriterUtil.writeContent(names, out);
    }

    private static void writeClass(CodePrintWriter out, Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces,
            Names names) {
        if (cls.topLevel) {
            out.line("package %s;", cls.pkg());
            out.println();
            out.format("%s", IMPORTS_HERE);
        } 
        // reserve class names in Imports for member classes
        reserveMemberClassNamesInImports(out.imports(), cls);
        writeClassDeclaration(out, cls, fullClassNameInterfaces);
        writeEnumMembers(out, cls);
        if (isPolymorphic(cls)) {
            writePolymorphicClassContent(out, cls, names);
        } else {
            writeFields(out, cls);
            writeConstructor(out, cls, fullClassNameInterfaces, names);
            writeGetters(out, cls, fullClassNameInterfaces);
            writePropertiesMapGetter(out, cls);
            writeMutators(out, cls, fullClassNameInterfaces);
            writeBuilder(out, cls, fullClassNameInterfaces);
        }
        writeEnumCreator(out, cls);
        writeEnumDeserializer(out, cls);
        writeMemberClasses(out, cls, fullClassNameInterfaces, names);
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeEqualsMethod(out, cls);
            writeHashCodeMethod(out, cls);
            writeToStringMethod(out, cls);
        }
        out.closeParen();
    }

    private static void reserveMemberClassNamesInImports(Imports imports, Cls cls) {
        if (cls.classes.isEmpty()) {
            return;
        }
        cls.classes.forEach(c -> reserveMemberClassNamesInImports(imports, c));
        cls.classes.forEach(c -> imports.add(c.fullClassName));
    }

    private static boolean isPolymorphic(Cls cls) {
        return cls.classType == ClassType.ONE_OF_NON_DISCRIMINATED //
                || cls.classType == ClassType.ANY_OF_NON_DISCRIMINATED //
                || cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED //
                || cls.classType == ClassType.ALL_OF;
    }

    private static void addOverrideAnnotation(CodePrintWriter out) {
        out.println();
        out.line("@%s", Override.class);
    }

    private static void writeEnumCreator(CodePrintWriter out, Cls cls) {
        if (cls.classType == ClassType.ENUM) {
            String simpleClassName = Names.simpleClassName(cls.fullClassName);
            out.println();
            out.line("@%s", JsonCreator.class);
            out.line("public static %s fromValue(%s value) {", simpleClassName, Object.class);
            out.line("for (%s x: %s.values()) {", simpleClassName, simpleClassName);
            // be careful because x.value can be primitive
            if (cls.isNullableEnum()) {
                out.line("if (%s.equals(value, x.value.get())) {", Objects.class);
            } else {
                out.line("if (%s.equals(value, x.value)) {", Objects.class);
            }
            out.line("return x;");
            out.closeParen();
            out.closeParen();
            out.line("throw new %s(\"unexpected enum value: '\" + value + \"'\");", IllegalArgumentException.class);
            out.closeParen();
        }
    }

    private static void writeEnumDeserializer(CodePrintWriter out, Cls cls) {
        if (cls.hasEnumNullValue()) {
            String nullValueMemberName = cls.enumMembers.stream().filter(x -> x.parameter == null).map(x -> x.name)
                    .findFirst().get();
            out.println();
            out.line("public static class _Deserializer extends %s<%s> {", NullEnumDeserializer.class, cls.simpleName());
            out.line("protected _Deserializer() {");
            out.line("super(%s.class, %s.class, %s);", cls.simpleName(), out.add(cls.enumValueFullType),
                    nullValueMemberName);
            out.closeParen();
            out.closeParen();
        }
    }

    private static void writeClassDeclaration(CodePrintWriter out, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        String modifier = classModifier(cls);
        Set<Cls> interfaces = fullClassNameInterfaces.get(cls.fullClassName);
        String implementsClause = implementsClause(out.imports(), interfaces, cls);
        //TODO ensure contentType() and value() methods of HasEncoding are annotated with @Override
        
        final boolean javadocExists;
        if (cls.description.isPresent()) {
            String html = WriterUtil.markdownToHtml(cls.description.get());
            javadocExists = Javadoc.printJavadoc(out, out.indent(), html, true);
        } else {
            javadocExists = false;
        }
        if (!javadocExists) {
            out.println();
        }
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeJsonTypeInfoAnnotation(out, cls);
        } else if (cls.classType == ClassType.ONE_OF_NON_DISCRIMINATED || cls.classType == ClassType.ALL_OF) {
            writePolymorphicDeserializerAnnotation(out, cls);
        } else if (cls.classType == ClassType.ANY_OF_NON_DISCRIMINATED) {
            writeAnyOfSerializerAnnotations(out, cls);
        }
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED
                && cls.classType != ClassType.ANY_OF_NON_DISCRIMINATED) {
            writeJsonIncludeAnnotation(out);
            writeAutoDetectAnnotation(out);
        }
        if (cls.classType == ClassType.ENUM && cls.hasEnumNullValue()) {
            writeEnumNullValueDeserializerAnnotation(out, cls);
        }
        if (cls.topLevel) {
            WriterUtil.addGeneratedAnnotation(out);
        }
        out.line("public %s%s %s%s {", modifier, cls.classType.word(), cls.simpleName(), implementsClause);
    }

    private static void writeAnyOfSerializerAnnotations(CodePrintWriter out, Cls cls) {
        out.line("@%s(using = %s._Deserializer.class)", JsonDeserialize.class, cls.simpleName());
        out.line("@%s(using = %s._Serializer.class)", JsonSerialize.class, cls.simpleName());
    }

    private static void writeEnumNullValueDeserializerAnnotation(CodePrintWriter out, Cls cls) {
        out.line("@%s(using = %s._Deserializer.class)", JsonDeserialize.class, cls.simpleName());
    }

    private static void writeJsonIncludeAnnotation(CodePrintWriter out) {
        out.line("@%s(%s.NON_ABSENT)", JsonInclude.class, Include.class);
    }

    private static String classModifier(Cls cls) {
        final String modifier;
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED || cls.classType == ClassType.ENUM) {
            modifier = "";
        } else {
            modifier = cls.topLevel ? "final " : "static final ";
        }
        return modifier;
    }

    private static String implementsClause(Imports imports, Set<Cls> interfaces, Cls cls) {
        interfaces = Util.orElse(interfaces, Collections.emptySet());
        final String implemented;
        if (interfaces.isEmpty() && !cls.hasEncoding()) {
            implemented = "";
        } else {
            Stream<String> a = interfaces //
                    .stream() //
                    .map(x -> x.fullClassName);
            Stream<String> b = Stream.of(HasEncoding.class.getCanonicalName()) //
                    .filter(x -> cls.hasEncoding() && cls.classType != ClassType.ENUM);
            // for use with ContentType class
            Stream<String> c = Stream.of(HasStringValue.class.getCanonicalName()) //
                    .filter(x -> cls.hasEncoding() && cls.classType == ClassType.ENUM);
            implemented = " implements " + Stream.concat(a, Stream.concat(b, c)) //
                    .map(x -> imports.add(x)) //
                    .collect(Collectors.joining(", "));
        }
        return implemented;
    }

    private static void writeJsonTypeInfoAnnotation(CodePrintWriter out, Cls cls) {
        out.line("@%s(use = %s.NAME, property = \"%s\", include = %s.EXISTING_PROPERTY, visible = true)",
                JsonTypeInfo.class, Id.class, cls.discriminator.propertyName, As.class);
        out.right().right();
        String types = cls.fields.stream().map(x -> {
            final String fieldImportedType;
            if (x.fullClassName.startsWith(cls.fullClassName)) {
                fieldImportedType = Names.simpleClassName(cls.fullClassName)
                        + x.fullClassName.substring(cls.fullClassName.length());
            } else {
                fieldImportedType = out.add(x.fullClassName);
            }
            return String.format("\n%s@%s.Type(value = %s.class, name = \"%s\")", out.indent(), out.add(JsonSubTypes.class),
                    fieldImportedType, cls.discriminator.discriminatorValueFromFullClassName(x.fullClassName));
        }).collect(Collectors.joining(", "));
        out.left().left();
        out.line("@%s({%s})", JsonSubTypes.class, types);
    }

    private static void writePolymorphicDeserializerAnnotation(CodePrintWriter out, Cls cls) {
        out.line("@%s(using = %s._Deserializer.class)", JsonDeserialize.class, cls.simpleName());
    }

    private static void writeAutoDetectAnnotation(CodePrintWriter out) {
        out.line("@%s(", JsonAutoDetect.class);
        out.right().right();
        out.line("fieldVisibility = %s.Visibility.ANY,", JsonAutoDetect.class);
        out.line("creatorVisibility = %s.Visibility.ANY,", JsonAutoDetect.class);
        out.line("setterVisibility = %s.Visibility.ANY)", JsonAutoDetect.class);
        out.left().left();
    }
    
    private final static ObjectMapper MAPPER = new ObjectMapper();
    
    private static String escapedJson(ObjectNode node) {
        try {
            return MAPPER.writeValueAsString(node).replace("\n", "\\n").replace("\"", "\\\"");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeEnumMembers(CodePrintWriter out, Cls cls) {
        final String parameterFullClassName;
        if (!cls.fields.isEmpty()) {
            parameterFullClassName = cls.fields.get(0).fullClassName;
        } else {
            parameterFullClassName = "NotUsed";
        }
        int[] index = new int[] {-1};
        String text = cls.enumMembers.stream() //
                .map(x -> {
                    index[0]++;
                    final String memberName;
                    if (!cls.enumNames.isEmpty()) {
                        memberName = cls.enumNames.get(index[0]);
                    } else {
                        memberName = x.name;
                    }
                    if (x.parameter instanceof ObjectNode) {
                        return String.format("%s%s(%s.toMap(\"%s\"))", out.indent(), memberName, out.add(RuntimeUtil.class),
                                escapedJson((ObjectNode) x.parameter));
                    } else if (parameterFullClassName.equals(BigInteger.class.getCanonicalName())
                            || parameterFullClassName.equals(BigDecimal.class.getCanonicalName())) {
                        return String.format("%s%s(new %s(\"\"))", out.indent(), memberName,
                                out.add(parameterFullClassName), x.parameter);
                    } else {
                        String delim = x.parameter instanceof String //
                                || //
                                cls.enumValueFullType.equals(String.class.getCanonicalName()) // 
                                && x.parameter instanceof Boolean ? "\"" : "";
                        if (x.nullable) {
                            if (x.parameter == null) {
                                return String.format("%s%s(%s.empty())", out.indent(), memberName,
                                        out.add(Optional.class), delim, x.parameter, delim);
                            } else {
                                return String.format("%s%s(%s.of(%s%s%s))", out.indent(), memberName,
                                        out.add(Optional.class), delim, x.parameter, delim);
                            }
                        } else {
                            return String.format("%s%s(%s%s%s)", out.indent(), memberName, delim, x.parameter, delim);
                        }
                    }
                }).collect(Collectors.joining(",\n"));
        if (!text.isEmpty()) {
            out.println("\n" + text + ";");
        }
    }

    private static void writePolymorphicClassContent(CodePrintWriter out, Cls cls, Names names) {
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.println();
            out.line("%s %s();", String.class, cls.discriminator.fieldName);
        } else {
            if (cls.classType == ClassType.ONE_OF_NON_DISCRIMINATED) {
                out.println();
                writeJsonValueAnnotation(out);
                out.line("private final %s %s;", Object.class, "value");

                // add constructor for each member of the oneOf (fieldTypes)
                // as there are multiple constructors we cannot add ConstructorBinding
                // annotations so polymorphic stuff can't be used to bind to rest method
                // parameters
                writeOneOfAnyOfNonDiscriminatedObjectConstructor(out, cls);
                cls.fields.forEach(f -> writeOneOfAnyOfNonDiscriminatedMemberSpecificConstructor(out, cls, f));
                out.println();
                writeGetter(out, out.add(Object.class), "value", "value");
                writeNonDiscriminatedBuilder(out, cls);
            } else if (cls.classType == ClassType.ANY_OF_NON_DISCRIMINATED) {
                writeFields(out, cls);
                
                // write constructor
                out.right().right();
                final String parameters = cls.fields //
                        .stream() ///
                        .map(x -> String.format("\n%s%s %s", out.indent(),
                                x.resolvedType(out.imports()), x.fieldName(cls)))
                        .collect(Collectors.joining(","));
                out.left().left();
                out.println();
                out.line("private %s(%s) {", Names.simpleClassName(cls.fullClassName), parameters);
                ifValidate(cls, out, names, //
                        o -> cls.fields.stream().forEach(x -> {
                                checkNotNull(cls, o, x);
                        }));
                cls.fields.stream().forEach(x -> {
                    assignField(out, cls, x);
                });
                out.closeParen();
                out.println();
                out.line("public static %s of(%s) {", cls.simpleName(), parameters);
                String fields = cls.fields.stream().map(x -> x.fieldName).collect(Collectors.joining(", "));
                out.line("%s $o = new %s(%s);", cls.simpleName(), cls.simpleName(), fields);
                out.line("%s.checkCanSerialize(%s.config(), $o);", //
                        RuntimeUtil.class, //
                        out.add(names.globalsFullClassName()));
                out.line("return $o;");
                out.closeParen();
                
                // write getters
                cls.fields.forEach(f -> {
                    out.println();
                    writeGetter(out, f.resolvedType(out.imports()), f.fieldName(cls),
                            f.fieldName(cls));
                });
                
                writeAnyOfOrAllOfBuilder(out, cls, true);
            } else if (cls.classType == ClassType.ALL_OF) {
                // allof
                writeFields(out, cls);

                out.right().right();
                final String parameters = cls //
                        .fields //
                        .stream() //
                        .map(x -> String.format("\n%s%s %s", out.indent(), x.resolvedType(out.imports()),
                                x.fieldName(cls))) //
                        .collect(Collectors.joining(","));
                out.left().left();
                out.println();
                out.line("public %s(%s) {", Names.simpleClassName(cls.fullClassName), parameters);
                ifValidate(cls, out, names, //
                        o -> cls.fields.stream().forEach(x -> {
                            if (!x.isPrimitive() && x.required) {
                                checkNotNull(cls, o, x);
                            } else {
                                o.line("// TODO %s", x.fieldName);
                            }
                            validateMore(o, cls, x);
                        }));
                cls.fields.stream().forEach(x -> {
                    assignField(out, cls, x);
                });
                out.closeParen();

                // write getters for allOf members
                cls.fields.forEach(f -> {
                    out.println();
                    writeGetter(out, f.resolvedType(out.imports()),
                            "as" + Names.simpleClassName(f.resolvedType(out.imports())),
                            f.fieldName(cls));
                });
                
                // write all field getters
                Set<String> used = new HashSet<>();
                cls.fields.forEach(field -> {
                    Optional<Cls> c = names.cls(field.fullClassName);
                    if (c.isPresent() && c.get().classType != ClassType.ONE_OF_NON_DISCRIMINATED) {
                        c.get().fields //
                        .stream() //
                        .filter(f -> !f.mapType.isPresent()) //
                        .forEach(f -> {
                            String fieldName = f.fieldName(c.get());
                            if (!used.contains(fieldName)) {
                                used.add(fieldName);
                                String type = f.resolvedTypePublicConstructor(out.imports());
                                out.println();
                                out.line("public %s %s() {", type, fieldName);
                                final String getter;
                                if (c.get().classType == ClassType.ALL_OF) {
                                    getter = "as" + Names.upperFirst(fieldName);
                                } else {
                                    getter = fieldName;
                                }
                                out.line("return %s.%s();", field.fieldName(cls), getter);
                                out.closeParen();
                            }
                        });
                    }
                });
                // write allof builder
                writeAnyOfOrAllOfBuilder(out, cls, false);
            }

            out.println();
            out.line("@%s(\"serial\")", SuppressWarnings.class);
            final Class<?> polymorphicDeserializer;
            if (cls.classType == ClassType.ANY_OF_NON_DISCRIMINATED) {
                polymorphicDeserializer = AnyOfDeserializer.class;
            } else {
                polymorphicDeserializer = PolymorphicDeserializer.class;
            }
            out.line("public static final class _Deserializer extends %s<%s> {", polymorphicDeserializer,
                    cls.simpleName());
            out.println();
            out.line("public _Deserializer() {");
            String classes = cls.fields //
                    .stream() //
                    .map(x -> out.add(toPrimitive(x.fullClassName)) + ".class") //
                    .collect(Collectors.joining(", "));
            
            if (cls.classType == ClassType.ANY_OF_NON_DISCRIMINATED) {
                // members is used with anyOf only
                String members = cls.fields //
                        .stream() //
                        .map(x -> {
                            String c = out.add(x.fullClassName) + ".class";
                            String method = x.nullable ? "nullable" : "nonNullable";
                            return String.format("%s.%s(%s)", out.add(AnyOfMember.class), method, c);
                        }) //
                        .collect(Collectors.joining(", "));
                out.line("super(%s.config(), %s.class, %s);", out.add(names.globalsFullClassName()), cls.simpleName(),
                        members);
            } else {
                out.line("super(%s.config(), %s.%s, %s.class, %s);", out.add(names.globalsFullClassName()),
                        PolymorphicType.class, cls.polymorphicType.name(), cls.simpleName(), classes);
            }
            out.closeParen();
            out.closeParen();
            if (cls.classType == ClassType.ANY_OF_NON_DISCRIMINATED) {
                out.println();
                out.line("@%s(\"serial\")", SuppressWarnings.class);
                out.line("public static final class _Serializer extends %s<%s> {", AnyOfSerializer.class,
                        cls.simpleName());
                out.println();
                out.line("public _Serializer() {");
                out.line("super(%s.config(), %s.class);", out.add(names.globalsFullClassName()), cls.simpleName());
                out.closeParen();
                out.closeParen();
            }
        }
    }

    private static void writeAnyOfOrAllOfBuilder(CodePrintWriter out, Cls cls, boolean useOf) {
        List<BuilderWriter.Field> fields = //
                cls.fields.stream() //
                        .map(f -> new BuilderWriter.Field(f.fieldName(cls), f.fullClassName, f.required, f.isArray,
                                f.mapType, f.nullable, Optional.empty())) //
                        .collect(Collectors.toList());
        BuilderWriter.write(out, fields, cls.simpleName(), useOf);
    }
    
    private static void writeNonDiscriminatedBuilder(CodePrintWriter out, Cls cls) {
        cls.fields.forEach(f -> {
            out.println();
            out.line("public static %s of(%s value) {", cls.simpleName(), out.add(f.fullClassName));
            out.line("return new %s(value);", cls.simpleName());
            out.closeParen();
        });
    }

    private static void writeOneOfAnyOfNonDiscriminatedMemberSpecificConstructor(CodePrintWriter out, Cls cls,
            Field f) {
        String className = toPrimitive(f.fullClassName);
        out.println();
        out.line("public %s(%s value) {", cls.simpleName(), out.add(className));
        if (org.davidmoten.oa3.codegen.generator.internal.Util.isPrimitiveFullClassName(className)) {
            out.line("this.value = value;");
        } else {
            out.line("this.value = %s.checkNotNull(value, \"value\");", Preconditions.class);
        }
        out.closeParen();
    }

    private static void writeOneOfAnyOfNonDiscriminatedObjectConstructor(CodePrintWriter out, Cls cls) {
        out.println();
        out.line("private %s(%s value) {", cls.simpleName(), Object.class);
        out.line("this.value = %s.checkNotNull(value, \"value\");", Preconditions.class);
        out.closeParen();
    }

    private static void writeFields(CodePrintWriter out, Cls cls) {
        if (!cls.fields.isEmpty()) {
            out.println();
        }
        Mutable<Boolean> first = Mutable.create(true);
        cls.fields.forEach(f -> {
            if (!first.value) {
                out.println();
            }
            first.value = false;
            if (cls.classType != ClassType.ANY_OF_NON_DISCRIMINATED) {
                if (f.isAdditionalProperties() && !f.isArray) {
                    out.line("@%s", JsonAnyGetter.class);
                    out.line("@%s", JsonAnySetter.class);
                } else if (cls.classType == ClassType.ALL_OF) {
                    out.line("@%s", JsonUnwrapped.class);
                } else if (cls.unwrapSingleField()) {
                    writeJsonValueAnnotation(out);
                } else {
                    out.line("@%s(\"%s\")", JsonProperty.class, f.name);
                }
            }
            if (f.required && f.nullable) {
                out.line("@%s(%s.ALWAYS)", JsonInclude.class, JsonInclude.Include.class);
            }
            if (f.isOctets()) {
                // TODO handle f.isArray (more serializers?)
                if (!f.required && f.nullable) {
                    out.line("@%s(using = %s.class)", JsonSerialize.class, JsonNullableOctetsSerializer.class);
                } else if (!f.required || f.nullable) {
                    out.line("@%s(using = %s.class)", JsonSerialize.class, OptionalOctetsSerializer.class);
                } else {
                    out.line("@%s(using = %s.class)", JsonSerialize.class, OctetsSerializer.class);
                }
            }
            final String fieldType;
            if (cls.classType == ClassType.ENUM && cls.enumValueFullType.equals(Map.class.getCanonicalName())) {
                fieldType = String.format("%s<%s, %s>", out.add(Map.class), out.add(String.class),
                        out.add(Object.class));
            } else {
                fieldType = f.resolvedType(out.imports());
            }
            out.line("private final %s %s;", fieldType, cls.fieldName(f));
        });
    }

    private static void writeJsonValueAnnotation(CodePrintWriter out) {
        out.line("@%s", JsonValue.class);
    }

    private static void writeConstructor(CodePrintWriter out, Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces,
            Names names) {

        Set<Cls> interfaces = interfaces(cls, fullClassNameInterfaces);

        boolean hasAdditionalProperties = cls.fields.stream().anyMatch(Field::isAdditionalProperties);
        boolean hasDiscriminator = cls.fields.stream().anyMatch(x -> isDiscriminator(interfaces, x));
        boolean extraConstructor = hasAdditionalProperties;
        if (extraConstructor) {
            // if has additionalProperties then we make the JsonCreator constructor private
            // (excluding properties) and make another public constructor that includes the
            // Map for additional properties as a parameter)
            out.right().right();
            String parameters = cls //
                    .fields //
                    .stream() //
                    // ignore discriminators that should be constants
//                    .filter(x -> !isDiscriminator(interfaces, x)) //
                    .map(x -> {
                        final String t;
                        if (x.mapType.isPresent()) {
                            t = x.resolvedTypeMapPublic(out.imports());
                        } else {
                            t = x.resolvedTypePublicConstructor(out.imports());
                        }
                        return String.format("\n%s%s %s", out.indent(), t, x.fieldName(cls));
                    }) //
                    .collect(Collectors.joining(","));
            out.left().left();
            out.println();
            out.line("public %s(%s) {", Names.simpleClassName(cls.fullClassName), parameters);
            writeConstructorBody(out, cls, names, interfaces, true);
            out.closeParen();
        }
        out.right().right();
        String parameters = cls //
                .fields //
                .stream() //
                // ignore discriminators that should be constants
//                    .filter(x -> !isDiscriminator(interfaces, x)) //
                .filter(x -> !x.isAdditionalProperties()) //
                .map(f -> {
                    final String t;
                    if (f.mapType.isPresent()) {
                        t = f.resolvedTypeMapPublic(out.imports());
                    } else {
                        t = f.resolvedTypePublicConstructor(out.imports());
                    }
                    String annotations = cls.unwrapSingleField() ? "" //
                            : String.format("@%s(\"%s\") ", out.add(JsonProperty.class), f.name);
                    if (f.isOctets()) {
                        if (!f.required && f.nullable) {
                            annotations += String.format("@%s(using = %s.class) ", out.add(JsonDeserialize.class),
                                    out.add(JsonNullableOctetsDeserializer.class));
                        } else if (!f.required || f.nullable) {
                            annotations += String.format("@%s(using = %s.class) ", out.add(JsonDeserialize.class),
                                    out.add(OptionalOctetsDeserializer.class));
                        } else {
                            annotations += String.format("@%s(using = %s.class) ", out.add(JsonDeserialize.class),
                                    out.add(OctetsDeserializer.class));
                        }
                    }
                    return String.format("\n%s%s%s %s", out.indent(), annotations, t, f.fieldName(cls));
                }) //
                .collect(Collectors.joining(","));
        out.left().left();
        out.println();
        final String modifier;
        if (cls.classType == ClassType.ENUM) {
            modifier = "";
        } else if (hasDiscriminator||extraConstructor) {
            modifier = "private ";
        } else {
            modifier = "public ";
        }
        if (modifier.equals("private ")) {
            addConstructorBindingAnnotation(out, names);
        }
        if (cls.classType != ClassType.ENUM) {
            out.line("@%s", JsonCreator.class);
        }
        out.line("%s%s(%s) {", modifier, Names.simpleClassName(cls.fullClassName), parameters);
        writeConstructorBody(out, cls, names, interfaces, false);
        out.closeParen();
    }

    private static void addConstructorBindingAnnotation(CodePrintWriter out, Names names) {
        if (names.generateService()) {
            if (names.generatorType() == ServerGeneratorType.SPRING3) {
                out.line("@%s", out
                        .add(ConstructorBinding.class.getName().replace("ConstructorBinding", "bind.ConstructorBinding")));
            } else {
                out.line("@%s", ConstructorBinding.class);
            }
        }
    }

    private static void writeConstructorBody(CodePrintWriter out, Cls cls, Names names, Set<Cls> interfaces,
            boolean additionalPropertiesIsParameter) {
        // validate
        ifValidate(cls, out, names, //
                out2 -> cls.fields.stream() //
                        .filter(x -> !x.isAdditionalProperties() || additionalPropertiesIsParameter) //
                        .forEach(x -> {
                            if (!isDiscriminator(interfaces, x)) {
                                if (x.isOctets() || !x.isPrimitive()) {
                                    checkNotNull(cls, out2, x);
                                }
                                validateMore(out2, cls, x);
                            }
                        }));
        // assign
        cls //
                .fields //
                .stream() //
                .forEach(x -> writeConstructorBodyFieldAssignment(out, cls, interfaces, x,
                        additionalPropertiesIsParameter));
    }

    private static void writeConstructorBodyFieldAssignment(CodePrintWriter out, Cls cls, Set<Cls> interfaces, Field x,
            boolean additionalPropertiesIsParameter) {
        if (x.isAdditionalProperties() && !additionalPropertiesIsParameter) {
            out.line("this.%s = new %s<>();", x.fieldName(cls), HashMap.class);
        } else if (x.mapType.isPresent()) {
            if (x.isArray) {
                out.line("this.%s = new %s<>();", x.fieldName(cls), ArrayList.class);
            } else if (x.required && !x.nullable) {
                out.line("this.%s = %s.createMapIfNull(%s);", x.fieldName(cls), Util.class, x.fieldName(cls));
            } else {
                out.line("this.%s = %s;", x.fieldName(cls), x.fieldName(cls));
            }
            return;
        } else {
            Optional<Discriminator> disc = discriminator(interfaces, x);
            if (disc.isPresent()) {
                // write constant value for discriminator, if is enum then
                // grab it's value using the DiscriminatorHelper
                out.line("%s.checkEquals(%s.value(%s.class, \"%s\"), %s, \"%s\");", //
                        Preconditions.class, //
                        DiscriminatorHelper.class, //
                        out.add(x.fullClassName), //
                        disc.get().discriminatorValueFromFullClassName(cls.fullClassName), //
                        x.fieldName(cls), //
                        x.fieldName(cls));
                out.line("this.%s = %s;", x.fieldName(cls), x.fieldName(cls));
            } else if (x.nullable) {
                if (x.required) {
                    out.line("this.%s = %s;", x.fieldName(cls), x.fieldName(cls));
                } else {
                    assignField(out, cls, x);
                }
            } else if (!x.isPrimitive()) {
                if (x.required) {
                    assignField(out, cls, x);
                } else {
                    assignOptionalField(out, cls, x);
                }
            } else {
                assignField(out, cls, x);
            }
        }
    }

    private static Set<Cls> interfaces(Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces) {
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());
        return interfaces;
    }

    private static void writeBuilder(CodePrintWriter out, Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces) {
        if (cls.classType == ClassType.ENUM) {
            return;
        }
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());
        List<BuilderWriter.Field> fields = cls.fields //
                .stream() //
//                .filter(x -> !isDiscriminator(interfaces, x)) //
                .map(f -> {
                    Optional<Function<String, String>> expressionFactory;
                    Optional<Discriminator> disc = discriminator(interfaces, f);
                    if (disc.isPresent()) {
                        // write constant value for discriminator, if is enum then
                        // grab it's value using the DiscriminatorHelper
                        String expression = String.format("%s.value(%s.class, \"%s\")", 
                                out.add(DiscriminatorHelper.class), 
                                out.add(f.fullClassName),
                                disc.get().discriminatorValueFromFullClassName(cls.fullClassName));
                        expressionFactory = Optional.of(x -> expression);
                    } else {
                        expressionFactory = Optional.empty();
                    }
                    return new BuilderWriter.Field(f.fieldName(cls), f.fullClassName,
                        f.required && !f.isAdditionalProperties(), f.isArray, f.mapType, f.nullable, expressionFactory);})
                .collect(Collectors.toList());
        BuilderWriter.write(out, fields, cls.simpleName());
    }

    private static void checkNotNull(Cls cls, CodePrintWriter out, Field x) {
        out.line("%s.checkNotNull(%s, \"%s\");", Preconditions.class, x.fieldName(cls), x.fieldName(cls));
    }

    private static void assignOptionalField(CodePrintWriter out, Cls cls, Field x) {
        out.line("this.%s = %s;", x.fieldName(cls), x.fieldName(cls));
    }

    private static boolean isDiscriminator(Set<Cls> interfaces, Field x) {
        return discriminator(interfaces, x).isPresent();
    }

    private static Optional<Discriminator> discriminator(Set<Cls> interfaces, Field x) {
        return interfaces.stream().filter(y -> x.name.equals(y.discriminator.propertyName)) //
                .map(y -> y.discriminator).findFirst();
    }

    private static void validateMore(CodePrintWriter out, Cls cls, Field x) {
        if (x.isAdditionalProperties()) {
            // TODO check values of map
            return;
        }
        String raw = x.fieldName(cls);
        if (x.minLength.isPresent() && !x.isDateOrTime()) {
            out.line("%s.checkMinLength(%s, %s, \"%s\");", Preconditions.class, raw, x.minLength.get(),
                    x.fieldName(cls));
        }
        if (x.maxLength.isPresent() && !x.isDateOrTime()) {
            out.line("%s.checkMaxLength(%s, %s, \"%s\");", Preconditions.class, raw, x.maxLength.get(),
                    x.fieldName(cls));
        }
        if (x.pattern.isPresent() && !x.isDateOrTime() && !x.isByteArray() && !x.isOctets()) {
            out.line("%s.checkMatchesPattern(%s, \"%s\", \"%s\");", Preconditions.class, raw,
                    WriterUtil.escapePattern(x.pattern.get()), x.fieldName(cls));
        }
        if (x.min.isPresent() && x.isNumber()) {
            out.line("%s.checkMinimum(%s, \"%s\", \"%s\", %s);", Preconditions.class, raw, x.min.get().toString(),
                    x.fieldName(cls), x.exclusiveMin);
        }
        if (x.max.isPresent() && x.isNumber()) {
            out.line("%s.checkMaximum(%s, \"%s\", \"%s\", %s);", Preconditions.class, raw, x.max.get().toString(),
                    x.fieldName(cls), x.exclusiveMax);
        }
        if (x.isArray && x.minItems.isPresent()) {
            out.line("%s.checkMinSize(%s, %s, \"%s\");", Preconditions.class, x.fieldName(cls), x.minItems.get(),
                    x.fieldName(cls));
        }
        if (x.isArray && x.maxItems.isPresent()) {
            out.line("%s.checkMaxSize(%s, %s, \"%s\");", Preconditions.class, x.fieldName(cls), x.maxItems.get(),
                    x.fieldName(cls));
        }
    }

    private static void writeEqualsMethod(CodePrintWriter out, Cls cls) {
        addOverrideAnnotation(out);
        out.line("public boolean equals(%s o) {", Object.class);
        out.line("if (this == o) {");
        out.line("return true;");
        out.closeParen();
        out.line("if (o == null || getClass() != o.getClass()) {");
        out.line("return false;");
        out.closeParen();
        out.right();
        String s = cls.fields
                .stream() //
                .map(x -> String.format("\n%s%s.deepEquals(this.%s, other.%s)", out.indent(),
                        out.add(Objects.class), x.fieldName(cls), x.fieldName(cls)))
                .distinct()
                .collect(Collectors.joining(" && "));
        out.left();
        if (!s.isEmpty()) {
            out.line("%s other = (%s) o;", cls.simpleName(), cls.simpleName());
        }
        out.line("return %s;", s.isEmpty() ? "true" : s);
        out.closeParen();
    }

    private static void writeHashCodeMethod(CodePrintWriter out, Cls cls) {
        final String s;
        if (cls.fields.size() <= 3) {
            s = cls.fields.stream().map(x -> x.fieldName(cls)).collect(Collectors.joining(", "));
        } else {
            out.right().right().right();
            s = cls.fields.stream().map(x -> String.format("\n%s%s", out.indent(), x.fieldName(cls)))
                    .collect(Collectors.joining(", "));
            out.left().left().left();
        }
        addOverrideAnnotation(out);
        out.line("public int hashCode() {");
        out.line("return %s.hash(%s);", Objects.class, s);
        out.closeParen();
    }

    private static void writeToStringMethod(CodePrintWriter out, Cls cls) {
        final String s;
        if (cls.fields.size() > 3) {
            out.right().right().right();
            s = cls.fields.stream()
                    .map(x -> String.format(",\n%s\"%s\", %s", out.indent(), x.fieldName(cls), x.fieldName(cls)))
                    .collect(Collectors.joining());
            out.left().left().left();
        } else {
            s = cls.fields.stream().map(x -> String.format(", \"%s\", %s", x.fieldName(cls), x.fieldName(cls)))
                    .collect(Collectors.joining(""));
        }
        addOverrideAnnotation(out);
        out.line("public %s toString() {", String.class);
        out.line("return %s.toString(%s.class%s);", Util.class, cls.simpleName(), s);
        out.closeParen();
    }

    private static void ifValidate(Cls cls, CodePrintWriter out, Names names, Consumer<CodePrintWriter> consumer) {
        CodePrintWriter b = CodePrintWriter.create(out);
        out.right();
        consumer.accept(b);
        out.left();
        b.close();
        String text = b.text();
        if (text.isEmpty()) {
            return;
        } else {
            out.line("if (%s.config().validateInConstructor().test(%s.class)) {", out.add(names.globalsFullClassName()),
                    cls.simpleName());
            out.left();
            out.print(text);
            out.line("}");
        }
    }

    private static void assignField(CodePrintWriter out, Cls cls, Field x) {
        out.line("this.%s = %s;", x.fieldName(cls), x.fieldName(cls));
    }

    private static void writeGetters(CodePrintWriter out, Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces) {
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());
        cls.fields.forEach(f -> {
            out.println();
            Optional<Discriminator> disc = discriminator(interfaces, f);
            if (disc.isPresent()) {
                // write constant value for discriminator, if is enum then
                // grab it's value using the DiscriminatorHelper
                String value = discriminatorHelperExpression(out, f.fieldName(cls));
                addOverrideAnnotation(out);
                writeGetter(out, out.add(String.class), f.fieldName(cls), value);
            } else if (f.mapType.isPresent()) {
                if (!f.isArray && f.isAdditionalProperties()) {
                    writeJsonAnySetter(out, cls, f);
                }
                final String expression = f.fieldName(cls);
                writeGetter(out, f.resolvedTypeMapPublic(out.imports()), f.fieldName(cls), expression);
            } else {
                final String value = f.fieldName(cls);
                final String returnType;
                if (cls.classType == ClassType.ENUM && f.fullClassName.equals(Map.class.getCanonicalName())) {
                    returnType = String.format("%s<%s, %s>", out.add(Map.class), out.add(String.class), out.add(Object.class));
                } else {
                    returnType = f.resolvedTypePublicConstructor(out.imports());
                }
                writeGetter(out, returnType, f.fieldName(cls), value);
            }
        });

    }

    private static String discriminatorHelperExpression(CodePrintWriter out, String fieldExpression) {
        return String.format("%s.value(%s)", out.add(DiscriminatorHelper.class), fieldExpression);
    }
    
    private static void writePropertiesMapGetter(CodePrintWriter out, Cls cls) {
        if (cls.fields.isEmpty() || cls.classType == ClassType.ENUM) {
            return;
        }
        Indent indent = out.indent().copy().right().right().right();
        String puts = cls.fields.stream().map(f -> String.format("\n%s.put(\"%s\", (%s) %s)", indent, f.name, out.add(Object.class), cls.fieldName(f))).collect(Collectors.joining());
        out.println();
        out.line("%s<%s, %s> _internal_properties() {", Map.class, String.class, Object.class);
        out.line("return %s%s\n%s.build();", Maps.class, puts, indent);
        out.closeParen();
    }

    private static void writeMutators(CodePrintWriter out, Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces) {
        List<Field> fields = cls.fields //
                .stream() //
                .collect(Collectors.toList());
        if (fields.size() <= 1) {
            return;
        }
        fields //
                .stream() //
                // ignore discriminators that should be constants so don't need a mutator
                .filter(x -> !isDiscriminator(interfaces(cls, fullClassNameInterfaces), x)) //
                .forEach(x -> {
                    String t = x.mapType.isPresent() ? x.resolvedTypeMapPublic(out.imports())
                            : x.resolvedTypePublicConstructor(out.imports());
                    out.println();
                    out.line("public %s with%s(%s %s) {", cls.simpleName(), Names.upperFirst(x.fieldName(cls)), t,
                            x.fieldName(cls));
                    {
                        String params = fields.stream() //
                                .map(y -> y.fieldName(cls)) //
                                .collect(Collectors.joining(", "));
                        out.line("return new %s(%s);", cls.simpleName(), params);
                        out.closeParen();
                    }
                    if (!x.mapType.isPresent()) {
                        Optional<String> tNonOptional = x.resolvedTypePublicConstructorNonOptional(out.imports());
                        if (tNonOptional.isPresent() && !tNonOptional.get().equals(t)) {
                            out.println();
                            out.line("public %s with%s(%s %s) {", cls.simpleName(), Names.upperFirst(x.fieldName(cls)),
                                    tNonOptional.get(), x.fieldName(cls));
                            String params = fields.stream() //
                                    .map(y -> {
                                        if (y.fieldName(cls).equals(x.fieldName(cls))) {
                                            if (y.nullable && !y.required) {
                                                return String.format("%s.of(%s)", out.add(JsonNullable.class),
                                                        y.fieldName(cls));
                                            } else {
                                                return String.format("%s.of(%s)", out.add(Optional.class),
                                                        y.fieldName(cls));
                                            }
                                        } else {
                                            return y.fieldName(cls);
                                        }
                                    }).collect(Collectors.joining(", "));
                            out.line("return new %s(%s);", cls.simpleName(), params);
                            out.closeParen();
                        }
                    }
                });
    }

    private static void writeJsonAnySetter(CodePrintWriter out, Cls cls, Field f) {
        out.println();
        out.line("@%s", JsonAnySetter.class);
        if (f.nullable) {
            out.line("private void put(%s key, %s<%s> value) {", String.class, JsonNullable.class,
                    out.add(f.fullClassName));
        } else {
            out.line("private void put(%s key, %s value) {", String.class, out.add(f.fullClassName));
        }
        out.line("this.%s.put(key, value);", f.fieldName(cls));
        out.closeParen();
    }

    private static void writeGetter(CodePrintWriter out, String returnImportedType, String fieldName, String value) {
        out.line("public %s %s() {", returnImportedType, fieldName);
        out.line("return %s;", value);
        out.closeParen();
    }

    private static void writeMemberClasses(CodePrintWriter out, Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces,
            Names names) {
        cls.classes.forEach(c -> writeClass(out, c, fullClassNameInterfaces, names));
    }

}
