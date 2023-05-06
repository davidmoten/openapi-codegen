package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.davidmoten.oa3.codegen.generator.BuilderWriter.Field;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.junit.jupiter.api.Test;

public class BuilderWriterTest {

    @Test
    public void testMixed() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", "String", true));
        fields.add(new Field("lastName", "String", true));
        fields.add(new Field("mobile", "String", false));
        fields.add(new Field("age", "Integer", false));
        try (PrintWriter writer = new PrintWriter(new File("target/Mixed.java"))) {
            BuilderWriter.write(writer, new Indent().right(), fields, "Thing", Optional.of("Optional"));
        }
    }

    @Test
    public void testJustOptional() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", "String", false));
        fields.add(new Field("lastName", "String", false));
        fields.add(new Field("mobile", "String", false));
        fields.add(new Field("age", "Integer", false));
        try (PrintWriter writer = new PrintWriter(new File("target/Optionals.java"))) {
            BuilderWriter.write(writer, new Indent().right(), fields, "Thing", Optional.of("Optional"));
        }
    }

    @Test
    public void testJustRequired() throws FileNotFoundException {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("firstName", "String", true));
        fields.add(new Field("lastName", "String", true));
        fields.add(new Field("mobile", "String", true));
        fields.add(new Field("age", "Integer", true));
        try (PrintWriter writer = new PrintWriter(new File("target/Required.java"))) {
            BuilderWriter.write(writer, new Indent().right(), fields, "Thing", Optional.of("Optional"));
        }
    }

}
