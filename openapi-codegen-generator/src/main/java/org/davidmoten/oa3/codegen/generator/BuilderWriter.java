package org.davidmoten.oa3.codegen.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;

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

    public static void write(CodePrintWriter out, List<Field> fields, String importedBuiltType,
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
                    out.newLine();
                    out.line("public static %s builder() {", builderName);
                    out.right();
                    out.line("return new %s();", builderName);
                    out.closeParen();
                }
                out.newLine();
                out.line("public static final class %s {", builderName);
                out.right();
                if (passBuilderIntoConstructor) {
                    out.newLine();
                    out.line("private final Builder b;");
                    out.newLine();
                    out.line("%s(Builder b) {", builderName);
                    out.right();
                    out.line("this.b = b;");
                    out.closeParen();
                } else {
                    boolean first = true;
                    for (Field fld : list) {
                        if (first) {
                            out.println();
                            first = false;
                        }
                        if (fld.required) {
                            out.line("private %s %s;", fld.importedType, fld.fieldName);
                        } else {
                            out.line("private %s %s = %s.empty();", enhancedImportedType(fld, importedOptionalType),
                                    fld.fieldName, importedOptionalType.get());
                        }
                    }
                    out.newLine();
                    out.line("%s() {", builderName);
                    out.right();
                    out.closeParen();
                }
            }
            out.newLine();
            out.line("public %s %s(%s %s) {", nextBuilderName, f.fieldName, f.importedType, f.fieldName);
            out.right();
            if (f.required) {
                out.line("this.b.%s = %s;", f.fieldName, f.fieldName);
            } else {
                out.line("this.b.%s = %s.of(%s);", f.fieldName, importedOptionalType.get(), f.fieldName);
            }
            if (f.required) {
                out.line("return new %s(this.b);", nextBuilderName);
            } else {
                out.line("%sreturn this;", nextBuilderName);
            }
            out.closeParen();

            if (!f.required) {
                out.newLine();
                out.line("public %s %s(%s %s) {", nextBuilderName, f.fieldName,
                        enhancedImportedType(f, importedOptionalType), f.fieldName);
                out.right();
                out.line("this.b.%s = %s;", f.fieldName, f.fieldName);
                out.line("return this;");
                out.closeParen();
            }
            if (f == last && !f.required) {
                writeBuildMethod(out, fields, importedBuiltType);
            }
            if (f.required || f == last) {
                out.closeParen();
            }
            if (f == last && f.required) {
                out.newLine();
                out.line("public static final class %s {", nextBuilderName);
                out.right();
                out.newLine();
                out.line("private final Builder b;");
                out.newLine();
                out.line("%s(%s b) {", nextBuilderName, "Builder");
                out.right();
                out.line("this.b = b;");
                out.closeParen();
                writeBuildMethod(out, fields, importedBuiltType);
                out.closeParen();
            }
            passBuilderIntoConstructor = f.required;
            builderName = nextBuilderName;
            previousWasRequired = f.required;
            out.flush();
        }

    }

    private static void writeBuildMethod(CodePrintWriter out, List<Field> fields, String importedBuiltType) {
        out.newLine();
        out.line("public %s build() {", importedBuiltType);
        out.right();
        String params = fields.stream().map(x -> "this.b." + x.fieldName).collect(Collectors.joining(", "));
        out.line("return new %s(%s);", importedBuiltType, params);
        out.closeParen();
    }

    private static String enhancedImportedType(Field f, Optional<String> importedOptionalType) {
        if (f.required) {
            return f.importedType;
        } else {
            return String.format("%s<%s>", importedOptionalType.get(), f.importedType);
        }
    }
}
