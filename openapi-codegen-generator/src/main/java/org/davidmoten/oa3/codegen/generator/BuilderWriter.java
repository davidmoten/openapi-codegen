package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.generator.WriterUtil.closeParen;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.internal.Indent;

import com.github.davidmoten.guavamini.Preconditions;

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

    public static void write(PrintWriter out, Indent indent, List<Field> fields, String importedBuiltType,
            Optional<String> importedOptionalType) {
        Preconditions.checkArgument(!fields.isEmpty());
        List<Field> list = new ArrayList<>(fields);
        // sort so required are first
        list.sort((a, b) -> Boolean.compare(b.required, a.required));

        String builderName = "Builder";
        boolean passBuilderIntoConstructor = false;
        boolean previousWasRequired = true;
        Field last = fields.get(fields.size() - 1);
        for (Field f : list) {
            final String nextBuilderName;
            if (f.required) {
                nextBuilderName = "BuilderWith" + Names.upperFirst(f.fieldName);
            } else {
                nextBuilderName = builderName;
            }
            if (previousWasRequired) {
                if (!passBuilderIntoConstructor) {
                    out.format("\n%spublic static %s builder() {\n", indent, builderName);
                    indent.right();
                    out.format("%sreturn new %s();\n", indent, builderName);
                    closeParen(out, indent);
                }
                out.format("\n%spublic static final class %s {\n", indent, builderName);
                indent.right();
                if (passBuilderIntoConstructor) {
                    out.format("\n%sprivate final Builder b;\n", indent);
                    out.format("\n%s%s(Builder b) {\n", indent, builderName);
                    indent.right();
                    out.format("%sthis.b = b;\n", indent);
                    closeParen(out, indent);
                } else {
                    boolean first = true;
                    for (Field fld : list) {
                        if (first) {
                            out.println();
                            first = false;
                        }
                        if (fld.required) {
                            out.format("%sprivate %s %s;\n", indent, fld.importedType, fld.fieldName);
                        } else {
                            out.format("%sprivate %s %s = %s.empty();\n", indent,
                                    enhancedImportedType(fld, importedOptionalType), fld.fieldName,
                                    importedOptionalType.get());
                        }
                    }
                    out.format("\n%s%s() {\n", indent, builderName);
                    out.format("%s}\n", indent);
                }
            }
            out.format("\n%spublic %s %s(%s %s) {\n", indent, nextBuilderName, f.fieldName, f.importedType,
                    f.fieldName);
            indent.right();
            if (f.required) {
                out.format("%sthis.b.%s = %s;\n", indent, f.fieldName, f.fieldName);
            } else {
                out.format("%sthis.b.%s = %s.of(%s);\n", indent, f.fieldName, importedOptionalType.get(), f.fieldName);
            }
            if (f.required) {
                out.format("%sreturn new %s(this.b);\n", indent, nextBuilderName);
            } else {
                out.format("%sreturn this;\n", indent, nextBuilderName);
            }
            closeParen(out, indent);

            if (!f.required) {
                out.format("\n%spublic %s %s(%s %s) {\n", indent, nextBuilderName, f.fieldName,
                        enhancedImportedType(f, importedOptionalType), f.fieldName);
                indent.right();
                out.format("%sthis.b.%s = %s;\n", indent, f.fieldName, f.fieldName);
                out.format("%sreturn this;\n", indent);
                closeParen(out, indent);

            }
            if (f == last && !f.required) {
                writeBuildMethod(out, indent, fields, importedBuiltType);
            }
            if (f.required || f == last) {
                closeParen(out, indent);
            }
            if (f == last && f.required) {
                out.format("\n%spublic static final class %s {\n", indent, nextBuilderName);
                indent.right();
                out.format("\n%sprivate final Builder b;\n", indent);
                out.format("\n%s%s(%s b) {\n", indent, nextBuilderName, "Builder");
                indent.right();
                out.format("%sthis.b = b;\n", indent);
                closeParen(out, indent);
                writeBuildMethod(out, indent, fields, importedBuiltType);
                closeParen(out, indent);
            }
            passBuilderIntoConstructor = f.required;
            builderName = nextBuilderName;
            previousWasRequired = f.required;
            out.flush();
        }

    }

    private static void writeBuildMethod(PrintWriter out, Indent indent, List<Field> fields, String importedBuiltType) {
        out.format("\n%spublic %s build() {\n", indent, importedBuiltType);
        indent.right();
        String params = fields.stream().map(x -> "this.b." + x.fieldName).collect(Collectors.joining(", "));
        out.format("%sreturn new %s(%s);\n", indent, importedBuiltType, params);
        indent.left();
        out.format("%s}\n", indent);
    }

    private static String enhancedImportedType(Field f, Optional<String> importedOptionalType) {
        if (f.required) {
            return f.importedType;
        } else {
            return String.format("%s<%s>", importedOptionalType.get(), f.importedType);
        }
    }
}
