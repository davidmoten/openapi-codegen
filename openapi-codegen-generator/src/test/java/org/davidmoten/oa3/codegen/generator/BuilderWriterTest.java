package org.davidmoten.oa3.codegen.generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.davidmoten.oa3.codegen.generator.BuilderWriter.Field;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.junit.jupiter.api.Test;

public class BuilderWriterTest {

    @Test
    public void test() {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("name", "String", true));
        fields.add(new Field("mobile", "String", false));
        try (PrintWriter writer = new PrintWriter(System.out)) {
            BuilderWriter.write(writer, new Indent(), fields, "Thing");
        }
    }

}
