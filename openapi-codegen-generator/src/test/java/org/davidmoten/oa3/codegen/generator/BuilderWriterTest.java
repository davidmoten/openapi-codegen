package org.davidmoten.oa3.codegen.generator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.davidmoten.oa3.codegen.generator.BuilderWriter.Field;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.junit.jupiter.api.Test;

public class BuilderWriterTest {

    @Test
    public void testMixed() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", "String", true, false));
        fields.add(new Field("lastName", "String", true, true));
        fields.add(new Field("mobile", "String", false, false));
        fields.add(new Field("age", "Integer", false, false));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/Mixed.java"))) {
            BuilderWriter.write(writer, fields, "Thing", createImports());
        }
    }

    @Test
    public void testJustOptional() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", "String", false, false));
        fields.add(new Field("lastName", "String", false, true));
        fields.add(new Field("mobile", "String", false, false));
        fields.add(new Field("age", "Integer", false, false));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/Optionals.java"))) {
            BuilderWriter.write(writer, fields, "Thing", createImports());
        }
    }

    private static Imports createImports() {
        return new Imports(BuilderWriterTest.class.getCanonicalName());
    }

    @Test
    public void testJustRequired() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", "String", true, false));
        fields.add(new Field("lastName", "String", true, true));
        fields.add(new Field("mobile", "String", true, false));
        fields.add(new Field("age", "Integer", true, false));
        try (CodePrintWriter writer = new CodePrintWriter(new FileOutputStream("target/Required.java"))) {
            BuilderWriter.write(writer, fields, "Thing", createImports());
        }
    }

}
