package org.davidmoten.oa3.codegen.generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.internal.Indent;

public class BuilderWriter {

    public static final class Field {
        private final String fieldName;
        private final String importedType;
        private final boolean required;

        public Field(String fieldName, String importedType, boolean required) {
            this.fieldName = fieldName;
            this.importedType = importedType;
            this.required = required;
        }
    }

    public static void write(PrintWriter out, Indent indent, List<Field> fields, String importedBuiltType) {
        List<Field> list = new ArrayList<>(fields);
        // sort so required are first
        list.sort((a, b) -> Boolean.compare(b.required, a.required));
        List<Field> required = list.stream().filter(x -> x.required).collect(Collectors.toList());
        List<Field> optional = list.stream().filter(x -> !x.required).collect(Collectors.toList());

        String builderName = "Builder";
        boolean passBuilderIntoConstructor = false;
        boolean previousWasOptional = false;
        for (Field f : list) {
            final String nextBuilderName;
            if (f.required) {
                nextBuilderName = "BuilderWith"+ Names.upperFirst(f.fieldName);
            } else {
                nextBuilderName = builderName;
            }
            if (!previousWasOptional) {
                out.format("\n%spublic static final class %s {\n", indent, builderName);
                indent.right();
                if (passBuilderIntoConstructor) {
                    out.format("\n%s%s(Builder b) {\n", indent, builderName);
                    indent.right();
                    out.format("%sthis.b = b;\n", indent);
                    indent.left();
                    out.format("%s}\n", indent);
                } else {
                    out.format("\n%s%s(Builder b) {\n", indent, builderName);
                    out.format("%s}\n", indent);
                }
                indent.left();
                out.format("%s}\n", indent);
            }
            passBuilderIntoConstructor = f.required;
            builderName = nextBuilderName ;
        }

    }
}
