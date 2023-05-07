package org.davidmoten.oa3.codegen.generator.writer;

import static org.davidmoten.oa3.codegen.generator.internal.Util.toPrimitive;
import static org.davidmoten.oa3.codegen.generator.internal.WriterUtil.IMPORTS_HERE;
import static org.davidmoten.oa3.codegen.generator.internal.WriterUtil.closeParen;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.BuilderWriter;
import org.davidmoten.oa3.codegen.generator.Generator;
import org.davidmoten.oa3.codegen.generator.Generator.ClassType;
import org.davidmoten.oa3.codegen.generator.Generator.Cls;
import org.davidmoten.oa3.codegen.generator.Generator.Discriminator;
import org.davidmoten.oa3.codegen.generator.Generator.Encoding;
import org.davidmoten.oa3.codegen.generator.Generator.Field;
import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.SchemaCategory;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.generator.internal.Javadoc;
import org.davidmoten.oa3.codegen.generator.internal.Mutable;
import org.davidmoten.oa3.codegen.generator.internal.WriterUtil;
import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.runtime.PolymorphicDeserializer;
import org.davidmoten.oa3.codegen.runtime.PolymorphicType;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.annotation.Generated;

public final class SchemasCodeWriter {

    private SchemasCodeWriter() {
        // prevent instantiation
    }

    private static final String version = readVersion();

    private static String readVersion() {
        Properties p = new Properties();
        try (InputStream in = Generator.class.getResourceAsStream("/application.properties")) {
            p.load(in);
            return p.get("groupId") + ":" + p.get("artifactId") + p.get("version");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeSchemaClass(Names names, Map<String, Set<Cls>> fullClassNameInterfaces, Cls cls,
            String schemaName) {
        Imports imports = new Imports(cls.fullClassName);
        if ((cls.category == SchemaCategory.PATH || cls.category == SchemaCategory.RESPONSE) && cls.schema.isPresent()
                && cls.schema.get().get$ref() != null) {
            // when a cls has a ref and is used with a Path or Response then the ref class
            // is used in generated code
            return;
        }
        CodePrintWriter out = CodePrintWriter.create();
        SchemasCodeWriter.writeClass(out, imports, out.indent(), cls, fullClassNameInterfaces, names);
        WriterUtil.writeContent(names, out, names.schemaNameToFullClassName(cls.category, schemaName), imports);
    }

    public static void writeGlobalsClass(Names names) {
        CodePrintWriter out = CodePrintWriter.create();
        String fullClassName = names.globalsFullClassName();
        Imports imports = new Imports(fullClassName);
        out.line("package %s;", Names.pkg(fullClassName));
        out.println();
        out.line("%s", IMPORTS_HERE);
        addGeneratedAnnotation(out, imports);
        out.line("public final class %s {", Names.simpleClassName(fullClassName));
        out.right();
        out.println();
        out.line("private static volatile %s config = %s.builder().build();", imports.add(Config.class),
                imports.add(Config.class));
        out.println();
        out.line("public static void setConfig(%s configuration) {", imports.add(Config.class));
        out.right();
        out.line("config = configuration;");
        out.closeParen();
        out.println();
        out.line("public static %s config() {", imports.add(Config.class));
        out.right();
        out.line("return config;");
        out.closeParen();
        out.closeParen();
        WriterUtil.writeContent(names, out, fullClassName, imports);
    }

    private static void writeClass(CodePrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces, Names names) {
        if (cls.topLevel) {
            out.line("package %s;", cls.pkg());
            out.println();
            out.line("%s", IMPORTS_HERE);
        }
        writeClassDeclaration(out, imports, cls, fullClassNameInterfaces);
        out.right();
        writeEnumMembers(out, cls);
        if (isPolymorphic(cls)) {
            writePolymorphicClassContent(out, imports, indent, cls, names, fullClassNameInterfaces);
        } else {
            writeFields(out, imports, indent, cls);
            writeConstructor(out, imports, indent, cls, fullClassNameInterfaces, names);
            writeBuilder(out, imports, cls, fullClassNameInterfaces);
            writeGetters(out, imports, indent, cls, fullClassNameInterfaces);
        }
        writeEnumCreator(out, imports, cls);
        writeMemberClasses(out, imports, indent, cls, fullClassNameInterfaces, names);
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeEqualsMethod(out, imports, cls);
            writeHashCodeMethod(out, imports, indent, cls);
            writeToStringMethod(out, imports, indent, cls);
        }
        out.closeParen();
    }

    private static boolean isPolymorphic(Cls cls) {
        return cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED
                || cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED || cls.classType == ClassType.ALL_OF;
    }

    private static void addOverrideAnnotation(CodePrintWriter out, Imports imports) {
        out.println();
        out.line("@%s", imports.add(Override.class));
    }

    private static void addGeneratedAnnotation(CodePrintWriter out, Imports imports) {
        out.line("@%s(value = \"%s\")", imports.add(Generated.class), version);
    }

    private static void writeEnumCreator(CodePrintWriter out, Imports imports, Cls cls) {
        if (cls.classType == ClassType.ENUM) {
            String simpleClassName = Names.simpleClassName(cls.fullClassName);
            out.println();
            out.line("@%s", imports.add(JsonCreator.class));
            out.line("public static %s fromValue(%s value) {", simpleClassName, imports.add(Object.class));
            out.right();
            out.line("for (%s x: %s.values()) {", simpleClassName, simpleClassName);
            out.right();
            // be careful because x.value can be primitive
            out.line("if (value.equals(x.value)) {");
            out.right();
            out.line("return x;");
            out.closeParen();
            out.closeParen();
            out.line("throw new %s(\"unexpected enum value: '\" + value + \"'\");",
                    imports.add(IllegalArgumentException.class));
            out.closeParen();
        }
    }

    private static void writeClassDeclaration(CodePrintWriter out, Imports imports, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        String modifier = classModifier(cls);
        Set<Cls> interfaces = fullClassNameInterfaces.get(cls.fullClassName);
        String implementsClause = implementsClause(imports, interfaces);
        if (cls.description.isPresent()) {
            Javadoc.printJavadoc(out, out.indent(), cls.description.get(), true);
        }
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeJsonTypeInfoAnnotation(out, imports, cls);
        } else if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED || cls.classType == ClassType.ALL_OF) {
            writePolymorphicDeserializerAnnotation(out, imports, cls);
        } else {
            out.println();
        }
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeJsonIncludeAnnotation(out, imports);
            writeAutoDetectAnnotation(out, imports);
        }
        if (cls.topLevel) {
            addGeneratedAnnotation(out, imports);
        }
        out.line("public %s%s %s%s {", modifier, cls.classType.word(), cls.simpleName(), implementsClause);
    }

    private static void writeJsonIncludeAnnotation(CodePrintWriter out, Imports imports) {
        out.line("@%s(%s.NON_NULL)", imports.add(JsonInclude.class), imports.add(Include.class));
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

    private static String implementsClause(Imports imports, Set<Cls> interfaces) {
        final String implemented;
        if (interfaces == null || interfaces.isEmpty()) {
            implemented = "";
        } else {
            implemented = " implements "
                    + interfaces.stream().map(x -> imports.add(x.fullClassName)).collect(Collectors.joining(", "));
        }
        return implemented;
    }

    private static void writeJsonTypeInfoAnnotation(CodePrintWriter out, Imports imports, Cls cls) {
        out.line("@%s(use = %s.NAME, property = \"%s\", include = %s.EXISTING_PROPERTY, visible = true)",
                imports.add(JsonTypeInfo.class), imports.add(Id.class), cls.discriminator.propertyName,
                imports.add(As.class));
        out.right().right();
        String types = cls.fields.stream()
                .map(x -> String.format("\n%s@%s(value = %s.class, name = \"%s\")", out.indent(),
                        imports.add(Type.class), imports.add(x.fullClassName),
                        cls.discriminator.discriminatorValueFromFullClassName(x.fullClassName)))
                .collect(Collectors.joining(", "));
        out.left().left();
        out.line("@%s({%s})", imports.add(JsonSubTypes.class), types);
    }

    private static void addConstructorBindingAnnotation(CodePrintWriter out, Imports imports, Names names) {
        if (names.generatorIsSpring3()) {
            out.line("@%s", imports
                    .add(ConstructorBinding.class.getName().replace("ConstructorBinding", "bind.ConstructorBinding")));
        } else {
            out.line("@%s", imports.add(ConstructorBinding.class));
        }
    }

    private static void writePolymorphicDeserializerAnnotation(CodePrintWriter out, Imports imports, Cls cls) {
        out.println();
        out.line("@%s(using = %s.Deserializer.class)", imports.add(JsonDeserialize.class), cls.simpleName());
    }

    private static void writeAutoDetectAnnotation(CodePrintWriter out, Imports imports) {
        out.line("@%s(fieldVisibility = %s.ANY, creatorVisibility = %s.ANY)", imports.add(JsonAutoDetect.class),
                imports.add(Visibility.class), imports.add(Visibility.class));
    }

    private static void writeEnumMembers(CodePrintWriter out, Cls cls) {
        String text = cls.enumMembers.stream().map(x -> {
            String delim = x.parameter instanceof String ? "\"" : "";
            return String.format("%s%s(%s%s%s)", out.indent(), x.name, delim, x.parameter, delim);
        }).collect(Collectors.joining(",\n"));
        if (!text.isEmpty()) {
            out.println("\n" + text + ";");
        }
    }

    private static void writePolymorphicClassContent(CodePrintWriter out, Imports imports, Indent indent, Cls cls,
            Names names, Map<String, Set<Cls>> fullClassNameInterfaces) {
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.println();
            out.line("%s %s();", imports.add(String.class), cls.discriminator.fieldName);
        } else {
            if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED) {
                out.println();
                writeJsonValueAnnotation(out, imports, indent);
                out.line("private final %s %s;", imports.add(Object.class), "value");

                // add constructor for each member of the oneOf (fieldTypes)
                // as there are multiple constructors we cannot add ConstructorBinding
                // annotations so polymorphic stuff can't be used to bind to rest method
                // parameters
                writeOneOfAnyOfNonDiscriminatedObjectConstructor(out, imports, cls);
                cls.fields.forEach(f -> writeOneOfAnyOfNonDiscriminatedMemberSpecificConstructor(out, imports, cls, f));
                writeNonDiscriminatedBuilder(out, imports, cls);
                out.println();
                writeGetter(out, indent, imports.add(Object.class), "value", "value");
            } else {
                // allof
                writeFields(out, imports, indent, cls);

                out.right().right();
                final String parametersNullable;
                parametersNullable = cls.fields.stream().map(x -> String.format("\n%s%s %s", out.indent(),
                        x.resolvedTypeNullable(imports), x.fieldName(cls))).collect(Collectors.joining(","));
                out.left().left();
                out.println();
                out.line("public %s(%s) {", Names.simpleClassName(cls.fullClassName), parametersNullable);
                out.right();
                ifValidate(cls, out, imports, names, //
                        out2 -> cls.fields.stream().forEach(x -> {
                            if (!x.isPrimitive() && x.required) {
                                checkNotNull(imports, cls, out2, x);
                            } else {
                                out.line("// ???");
                            }
                            validateMore(out2, imports, cls, x);
                        }));
                cls.fields.stream().forEach(x -> {
                    assignField(out, indent, cls, x);
                });
                closeParen(out, indent);
                writeGetters(out, imports, indent, cls, fullClassNameInterfaces);
            }
            out.println();
            out.line("@%s(\"serial\")", imports.add(SuppressWarnings.class));
            out.line("public static final class Deserializer extends %s<%s> {",
                    imports.add(PolymorphicDeserializer.class), cls.simpleName());
            out.right();
            out.println();
            out.line("public Deserializer() {");
            out.right();
            String classes = cls.fields.stream().map(x -> imports.add(toPrimitive(x.fullClassName)) + ".class")
                    .collect(Collectors.joining(", "));
            out.line("super(%s.config(), %s.%s, %s.class, %s);", imports.add(names.globalsFullClassName()),
                    imports.add(PolymorphicType.class), cls.polymorphicType.name(), cls.simpleName(), classes);
            out.closeParen();
            out.closeParen();
        }
    }

    private static void writeNonDiscriminatedBuilder(CodePrintWriter out, Imports imports, Cls cls) {
        cls.fields.forEach(f -> {
            out.line("public static %s of(%s value) {", cls.simpleName(), imports.add(f.fullClassName));
            out.right();
            out.line("return new %s(value);", cls.simpleName());
            out.closeParen();
        });
    }

    private static void writeOneOfAnyOfNonDiscriminatedMemberSpecificConstructor(CodePrintWriter out, Imports imports,
            Cls cls, Field f) {
        String className = toPrimitive(f.fullClassName);
        out.println();
        out.line("public %s(%s value) {", cls.simpleName(), imports.add(className));
        out.right();
        if (org.davidmoten.oa3.codegen.generator.internal.Util.isPrimitiveFullClassName(className)) {
            out.line("this.value = value;");
        } else {
            out.line("this.value = %s.checkNotNull(value, \"value\");",
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class));
        }
        out.closeParen();
    }

    private static void writeOneOfAnyOfNonDiscriminatedObjectConstructor(CodePrintWriter out, Imports imports,
            Cls cls) {
        out.println();
        out.line("@%s", imports.add(JsonCreator.class));
        out.line("private %s(%s value) {", cls.simpleName(), imports.add(Object.class));
        out.right();
        out.line("this.value = %s.checkNotNull(value, \"value\");",
                imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class));
        out.closeParen();
    }

    private static void writeFields(CodePrintWriter out, Imports imports, Indent indent, Cls cls) {
        if (!cls.fields.isEmpty()) {
            out.println();
        }
        Mutable<Boolean> first = Mutable.create(true);
        cls.fields.forEach(f -> {
            if (!first.value) {
                out.println();
            }
            first.value = false;
            if (cls.classType == ClassType.ALL_OF) {
                out.line("@%s", imports.add(JsonUnwrapped.class));
            } else if (cls.unwrapSingleField()) {
                writeJsonValueAnnotation(out, imports, indent);
            } else {
                out.line("@%s(\"%s\")", imports.add(JsonProperty.class), f.name);
            }
            final String fieldType;
            if (f.encoding == Encoding.OCTET) {
                fieldType = imports.add(String.class);
            } else {
                fieldType = f.resolvedTypeNullable(imports);
            }
            out.line("private final %s %s;", fieldType, cls.fieldName(f));
        });
    }

    private static void writeJsonValueAnnotation(CodePrintWriter out, Imports imports, Indent indent) {
        out.format("%s@%s\n", indent, imports.add(JsonValue.class));
    }

    private static void writeConstructor(CodePrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces, Names names) {
        // this code will write one public constructor or one private and one public.
        // The private one is to be annotated with JsonCreator for use by Jackson.
        // TODO javadoc
        out.right().right();
        // collect constructor parameters
        final String parametersNullable;
        if (cls.unwrapSingleField()) {
            // don't annotate parameters with JsonProperty because we will annotate field
            // with JsonValue
            parametersNullable = cls.fields.stream().map(
                    x -> String.format("\n%s%s %s", out.indent(), x.resolvedTypeNullable(imports), x.fieldName(cls)))
                    .collect(Collectors.joining(","));
        } else {
            parametersNullable = cls.fields.stream()
                    .map(x -> String.format("\n%s@%s(\"%s\") %s %s", out.indent(), imports.add(JsonProperty.class),
                            x.name, x.resolvedTypeNullable(imports), x.fieldName(cls)))
                    .collect(Collectors.joining(","));
        }
        out.left().left();
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());

        if (cls.classType != ClassType.ENUM) {
            out.line("@%s", imports.add(JsonCreator.class));
        } else {
            out.println();
        }
        boolean hasOptional = cls.fields.stream().anyMatch(f -> !f.required);
        boolean hasBinary = cls.fields.stream().anyMatch(Field::isOctets);
        // if has optional or other criteria then write a private constructor with
        // nullable parameters
        // and a public constructor with Optional parameters
        final String visibility = cls.classType == ClassType.ENUM || hasOptional || hasBinary || !interfaces.isEmpty()
                ? "private"
                : "public";
        if (visibility.equals("public")) {
            addConstructorBindingAnnotation(out, imports, names);
        }
        out.line("%s %s(%s) {\n", visibility, Names.simpleClassName(cls.fullClassName), parametersNullable);
        out.right();
        ifValidate(cls, out, imports, names, //
                out2 -> cls.fields.stream().forEach(x -> {
                    if (!x.isPrimitive() && x.required && !visibility.equals("private")) {
                        checkNotNull(imports, cls, out2, x);
                    }
                    validateMore(out2, imports, cls, x);
                }));

        // assign
        cls.fields.stream().forEach(x -> {
            assignField(out, indent, cls, x);
        });
        out.closeParen();
        if (hasOptional || !interfaces.isEmpty() || hasBinary) {
            out.right().right();
            String parametersOptional = cls.fields //
                    .stream() //
                    // ignore discriminators that should be constants
                    .filter(x -> !isDiscriminator(interfaces, x)) //
                    .map(x -> String.format("\n%s%s %s", indent, x.resolvedType(imports), x.fieldName(cls))) //
                    .collect(Collectors.joining(","));
            out.left().left();
            out.println();
            addConstructorBindingAnnotation(out, imports, names);
            out.line("public %s(%s) {", Names.simpleClassName(cls.fullClassName), parametersOptional);
            out.right();
            // validate
            ifValidate(cls, out, imports, names, //
                    out2 -> cls.fields.stream().forEach(x -> {
                        if (!isDiscriminator(interfaces, x) && (x.isOctets() || !x.isPrimitive() && !x.isByteArray())) {
                            checkNotNull(imports, cls, out2, x);
                            validateMore(out2, imports, cls, x);
                        }
                    }));

            // assign
            cls.fields.stream().forEach(x -> {
                Optional<Discriminator> disc = discriminator(interfaces, x);
                if (disc.isPresent()) {
                    // write constant value for discriminator
                    out.line("this.%s = \"%s\";", x.fieldName(cls),
                            disc.get().discriminatorValueFromFullClassName(cls.fullClassName));
                } else if (!x.isPrimitive() && !x.isByteArray()) {
                    if (x.required) {
                        assignField(out, indent, cls, x);
                    } else {
                        assignOptionalField(out, cls, x);
                    }
                } else if (x.isOctets()) {
                    assignEncodedOctets(out, imports, cls, x);
                } else {
                    assignField(out, indent, cls, x);
                }
            });
            out.closeParen();
        }
    }

    private static void writeBuilder(CodePrintWriter out, Imports imports, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        if (cls.classType == ClassType.ENUM) {
            return;
        }
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());
        List<BuilderWriter.Field> fields = cls.fields //
                .stream() //
                .filter(x -> !isDiscriminator(interfaces, x)) //
                .map(f -> new BuilderWriter.Field(f.fieldName, f.fullClassName, f.required, f.isArray))
                .collect(Collectors.toList());
        BuilderWriter.write(out, fields, cls.simpleName(), imports);
    }

    private static void checkNotNull(Imports imports, Cls cls, CodePrintWriter out, Field x) {
        out.line("%s.checkNotNull(%s, \"%s\");", imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class),
                x.fieldName(cls), x.fieldName(cls));
    }

    private static void assignEncodedOctets(CodePrintWriter out, Imports imports, Cls cls, Field x) {
        out.line("this.%s = %s.encodeOctets(%s);", x.fieldName(cls), imports.add(Util.class), x.fieldName(cls));
    }

    private static void assignOptionalField(CodePrintWriter out, Cls cls, Field x) {
        out.line("this.%s = %s.orElse(null);", x.fieldName(cls), x.fieldName(cls));
    }

    private static boolean isDiscriminator(Set<Cls> interfaces, Field x) {
        return discriminator(interfaces, x).isPresent();
    }

    private static Optional<Discriminator> discriminator(Set<Cls> interfaces, Field x) {
        return interfaces.stream().filter(y -> x.name.equals(y.discriminator.propertyName)) //
                .map(y -> y.discriminator).findFirst();
    }

    private static void validateMore(CodePrintWriter out, Imports imports, Cls cls, Field x) {
        String raw = x.fieldName(cls);
        if (x.minLength.isPresent()) {
            out.line("%s.checkMinLength(%s, %s, \"%s\");",
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw, x.minLength.get(),
                    x.fieldName(cls));
        }
        if (x.maxLength.isPresent()) {
            out.line("%s.checkMaxLength(%s, %s, \"%s\");",
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw, x.maxLength.get(),
                    x.fieldName(cls));
        }
        if (x.pattern.isPresent()) {
            out.line("%s.checkMatchesPattern(%s, \"%s\", \"%s\");",
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw,
                    escapePattern(x.pattern.get()), x.fieldName(cls));
        }
        if (x.min.isPresent()) {
            out.line("%s.checkMinimum(%s, \"%s\", \"%s\", %s);",
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw, x.min.get().toString(),
                    x.fieldName(cls), x.exclusiveMin);
        }
        if (x.max.isPresent()) {
            out.line("%s.checkMaximum(%s, \"%s\", \"%s\", %s);",
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw, x.max.get().toString(),
                    x.fieldName(cls), x.exclusiveMax);
        }
        if (x.isArray && x.minItems.isPresent()) {
            out.line("%s.checkMinSize(%s, %s, \"%s\");",
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), x.fieldName(cls),
                    x.minItems.get(), x.fieldName(cls));
        }
        if (x.isArray && x.maxItems.isPresent()) {
            out.line("%s.checkMaxSize(%s, %s, \"%s\");",
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), x.fieldName(cls),
                    x.maxItems.get(), x.fieldName(cls));
        }
    }

    private static String escapePattern(String pattern) {
        return pattern.replace("\\", "\\\\");
    }

    private static void writeEqualsMethod(CodePrintWriter out, Imports imports, Cls cls) {
        addOverrideAnnotation(out, imports);
        out.line("public boolean equals(Object o) {\n");
        out.right();
        out.line("if (this == o) {");
        out.right();
        out.line("return true;");
        out.closeParen();
        out.line("if (o == null || getClass() != o.getClass()) {");
        out.right();
        out.line("return false;");
        out.closeParen();
        out.right();
        String s = cls.fields
                .stream().map(x -> String.format("\n%s%s.equals(this.%s, other.%s)", out.indent(),
                        imports.add(Objects.class), x.fieldName(cls), x.fieldName(cls)))
                .collect(Collectors.joining(" && "));
        out.left();
        if (!s.isEmpty()) {
            out.line("%s other = (%s) o;", cls.simpleName(), cls.simpleName());
        }
        out.line("return %s;", s.isEmpty() ? "true" : s);
        out.closeParen();
    }

    private static void writeHashCodeMethod(CodePrintWriter out, Imports imports, Indent indent, Cls cls) {
        addOverrideAnnotation(out, imports);
        out.format("%spublic int hashCode() {\n", indent);
        final String s;
        if (cls.fields.size() <= 3) {
            s = cls.fields.stream().map(x -> x.fieldName(cls)).collect(Collectors.joining(", "));
        } else {
            indent.right().right().right();
            s = cls.fields.stream().map(x -> String.format("\n%s%s", indent, x.fieldName(cls)))
                    .collect(Collectors.joining(", "));
            indent.left().left().left();
        }
        out.format("%sreturn %s.hash(%s);\n", indent.right(), imports.add(Objects.class), s);
        closeParen(out, indent);
    }

    private static void writeToStringMethod(CodePrintWriter out, Imports imports, Indent indent, Cls cls) {
        addOverrideAnnotation(out, imports);
        out.format("%spublic String toString() {\n", indent);
        final String s;
        if (cls.fields.size() > 3) {
            indent.right().right().right();
            s = cls.fields.stream()
                    .map(x -> String.format(",\n%s\"%s\", %s", indent, x.fieldName(cls), x.fieldName(cls)))
                    .collect(Collectors.joining());
            indent.left().left().left();
        } else {
            s = cls.fields.stream().map(x -> String.format(", \"%s\", %s", x.fieldName(cls), x.fieldName(cls)))
                    .collect(Collectors.joining(""));
        }
        out.format("%sreturn %s.toString(%s.class%s);\n", indent.right(), imports.add(Util.class), cls.simpleName(), s);
        closeParen(out, indent);
    }

    private static void ifValidate(Cls cls, CodePrintWriter out, Imports imports, Names names,
            Consumer<CodePrintWriter> r) {
        CodePrintWriter b = CodePrintWriter.create();
        out.right();
        r.accept(b);
        out.left();
        b.close();
        String text = b.text();
        if (text.isEmpty()) {
            return;
        } else {
            out.line("if (%s.config().validateInConstructor().test(%s.class)) {",
                    imports.add(names.globalsFullClassName()), cls.simpleName());
            out.print(text);
            out.line("}");
        }
    }

    private static void assignField(CodePrintWriter out, Indent indent, Cls cls, Field x) {
        out.format("%sthis.%s = %s;\n", indent, x.fieldName(cls), x.fieldName(cls));
    }

    private static void writeGetters(CodePrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());
        cls.fields.forEach(f -> {
            if (interfaces.stream().anyMatch(c -> c.discriminator.propertyName.equals(f.name))) {
                addOverrideAnnotation(out, imports);
            } else {
                out.println();
            }
            final String value;
            if (!f.isOctets() && !f.required) {
                value = String.format("%s.ofNullable(%s)", imports.add(Optional.class), f.fieldName(cls));
            } else if (f.isOctets()) {
                value = String.format("%s.decodeOctets(%s)", imports.add(Util.class), f.fieldName(cls));
            } else {
                value = f.fieldName(cls);
            }
            writeGetter(out, indent, f.resolvedType(imports), f.fieldName(cls), value);
        });
    }

    private static void writeGetter(CodePrintWriter out, Indent indent, String returnImportedType, String fieldName,
            String value) {
        out.format("%spublic %s %s() {\n", indent, returnImportedType, fieldName);
        indent.right();
        out.format("%sreturn %s;\n", indent, value);
        closeParen(out, indent);
    }

    private static void writeMemberClasses(CodePrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces, Names names) {
        cls.classes.forEach(c -> writeClass(out, imports, indent, c, fullClassNameInterfaces, names));
    }

}
