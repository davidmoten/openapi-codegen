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

        String builderName = "Builder";
        boolean passBuilderIntoConstructor = false;
        boolean previousWasRequired = true;
        for (Field f : list) {
            final String nextBuilderName;
            if (f.required) {
                nextBuilderName = "BuilderWith" + Names.upperFirst(f.fieldName);
            } else {
                nextBuilderName = builderName;
            }
            if (previousWasRequired) {
                out.format("\n%spublic static final class %s {\n", indent, builderName);
                indent.right();
                if (passBuilderIntoConstructor) {
                    out.format("\n%s%s(Builder b) {\n", indent, builderName);
                    indent.right();
                    out.format("%sthis.b = b;\n", indent);
                    indent.left();
                    out.format("%s}\n", indent);
                } else {
                    boolean first = true;
                    for (Field fld : list) {
                        if (first) {
                            out.println();
                            first = false;
                        }
                        out.format("%sprivate %s %s;\n", indent, fld.importedType, fld.fieldName);
                    }
                    out.format("\n%s%s() {\n", indent, builderName);
                    out.format("%s}\n", indent);
                }
            }
            out.format("\n%spublic %s %s(%s %s) {\n", indent, nextBuilderName, f.fieldName, f.importedType,
                    f.fieldName);
            indent.right();
            out.format("%sthis.b.%s = %s;\n", indent, f.fieldName, f.fieldName);
            if (f.required) {
                out.format("%sreturn new %s(this.b);\n", indent, nextBuilderName);
            } else {
                out.format("%sreturn this;\n", indent, nextBuilderName);
            }
            indent.left();
            out.format("%s}\n", indent);

            if (previousWasRequired) {
                indent.left();
                out.format("%s}\n", indent);
            }
            passBuilderIntoConstructor = f.required;
            builderName = nextBuilderName;
            previousWasRequired = f.required;
        }

    }
}
