package org.davidmoten.oa3.codegen.generator.writer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.writer.BuilderWriter;
import org.davidmoten.oa3.codegen.generator.writer.BuilderWriter.Field;
import org.junit.jupiter.api.Test;

public class BuilderWriterTest {

    @Test
    public void testMixed() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", String.class.getCanonicalName(), true, false));
        fields.add(new Field("lastName", String.class.getCanonicalName(), true, true));
        fields.add(new Field("mobile", String.class.getCanonicalName(), false, false));
        fields.add(new Field("age", Integer.class.getCanonicalName(), false, false));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/Mixed.java"), "Mixed")) {
            BuilderWriter.write(writer, fields, "Thing");
        }
    }

    @Test
    public void testJustOptional() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", String.class.getCanonicalName(), false, false));
        fields.add(new Field("lastName", String.class.getCanonicalName(), false, true));
        fields.add(new Field("mobile", String.class.getCanonicalName(), false, false));
        fields.add(new Field("age", Integer.class.getCanonicalName(), false, false));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/Optionals.java"), "Optionals")) {
            BuilderWriter.write(writer, fields, "Thing");
        }
    }

    @Test
    public void testJustRequired() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", String.class.getCanonicalName(), true, false));
        fields.add(new Field("lastName", String.class.getCanonicalName(), true, true));
        fields.add(new Field("mobile", String.class.getCanonicalName(), true, false));
        fields.add(new Field("age", Integer.class.getCanonicalName(), true, false));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/Required.java"), "Required")) {
            BuilderWriter.write(writer, fields, "Thing");
        }
    }

    @Test
    public void testNameValue() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("name", String.class.getCanonicalName(), false, false));
        fields.add(new Field("value", String.class.getCanonicalName(), true, false));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/NameValue.java"), "NameValue")) {
            BuilderWriter.write(writer, fields, "Thing");
        }
    }
}
