package org.davidmoten.oa3.codegen.generator.writer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.davidmoten.oa3.codegen.generator.Generator.MapType;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.writer.BuilderWriter.Field;
import org.junit.jupiter.api.Test;

public class BuilderWriterTest {

    @Test
    public void testMixed() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", String.class.getCanonicalName(), true, false, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("lastName", String.class.getCanonicalName(), true, true, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("mobile", String.class.getCanonicalName(), false, false, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("age", Integer.class.getCanonicalName(), false, false, Optional.empty(), false, Optional.empty()));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/Mixed.java"), "Mixed", x -> false)) {
            BuilderWriter.write(writer, fields, "Thing");
        }
    }

    @Test
    public void testJustOptional() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", String.class.getCanonicalName(), false, false, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("lastName", String.class.getCanonicalName(), false, true, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("mobile", String.class.getCanonicalName(), false, false, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("age", Integer.class.getCanonicalName(), false, false, Optional.empty(), false, Optional.empty()));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/Optionals.java"), "Optionals", x -> false)) {
            BuilderWriter.write(writer, fields, "Thing");
        }
    }

    @Test
    public void testJustRequired() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", String.class.getCanonicalName(), true, false, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("lastName", String.class.getCanonicalName(), true, true, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("mobile", String.class.getCanonicalName(), true, false, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("age", Integer.class.getCanonicalName(), true, false, Optional.empty(), false, Optional.empty()));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/Required.java"), "Required", x -> false)) {
            BuilderWriter.write(writer, fields, "Thing");
        }
    }

    @Test
    public void testNameValue() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("name", String.class.getCanonicalName(), false, false, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("value", String.class.getCanonicalName(), true, false, Optional.empty(), false, Optional.empty()));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/NameValue.java"), "NameValue", x -> false)) {
            BuilderWriter.write(writer, fields, "Thing");
        }
    }

    @Test
    public void testMixedWithMap() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("map", String.class.getCanonicalName(), true, false,
                Optional.of(MapType.ADDITIONAL_PROPERTIES), false, Optional.empty()));
        fields.add(new Field("firstName", String.class.getCanonicalName(), true, false, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("lastName", String.class.getCanonicalName(), true, true, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("mobile", String.class.getCanonicalName(), false, false, Optional.empty(), false, Optional.empty()));
        fields.add(new Field("age", Integer.class.getCanonicalName(), false, false, Optional.empty(), false, Optional.empty()));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/WithMap.java"), "Mixed", x -> false)) {
            BuilderWriter.write(writer, fields, "Thing");
        }
    }
}
