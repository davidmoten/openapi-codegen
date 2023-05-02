package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.generator.WriterUtil.IMPORTS_HERE;
import static org.davidmoten.oa3.codegen.generator.WriterUtil.closeParen;
import static org.davidmoten.oa3.codegen.generator.internal.Util.toPrimitive;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.Generator.ClassType;
import org.davidmoten.oa3.codegen.generator.Generator.Cls;
import org.davidmoten.oa3.codegen.generator.Generator.Discriminator;
import org.davidmoten.oa3.codegen.generator.Generator.Encoding;
import org.davidmoten.oa3.codegen.generator.Generator.Field;
import org.davidmoten.oa3.codegen.generator.internal.ByteArrayPrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.generator.internal.Javadoc;
import org.davidmoten.oa3.codegen.generator.internal.Mutable;
import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.runtime.internal.PolymorphicDeserializer;
import org.davidmoten.oa3.codegen.runtime.internal.PolymorphicType;
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

final class SchemasCodeWriter {

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

    static void writeSchemaClass(Names names, Map<String, Set<Cls>> fullClassNameInterfaces, Cls cls,
            String schemaName) {
        Imports imports = new Imports(cls.fullClassName);
        if ((cls.category == SchemaCategory.PATH || cls.category == SchemaCategory.RESPONSE) && cls.schema.isPresent()
                && cls.schema.get().get$ref() != null) {
            // when a cls has a ref and is used with a Path or Response then the ref class
            // is used in generated code
            return;
        }
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        Indent indent = new Indent();
        SchemasCodeWriter.writeClass(out, imports, indent, cls, fullClassNameInterfaces, names);
        WriterUtil.writeContent(names, out, names.schemaNameToFullClassName(cls.category, schemaName), imports);
    }

    static void writeGlobalsClass(Names names) {
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        Indent indent = new Indent();
        String fullClassName = names.globalsFullClassName();
        Imports imports = new Imports(fullClassName);
        out.format("package %s;\n", Names.pkg(fullClassName));
        out.format("\n%s", IMPORTS_HERE);
        out.println();
        addGeneratedAnnotation(out, imports, indent);
        out.format("public final class %s {\n", Names.simpleClassName(fullClassName));
        indent.right();
        out.format("\n%sprivate static volatile %s config = %s.builder().build();\n", indent, imports.add(Config.class),
                imports.add(Config.class));
        out.format("\n%spublic static void setConfig(%s configuration) {\n", indent, imports.add(Config.class));
        indent.right();
        out.format("%sconfig = configuration;\n", indent);
        indent.left();
        out.format("%s}\n", indent);
        out.format("\n%spublic static %s config() {\n", indent, imports.add(Config.class));
        indent.right();
        out.format("%sreturn config;\n", indent);
        indent.left();
        out.format("%s}\n", indent);
        indent.left();
        out.format("%s}\n", indent);
        WriterUtil.writeContent(names, out, fullClassName, imports);
    }

    private static void writeClass(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces, Names names) {
        if (cls.topLevel) {
            out.format("package %s;\n", cls.pkg());
            out.format("\n%s", IMPORTS_HERE);
        }
        writeClassDeclaration(out, imports, indent, cls, fullClassNameInterfaces);
        indent.right();
        writeEnumMembers(out, indent, cls);
        if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED
                || cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED || cls.classType == ClassType.ALL_OF) {
            writePolymorphicClassContent(out, imports, indent, cls, names, fullClassNameInterfaces);
        } else {
            writeFields(out, imports, indent, cls);
            writeConstructor(out, imports, indent, cls, fullClassNameInterfaces, names);
            writeGetters(out, imports, indent, cls, fullClassNameInterfaces);
        }
        writeEnumCreator(out, imports, indent, cls);
        writeMemberClasses(out, imports, indent, cls, fullClassNameInterfaces, names);
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            writeEqualsMethod(out, imports, indent, cls);
            writeHashCodeMethod(out, imports, indent, cls);
            writeToStringMethod(out, imports, indent, cls);
        }
        closeParen(out, indent);
    }

    private static void addOverrideAnnotation(PrintWriter out, Imports imports, Indent indent) {
        out.format("\n%s@%s\n", indent, imports.add(Override.class));
    }

    private static void addGeneratedAnnotation(PrintWriter out, Imports imports, Indent indent) {
        out.format("%s@%s(value = \"%s\")\n", indent, imports.add(Generated.class), version);
    }

    private static void writeEnumCreator(PrintWriter out, Imports imports, Indent indent, Cls cls) {
        if (cls.classType == ClassType.ENUM) {
//            @JsonCreator
//            public static SubjectIndicator fromValue(String value) {
//              for (SubjectIndicator b : SubjectIndicator.values()) {
//                if (b.value.equals(value)) {
//                  return b;
//                }
//              }
//              throw new IllegalArgumentException("Unexpected value '" + value + "'");
//            }
            String simpleClassName = Names.simpleClassName(cls.fullClassName);
            out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
            out.format("%spublic static %s fromValue(%s value) {\n", indent, simpleClassName,
                    imports.add(Object.class));
            indent.right();
            out.format("%sfor (%s x: %s.values()) {\n", indent, simpleClassName, simpleClassName);
            indent.right();
            // be careful because x.value can be primitive
            out.format("%sif (value.equals(x.value)) {\n", indent);
            indent.right();
            out.format("%sreturn x;\n", indent);
            closeParen(out, indent);
            closeParen(out, indent);
            out.format("%sthrow new %s(\"unexpected enum value: '\" + value + \"'\");\n", indent,
                    imports.add(IllegalArgumentException.class));
            closeParen(out, indent);
        }
    }

    private static void writeClassDeclaration(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        final String modifier;
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED || cls.classType == ClassType.ENUM) {
            modifier = "";
        } else {
            modifier = cls.topLevel ? "final " : "static final ";
        }
        Set<Cls> interfaces = fullClassNameInterfaces.get(cls.fullClassName);
        final String implemented;
        if (interfaces == null || interfaces.isEmpty()) {
            implemented = "";
        } else {
            implemented = " implements "
                    + interfaces.stream().map(x -> imports.add(x.fullClassName)).collect(Collectors.joining(", "));
        }
        if (cls.description.isPresent()) {
            Javadoc.printJavadoc(out, indent, cls.description.get(), true);
        }
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.format("\n%s@%s(use = %s.NAME, property = \"%s\", include = %s.EXISTING_PROPERTY, visible = true)\n",
                    indent, imports.add(JsonTypeInfo.class), imports.add(Id.class), cls.discriminator.propertyName,
                    imports.add(As.class));
            indent.right().right();
            String types = cls.fields.stream()
                    .map(x -> String.format("\n%s@%s(value = %s.class, name = \"%s\")", indent, imports.add(Type.class),
                            imports.add(x.fullClassName),
                            cls.discriminator.discriminatorValueFromFullClassName(x.fullClassName)))
                    .collect(Collectors.joining(", "));
            indent.left().left();
            out.format("%s@%s({%s})\n", indent, imports.add(JsonSubTypes.class), types);
        } else if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED || cls.classType == ClassType.ALL_OF) {
            writePolymorphicDeserializerAnnotation(out, imports, indent, cls);
        } else {
            out.println();
        }
        if (cls.classType != ClassType.ENUM && cls.classType != ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.format("%s@%s(%s.NON_NULL)\n", indent, imports.add(JsonInclude.class), imports.add(Include.class));
            writeAutoDetectAnnotation(out, imports, indent);
        }
        if (cls.topLevel) {
            addGeneratedAnnotation(out, imports, indent);
        }
        out.format("%spublic %s%s %s%s {\n", indent, modifier, cls.classType.word(), cls.simpleName(), implemented);
    }

    private static void addConstructorBindingAnnotation(PrintWriter out, Imports imports, Indent indent, Names names) {
        if (names.generatorIsSpring3()) {
            out.format("%s@%s\n", indent, imports.add(ConstructorBinding.class.getName().replace("ConstructorBinding", "bind.ConstructorBinding")));
        } else {
            out.format("%s@%s\n", indent, imports.add(ConstructorBinding.class));
        }
    }

    private static void writePolymorphicDeserializerAnnotation(PrintWriter out, Imports imports, Indent indent,
            Cls cls) {
        out.format("\n%s@%s(using = %s.Deserializer.class)\n", indent, imports.add(JsonDeserialize.class),
                cls.simpleName());
    }

    private static void writeAutoDetectAnnotation(PrintWriter out, Imports imports, Indent indent) {
        out.format("%s@%s(fieldVisibility = %s.ANY, creatorVisibility = %s.ANY)\n", indent,
                imports.add(JsonAutoDetect.class), imports.add(Visibility.class), imports.add(Visibility.class));
    }

    private static void writeEnumMembers(PrintWriter out, Indent indent, Cls cls) {
        String text = cls.enumMembers.stream().map(x -> {
            String delim = x.parameter instanceof String ? "\"" : "";
            return String.format("%s%s(%s%s%s)", indent, x.name, delim, x.parameter, delim);
        }).collect(Collectors.joining(",\n"));
        if (!text.isEmpty()) {
            out.println("\n" + text + ";");
        }
    }

    private static void writePolymorphicClassContent(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Names names, Map<String, Set<Cls>> fullClassNameInterfaces) {
        if (cls.classType == ClassType.ONE_OR_ANY_OF_DISCRIMINATED) {
            out.format("\n%s%s %s();\n", indent, imports.add(String.class), cls.discriminator.fieldName);
        } else {
            if (cls.classType == ClassType.ONE_OR_ANY_OF_NON_DISCRIMINATED) {
                out.println();
                out.format("%s@%s\n", indent, imports.add(JsonValue.class));
                out.format("%sprivate final %s %s;\n", indent, imports.add(Object.class), "value");

                // add constructor for each member of the oneOf (fieldTypes)
                // as there are multiple constructors we cannot add ConstructorBinding
                // annotations
                // so polymorphic stuff can't be used to bind to rest method parameters
                out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
                out.format("%sprivate %s(%s value) {\n", indent, cls.simpleName(), imports.add(Object.class));
                out.format("%sthis.value = %s.checkNotNull(value, \"value\");\n", indent.right(),
                        imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class));
                out.format("%s}\n", indent.left());
                cls.fields.forEach(f -> {
                    String className = toPrimitive(f.fullClassName);
                    out.format("\n%spublic %s(%s value) {\n", indent, cls.simpleName(), imports.add(className));
                    indent.right();
                    if (org.davidmoten.oa3.codegen.generator.internal.Util.isPrimitiveFullClassName(className)) {
                        out.format("%sthis.value = value;\n", indent);
                    } else {
                        out.format("%sthis.value = %s.checkNotNull(value, \"value\");\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class));
                    }
                    out.format("%s}\n", indent.left());
                });

                out.format("\n%spublic Object value() {\n", indent);
                out.format("%sreturn value;\n", indent.right());
                out.format("%s}\n", indent.left());
            } else {
                // allof
                writeFields(out, imports, indent, cls);

                indent.right().right();
                final String parametersNullable;
                parametersNullable = cls.fields.stream()
                        .map(x -> String.format("\n%s%s %s", indent, x.resolvedTypeNullable(imports), x.fieldName(cls)))
                        .collect(Collectors.joining(","));
                indent.left().left();
                out.println();
                out.format("%spublic %s(%s) {\n", indent, Names.simpleClassName(cls.fullClassName), parametersNullable);
                indent.right();
                ifValidate(cls, out, indent, imports, names, //
                        out2 -> cls.fields.stream().forEach(x -> {
                            if (!x.isPrimitive() && x.required) {
                                out2.format("%s%s.checkNotNull(%s, \"%s\");\n", indent,
                                        imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class),
                                        x.fieldName(cls), x.fieldName(cls));
                            } else {
                                out.format("%s// ???\n", indent);
                            }
                            validateMore(out2, imports, indent, cls, x, false);
                        }));
                cls.fields.stream().forEach(x -> {
                    assignField(out, indent, cls, x);
                });
                closeParen(out, indent);
                writeGetters(out, imports, indent, cls, fullClassNameInterfaces);
            }
            out.format("\n%s@%s(\"serial\")\n", indent, imports.add(SuppressWarnings.class));
            out.format("%spublic static final class Deserializer extends %s<%s> {\n", indent,
                    imports.add(PolymorphicDeserializer.class), cls.simpleName());
            indent.right();
            out.format("\n%spublic Deserializer() {\n", indent);
            indent.right();
            String classes = cls.fields.stream().map(x -> imports.add(toPrimitive(x.fullClassName)) + ".class")
                    .collect(Collectors.joining(", "));
            out.format("%ssuper(%s.config(), %s.%s, %s.class, %s);\n", indent,
                    imports.add(names.globalsFullClassName()), imports.add(PolymorphicType.class),
                    cls.polymorphicType.name(), cls.simpleName(), classes);
            closeParen(out, indent);
            closeParen(out, indent);
        }
    }

    private static void writeFields(PrintWriter out, Imports imports, Indent indent, Cls cls) {
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
                out.format("%s@%s\n", indent, imports.add(JsonUnwrapped.class));
            } else if (cls.unwrapSingleField()) {
                out.format("%s@%s\n", indent, imports.add(JsonValue.class));
            } else {
                out.format("%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class), f.name);
            }
            final String fieldType;
            if (f.encoding == Encoding.OCTET) {
                fieldType = imports.add(String.class);
            } else {
                fieldType = f.resolvedTypeNullable(imports);
            }
            out.format("%sprivate final %s %s;\n", indent, fieldType, cls.fieldName(f));
        });
    }

    private static void writeConstructor(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces, Names names) {
        // this code will write one public constructor or one private and one public.
        // The private one is to be annotated
        // with JsonCreator for use by Jackson.
        // TODO javadoc
        indent.right().right();
        final String parametersNullable;
        if (cls.unwrapSingleField()) {
            parametersNullable = cls.fields.stream()
                    .map(x -> String.format("\n%s%s %s", indent, x.resolvedTypeNullable(imports), x.fieldName(cls)))
                    .collect(Collectors.joining(","));
        } else {
            parametersNullable = cls.fields
                    .stream().map(x -> String.format("\n%s@%s(\"%s\") %s %s", indent, imports.add(JsonProperty.class),
                            x.name, x.resolvedTypeNullable(imports), x.fieldName(cls)))
                    .collect(Collectors.joining(","));
        }
        indent.left().left();
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());

        if (cls.classType != ClassType.ENUM) {
            out.format("\n%s@%s\n", indent, imports.add(JsonCreator.class));
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
            addConstructorBindingAnnotation(out, imports, indent, names);
        }
        out.format("%s%s %s(%s) {\n", indent, visibility, Names.simpleClassName(cls.fullClassName), parametersNullable);
        indent.right();
        ifValidate(cls, out, indent, imports, names, //
                out2 -> cls.fields.stream().forEach(x -> {
                    if (!x.isPrimitive() && x.required && !visibility.equals("private")) {
                        out2.format("%s%s.checkNotNull(%s, \"%s\");\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), x.fieldName(cls),
                                x.fieldName(cls));
                    }
                    validateMore(out2, imports, indent, cls, x, false);
                }));

        // assign
        cls.fields.stream().forEach(x -> {
            assignField(out, indent, cls, x);
        });
        closeParen(out, indent);
        if (hasOptional || !interfaces.isEmpty() || hasBinary) {
            indent.right().right();
            String parametersOptional = cls.fields.stream().filter(
                    x -> !interfaces.stream().map(y -> y.discriminator.propertyName).anyMatch(y -> x.name.equals(y)))
                    .map(x -> String.format("\n%s%s %s", indent, x.resolvedType(imports), x.fieldName(cls)))
                    .collect(Collectors.joining(","));
            indent.left().left();
            out.println();
            addConstructorBindingAnnotation(out, imports, indent, names);
            out.format("%spublic %s(%s) {\n", indent, Names.simpleClassName(cls.fullClassName), parametersOptional);
            indent.right();
            // validate
            ifValidate(cls, out, indent, imports, names, //
                    out2 -> cls.fields.stream().forEach(x -> {
                        Optional<Discriminator> disc = interfaces.stream()
                                .filter(y -> x.name.equals(y.discriminator.propertyName)).map(y -> y.discriminator)
                                .findFirst();
                        if (!disc.isPresent() && (x.isOctets() || !x.isPrimitive() && !x.isByteArray())) {
                            out2.format("%s%s.checkNotNull(%s, \"%s\");\n", indent,
                                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class),
                                    x.fieldName(cls), x.fieldName(cls));
                            validateMore(out2, imports, indent, cls, x, !x.required);
                        }
                    }));

            // assign
            cls.fields.stream().forEach(x -> {
                Optional<Discriminator> disc = interfaces.stream()
                        .filter(y -> x.name.equals(y.discriminator.propertyName)).map(y -> y.discriminator).findFirst();
                if (disc.isPresent()) {
                    out.format("%sthis.%s = \"%s\";\n", indent, x.fieldName(cls),
                            disc.get().discriminatorValueFromFullClassName(cls.fullClassName));
                } else if (!x.isPrimitive() && !x.isByteArray()) {
                    if (x.required) {
                        assignField(out, indent, cls, x);
                    } else {
                        out.format("%sthis.%s = %s.orElse(null);\n", indent, x.fieldName(cls), x.fieldName(cls));
                    }
                } else if (x.isOctets()) {
                    out.format("%sthis.%s = %s.encodeOctets(%s);\n", indent, x.fieldName(cls), imports.add(Util.class),
                            x.fieldName(cls));
                } else {
                    assignField(out, indent, cls, x);
                }
            });
            closeParen(out, indent);
        }
    }

    private static void validateMore(PrintWriter out, Imports imports, Indent indent, Cls cls, Field x,
            boolean useGet) {
        String raw = x.fieldName(cls) + (useGet ? ".get()" : "");
        if (x.minLength.isPresent()) {
            out.format("%s%s.checkMinLength(%s, %s, \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw, x.minLength.get(),
                    x.fieldName(cls));
        }
        if (x.maxLength.isPresent()) {
            out.format("%s%s.checkMaxLength(%s, %s, \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw, x.maxLength.get(),
                    x.fieldName(cls));
        }
        if (x.pattern.isPresent()) {
            out.format("%s%s.checkMatchesPattern(%s, \"%s\", \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw, escapePattern(x.pattern.get()),
                    x.fieldName(cls));
        }
        if (x.min.isPresent()) {
            out.format("%s%s.checkMinimum(%s, \"%s\", \"%s\", %s);\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw, x.min.get().toString(),
                    x.fieldName(cls), x.exclusiveMin);
        }
        if (x.max.isPresent()) {
            out.format("%s%s.checkMaximum(%s, \"%s\", \"%s\", %s);\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), raw, x.max.get().toString(),
                    x.fieldName(cls), x.exclusiveMax);
        }
        if (x.isArray && x.minItems.isPresent()) {
            out.format("%s%s.checkMinSize(%s, %s, \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), x.fieldName(cls),
                    x.minItems.get(), x.fieldName(cls));
        }
        if (x.isArray && x.maxItems.isPresent()) {
            out.format("%s%s.checkMaxSize(%s, %s, \"%s\");\n", indent,
                    imports.add(org.davidmoten.oa3.codegen.runtime.Preconditions.class), x.fieldName(cls),
                    x.maxItems.get(), x.fieldName(cls));
        }
    }

    private static String escapePattern(String pattern) {
        return pattern.replace("\\", "\\\\");
    }

    private static void writeEqualsMethod(PrintWriter out, Imports imports, Indent indent, Cls cls) {
//      @Override
//      public boolean equals(Object o) {
//        if (this == o) {
//          return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//          return false;
//        }
//        Point point = (Point) o;
//        return Objects.equals(this.lat, point.lat) &&
//            Objects.equals(this.lon, point.lon);
//      }
        addOverrideAnnotation(out, imports, indent);
        out.format("%spublic boolean equals(Object o) {\n", indent);
        indent.right();
        out.format("%sif (this == o) {\n", indent);
        out.format("%sreturn true;\n", indent.right());
        closeParen(out, indent);
        out.format("%sif (o == null || getClass() != o.getClass()) {\n", indent);
        out.format("%sreturn false;\n", indent.right());
        closeParen(out, indent);
        indent.right();
        String s = cls.fields.stream().map(x -> String.format("\n%s%s.equals(this.%s, other.%s)", indent,
                imports.add(Objects.class), x.fieldName(cls), x.fieldName(cls))).collect(Collectors.joining(" && "));
        indent.left();
        if (!s.isEmpty()) {
            out.format("%s%s other = (%s) o;\n", indent, cls.simpleName(), cls.simpleName());
        }
        out.format("%sreturn %s;\n", indent, s.isEmpty() ? "true" : s);
        closeParen(out, indent);
    }

    private static void writeHashCodeMethod(PrintWriter out, Imports imports, Indent indent, Cls cls) {
//      @Override
//      public int hashCode() {
//        return Objects.hash(lat, lon);
//      }

        addOverrideAnnotation(out, imports, indent);
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

    private static void writeToStringMethod(PrintWriter out, Imports imports, Indent indent, Cls cls) {
        addOverrideAnnotation(out, imports, indent);
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

    private static void ifValidate(Cls cls, PrintWriter out, Indent indent, Imports imports, Names names,
            Consumer<PrintWriter> r) {
        ByteArrayPrintWriter b = ByteArrayPrintWriter.create();
        indent.right();
        r.accept(b);
        indent.left();
        b.close();
        String text = b.text();
        if (text.isEmpty()) {
            return;
        } else {
            out.format("%sif (%s.config().validateInConstructor().test(%s.class)) {\n", indent,
                    imports.add(names.globalsFullClassName()), cls.simpleName());
            out.print(text);
            out.format("%s}\n", indent);
        }
    }

    private static void assignField(PrintWriter out, Indent indent, Cls cls, Field x) {
        out.format("%sthis.%s = %s;\n", indent, x.fieldName(cls), x.fieldName(cls));
    }

    private static void writeGetters(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces) {
        Set<Cls> interfaces = Util.orElse(fullClassNameInterfaces.get(cls.fullClassName), Collections.emptySet());
        cls.fields.forEach(f -> {
            if (interfaces.stream().anyMatch(c -> c.discriminator.propertyName.equals(f.name))) {
                addOverrideAnnotation(out, imports, indent);
            } else {
                out.println();
            }
            out.format("%spublic %s %s() {\n", indent, f.resolvedType(imports), f.fieldName(cls));
            indent.right();
            if (!f.isOctets() && !f.required) {
                out.format("%sreturn %s.ofNullable(%s);\n", indent, imports.add(Optional.class), f.fieldName(cls));
            } else if (f.isOctets()) {
                out.format("%sreturn %s.decodeOctets(%s);\n", indent, imports.add(Util.class), f.fieldName(cls));
            } else {
                out.format("%sreturn %s;\n", indent, f.fieldName(cls));
            }
            closeParen(out, indent);
        });
    }

    private static void writeMemberClasses(PrintWriter out, Imports imports, Indent indent, Cls cls,
            Map<String, Set<Cls>> fullClassNameInterfaces, Names names) {
        cls.classes.forEach(c -> writeClass(out, imports, indent, c, fullClassNameInterfaces, names));
    }

}