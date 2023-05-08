package org.davidmoten.oa3.codegen.generator.writer;

import static org.davidmoten.oa3.codegen.generator.internal.Util.toPrimitive;
import static org.davidmoten.oa3.codegen.generator.internal.WriterUtil.IMPORTS_HERE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.BuilderWriter;
import org.davidmoten.oa3.codegen.generator.Generator.ClassType;
import org.davidmoten.oa3.codegen.generator.Generator.Cls;
import org.davidmoten.oa3.codegen.generator.Generator.Discriminator;
import org.davidmoten.oa3.codegen.generator.Generator.Encoding;
import org.davidmoten.oa3.codegen.generator.Generator.Field;
import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.SchemaCategory;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Javadoc;
import org.davidmoten.oa3.codegen.generator.internal.Mutable;
import org.davidmoten.oa3.codegen.generator.internal.WriterUtil;
import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.runtime.PolymorphicDeserializer;
import org.davidmoten.oa3.codegen.runtime.PolymorphicType;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
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
        CodePrintWriter out = CodePrintWriter.create(cls.fullClassName);
        SchemasCodeWriter.writeClass(out, cls, fullClassNameInterfaces, names);
        WriterUtil.writeContent(names, out);
    }

    public static void writeGlobalsClass(Names names) {
        String fullClassName = names.globalsFullClassName();
        CodePrintWriter out = CodePrintWriter.create(fullClassName);
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
        writeClassDeclaration(out, cls, fullClassNameInterfaces);
        writeEnumMembers(out, cls);
        if (isPolymorphic(cls)) {
            writePolymorphicClassContent(out, cls, names, fullClassNameInterfaces);
        } else {
            writeFields(out, cls);
            writeConstructor(out, cls, fullClassNameInterfaces, names);
            writeBuilder(out, cls, fullClassNameInterfaces);
            writeGetters(out, cls, fullClassNameInterfaces);
        }
        writeEnumCreator(out, cls);
        writeMemberClasses(out, cls, fullClassNameInterfaces, names);
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeEqualsMethod(out, cls);
            writeHashCodeMethod(out, cls);
            writeToStringMethod(out, cls);
        }
        out.closeParen();
    }

    private static boolean isPolymorphic(Cls cls) {
        return cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED
                || cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED || cls.classType == ClassType.ALL_OF;
    }

    private static void addOverrideAnnotation(CodePrintWriter out) {
        out.println();
        out.line("@%s", out.add(Override.class));
    }

    private static void writeEnumCreator(CodePrintWriter out, Cls cls) {
        if (cls.classType == ClassType.ENUM) {
            String simpleClassName = Names.simpleClassName(cls.fullClassName);
            out.println();
            out.line("@%s", JsonCreator.class);
            out.line("public static %s fromValue(%s value) {", simpleClassName, Object.class);
            out.line("for (%s x: %s.values()) {", simpleClassName, simpleClassName);
            // be careful because x.value can be primitive
            out.line("if (value.equals(x.value)) {");
            out.line("return x;");
            out.closeParen();
            out.closeParen();
            out.line("throw new %s(\"unexpected enum value: '\" + value + \"'\");", IllegalArgumentException.class);
            out.closeParen();
        }
    }

    private static void writeClassDeclaration(CodePrintWriter out, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        String modifier = classModifier(cls);
        Set<Cls> interfaces = fullClassNameInterfaces.get(cls.fullClassName);
        String implementsClause = implementsClause(out.imports(), interfaces);
        final boolean javadocExists;
        if (cls.description.isPresent()) {
            javadocExists = Javadoc.printJavadoc(out, out.indent(), cls.description.get(), true);
        } else {
            javadocExists = false;
        }
        if (!javadocExists) {
            out.println();
        }
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeJsonTypeInfoAnnotation(out, cls);
        } else if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED || cls.classType == ClassType.ALL_OF) {
            writePolymorphicDeserializerAnnotation(out, cls);
        } 
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeJsonIncludeAnnotation(out);
            writeAutoDetectAnnotation(out);
        }
        if (cls.topLevel) {
            WriterUtil.addGeneratedAnnotation(out);
        }
        out.line("public %s%s %s%s {", modifier, cls.classType.word(), cls.simpleName(), implementsClause);
    }

    private static void writeJsonIncludeAnnotation(CodePrintWriter out) {
        out.line("@%s(%s.NON_NULL)", JsonInclude.class, Include.class);
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

    private static void writeJsonTypeInfoAnnotation(CodePrintWriter out, Cls cls) {
        out.line("@%s(use = %s.NAME, property = \"%s\", include = %s.EXISTING_PROPERTY, visible = true)",
                JsonTypeInfo.class, Id.class, cls.discriminator.propertyName, As.class);
        out.right().right();
        String types = cls.fields.stream()
                .map(x -> String.format("\n%s@%s(value = %s.class, name = \"%s\")", out.indent(), out.add(Type.class),
                        out.add(x.fullClassName),
                        cls.discriminator.discriminatorValueFromFullClassName(x.fullClassName)))
                .collect(Collectors.joining(", "));
        out.left().left();
        out.line("@%s({%s})", JsonSubTypes.class, types);
    }

    private static void addConstructorBindingAnnotation(CodePrintWriter out, Names names) {
        if (names.generatorIsSpring3()) {
            out.line("@%s", out
                    .add(ConstructorBinding.class.getName().replace("ConstructorBinding", "bind.ConstructorBinding")));
        } else {
            out.line("@%s", ConstructorBinding.class);
        }
    }

    private static void writePolymorphicDeserializerAnnotation(CodePrintWriter out, Cls cls) {
        out.line("@%s(using = %s.Deserializer.class)", JsonDeserialize.class, cls.simpleName());
    }

    private static void writeAutoDetectAnnotation(CodePrintWriter out) {
        out.line("@%s(fieldVisibility = %s.ANY, creatorVisibility = %s.ANY)", JsonAutoDetect.class, Visibility.class,
                Visibility.class);
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

    private static void writePolymorphicClassContent(CodePrintWriter out, Cls cls, Names names,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.println();
            out.line("%s %s();", String.class, cls.discriminator.fieldName);
        } else {
            if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED) {
                out.println();
                writeJsonValueAnnotation(out);
                out.line("private final %s %s;", Object.class, "value");

                // add constructor for each member of the oneOf (fieldTypes)
                // as there are multiple constructors we cannot add ConstructorBinding
                // annotations so polymorphic stuff can't be used to bind to rest method
                // parameters
                writeOneOfAnyOfNonDiscriminatedObjectConstructor(out, cls);
                cls.fields.forEach(f -> writeOneOfAnyOfNonDiscriminatedMemberSpecificConstructor(out, cls, f));
                writeNonDiscriminatedBuilder(out, cls);
                out.println();
                writeGetter(out, out.add(Object.class), "value", "value");
            } else {
                // allof
                writeFields(out, cls);

                out.right().right();
                final String parametersNullable;
                parametersNullable = cls.fields
                        .stream().map(x -> String.format("\n%s%s %s", out.indent(),
                                x.resolvedTypeNullable(out.imports()), x.fieldName(cls)))
                        .collect(Collectors.joining(","));
                out.left().left();
                out.println();
                out.line("public %s(%s) {", Names.simpleClassName(cls.fullClassName), parametersNullable);
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
                writeGetters(out, cls, fullClassNameInterfaces);
            }
            out.println();
            out.line("@%s(\"serial\")", SuppressWarnings.class);
            out.line("public static final class Deserializer extends %s<%s> {", PolymorphicDeserializer.class,
                    cls.simpleName());
            out.println();
            out.line("public Deserializer() {");
            String classes = cls.fields.stream().map(x -> out.add(toPrimitive(x.fullClassName)) + ".class")
                    .collect(Collectors.joining(", "));
            out.line("super(%s.config(), %s.%s, %s.class, %s);", out.add(names.globalsFullClassName()),
                    PolymorphicType.class, cls.polymorphicType.name(), cls.simpleName(), classes);
            out.closeParen();
            out.closeParen();
        }
    }

    private static void writeNonDiscriminatedBuilder(CodePrintWriter out, Cls cls) {
        cls.fields.forEach(f -> {
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
            out.line("this.value = %s.checkNotNull(value, \"value\");", out.add(Preconditions.class));
        }
        out.closeParen();
    }

    private static void writeOneOfAnyOfNonDiscriminatedObjectConstructor(CodePrintWriter out, Cls cls) {
        out.println();
        out.line("@%s", out.add(JsonCreator.class));
        out.line("private %s(%s value) {", cls.simpleName(), out.add(Object.class));
        out.line("this.value = %s.checkNotNull(value, \"value\");", out.add(Preconditions.class));
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
            if (cls.classType == ClassType.ALL_OF) {
                out.line("@%s", out.add(JsonUnwrapped.class));
            } else if (cls.unwrapSingleField()) {
                writeJsonValueAnnotation(out);
            } else {
                out.line("@%s(\"%s\")", out.add(JsonProperty.class), f.name);
            }
            final String fieldType;
            if (f.encoding == Encoding.OCTET) {
                fieldType = out.add(String.class);
            } else {
                fieldType = f.resolvedTypeNullable(out.imports());
            }
            out.line("private final %s %s;", fieldType, cls.fieldName(f));
        });
    }

    private static void writeJsonValueAnnotation(CodePrintWriter out) {
        out.line("@%s", out.add(JsonValue.class));
    }

    private static void writeConstructor(CodePrintWriter out, Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces,
            Names names) {
        // this code will write one public constructor or one private and one public.
        // The private one is to be annotated with JsonCreator for use by Jackson.
        // TODO javadoc
        out.right().right();
        // collect constructor parameters
        final String parametersNullable;
        if (cls.unwrapSingleField()) {
            // don't annotate parameters with JsonProperty because we will annotate field
            // with JsonValue
            parametersNullable = cls.fields.stream().map(x -> String.format("\n%s%s %s", out.indent(),
                    x.resolvedTypeNullable(out.imports()), x.fieldName(cls))).collect(Collectors.joining(","));
        } else {
            parametersNullable = cls.fields
                    .stream().map(x -> String.format("\n%s@%s(\"%s\") %s %s", out.indent(), out.add(JsonProperty.class),
                            x.name, x.resolvedTypeNullable(out.imports()), x.fieldName(cls)))
                    .collect(Collectors.joining(","));
        }
        out.left().left();
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());

        if (cls.classType != ClassType.ENUM) {
            out.line("@%s", out.add(JsonCreator.class));
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
            addConstructorBindingAnnotation(out, names);
        }
        out.line("%s %s(%s) {", visibility, Names.simpleClassName(cls.fullClassName), parametersNullable);
        ifValidate(cls, out, names, //
                out2 -> cls.fields.stream().forEach(x -> {
                    if (!x.isPrimitive() && x.required && !visibility.equals("private")) {
                        checkNotNull(cls, out2, x);
                    }
                    validateMore(out2, cls, x);
                }));

        // assign
        cls.fields.stream().forEach(x -> {
            assignField(out, cls, x);
        });
        out.closeParen();
        if (hasOptional || !interfaces.isEmpty() || hasBinary) {
            out.right().right();
            String parametersOptional = cls.fields //
                    .stream() //
                    // ignore discriminators that should be constants
                    .filter(x -> !isDiscriminator(interfaces, x)) //
                    .map(x -> String.format("\n%s%s %s", out.indent(), x.resolvedType(out.imports()), x.fieldName(cls))) //
                    .collect(Collectors.joining(","));
            out.left().left();
            out.println();
            addConstructorBindingAnnotation(out, names);
            out.line("public %s(%s) {", Names.simpleClassName(cls.fullClassName), parametersOptional);
            // validate
            ifValidate(cls, out, names, //
                    out2 -> cls.fields.stream().forEach(x -> {
                        if (!isDiscriminator(interfaces, x) && (x.isOctets() || !x.isPrimitive() && !x.isByteArray())) {
                            checkNotNull(cls, out2, x);
                            validateMore(out2, cls, x);
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
                        assignField(out, cls, x);
                    } else {
                        assignOptionalField(out, cls, x);
                    }
                } else if (x.isOctets()) {
                    assignEncodedOctets(out, cls, x);
                } else {
                    assignField(out, cls, x);
                }
            });
            out.closeParen();
        }
    }

    private static void writeBuilder(CodePrintWriter out, Cls cls, Map<String, Set<Cls>> fullClassNameInterfaces) {
        if (cls.classType == ClassType.ENUM) {
            return;
        }
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());
        List<BuilderWriter.Field> fields = cls.fields //
                .stream() //
                .filter(x -> !isDiscriminator(interfaces, x)) //
                .map(f -> new BuilderWriter.Field(f.fieldName, f.fullClassName, f.required, f.isArray))
                .collect(Collectors.toList());
        BuilderWriter.write(out, fields, cls.simpleName());
    }

    private static void checkNotNull(Cls cls, CodePrintWriter out, Field x) {
        out.line("%s.checkNotNull(%s, \"%s\");", Preconditions.class,
                x.fieldName(cls), x.fieldName(cls));
    }

    private static void assignEncodedOctets(CodePrintWriter out, Cls cls, Field x) {
        out.line("this.%s = %s.encodeOctets(%s);", x.fieldName(cls), out.add(Util.class), x.fieldName(cls));
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

    private static void validateMore(CodePrintWriter out, Cls cls, Field x) {
        String raw = x.fieldName(cls);
        if (x.minLength.isPresent()) {
            out.line("%s.checkMinLength(%s, %s, \"%s\");", Preconditions.class, raw, x.minLength.get(),
                    x.fieldName(cls));
        }
        if (x.maxLength.isPresent()) {
            out.line("%s.checkMaxLength(%s, %s, \"%s\");", Preconditions.class, raw, x.maxLength.get(),
                    x.fieldName(cls));
        }
        if (x.pattern.isPresent()) {
            out.line("%s.checkMatchesPattern(%s, \"%s\", \"%s\");", Preconditions.class, raw,
                    escapePattern(x.pattern.get()), x.fieldName(cls));
        }
        if (x.min.isPresent()) {
            out.line("%s.checkMinimum(%s, \"%s\", \"%s\", %s);", Preconditions.class, raw, x.min.get().toString(),
                    x.fieldName(cls), x.exclusiveMin);
        }
        if (x.max.isPresent()) {
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

    private static String escapePattern(String pattern) {
        return pattern.replace("\\", "\\\\");
    }

    private static void writeEqualsMethod(CodePrintWriter out, Cls cls) {
        addOverrideAnnotation(out);
        out.line("public boolean equals(Object o) {");
        out.line("if (this == o) {");
        out.line("return true;");
        out.closeParen();
        out.line("if (o == null || getClass() != o.getClass()) {");
        out.line("return false;");
        out.closeParen();
        out.right();
        String s = cls.fields
                .stream().map(x -> String.format("\n%s%s.equals(this.%s, other.%s)", out.indent(),
                        out.add(Objects.class), x.fieldName(cls), x.fieldName(cls)))
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
        out.line("return %s.hash(%s);", out.add(Objects.class), s);
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
        out.line("public String toString() {");
        out.line("return %s.toString(%s.class%s);", out.add(Util.class), cls.simpleName(), s);
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
            if (interfaces.stream().anyMatch(c -> c.discriminator.propertyName.equals(f.name))) {
                addOverrideAnnotation(out);
            } else {
                out.println();
            }
            final String value;
            if (!f.isOctets() && !f.required) {
                value = String.format("%s.ofNullable(%s)", out.add(Optional.class), f.fieldName(cls));
            } else if (f.isOctets()) {
                value = String.format("%s.decodeOctets(%s)", out.add(Util.class), f.fieldName(cls));
            } else {
                value = f.fieldName(cls);
            }
            writeGetter(out, f.resolvedType(out.imports()), f.fieldName(cls), value);
        });
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
