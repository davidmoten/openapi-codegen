package org.davidmoten.oa3.codegen.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;

public class BuilderWriter {

    public static final class Field {
        private final String fieldName;
        private final String fullClassName;
        private final boolean required;
        private final boolean isArray;

        public Field(String fieldName, String fullClassName, boolean required, boolean isArray) {
            this.fieldName = fieldName;
            this.fullClassName = fullClassName;
            this.required = required;
            this.isArray = isArray;
        }

    }

    public static void write(CodePrintWriter out, List<Field> fields, String importedBuiltType, Imports imports) {
        if (fields.isEmpty()) {
            return;
        }
        List<Field> sortedFields = new ArrayList<>(fields);
        // sort so required are first
        sortedFields.sort((a, b) -> Boolean.compare(b.required, a.required));

        String builderName = "Builder";
        boolean passBuilderIntoConstructor = false;
        boolean previousWasRequired = true;
        boolean inFirstBuilder = true;
        Field last = sortedFields.get(sortedFields.size() - 1);
        for (Field f : sortedFields) {
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
                    inFirstBuilder = false;
                } else {
                    boolean first = true;
                    for (Field fld : sortedFields) {
                        if (first) {
                            out.println();
                            first = false;
                        }
                        if (fld.required) {
                            if (fld.isArray) {
                                out.line("private %s<%s> %s;", imports.add(List.class), imports.add(fld.fullClassName),
                                        fld.fieldName);
                            } else {
                                out.line("private %s %s;", imports.add(fld.fullClassName), fld.fieldName);
                            }
                        } else {
                            out.line("private %s %s = %s.empty();", enhancedImportedType(fld, imports), fld.fieldName,
                                    imports.add(Optional.class));
                        }
                    }
                    out.newLine();
                    out.line("%s() {", builderName);
                    out.right();
                    out.closeParen();
                }
            }
            String builderField = inFirstBuilder ? "" : ".b";
            out.newLine();
            out.line("public %s %s(%s %s) {", nextBuilderName, f.fieldName, baseImportedType(f, imports), f.fieldName);
            out.right();
            if (f.required) {
                out.line("this%s.%s = %s;", builderField, f.fieldName, f.fieldName);
            } else {
                out.line("this%s.%s = %s.of(%s);", builderField, f.fieldName, imports.add(Optional.class), f.fieldName);
            }
            if (f.required) {
                out.line("return new %s(this%s);", nextBuilderName, builderField);
            } else {
                out.line("return this;");
            }
            out.closeParen();

            if (!f.required) {
                out.line("// first");
                out.newLine();
                out.line("public %s %s(%s %s) {", nextBuilderName, f.fieldName, enhancedImportedType(f, imports),
                        f.fieldName);
                out.right();
                out.line("this%s.%s = %s;", builderField, f.fieldName, f.fieldName);
                out.line("return this;");
                out.closeParen();
                if (f == last) {
                    writeBuildMethod(out, fields, importedBuiltType, builderField);
                }
            }
            if (f.required || f == last) {
                out.closeParen();
            }
            if (f == last && f.required) {
                out.newLine();
                out.line("// second");
                out.line("public static final class %s {", nextBuilderName);
                out.right();
                out.newLine();
                out.line("private final Builder b;");
                out.newLine();
                out.line("%s(%s b) {", nextBuilderName, "Builder");
                out.right();
                out.line("this.b = b;");
                out.closeParen();
                writeBuildMethod(out, fields, importedBuiltType, ".b");
                out.closeParen();
            }
            passBuilderIntoConstructor = f.required;
            builderName = nextBuilderName;
            previousWasRequired = f.required;
            out.flush();
        }

    }

    private static void writeBuildMethod(CodePrintWriter out, List<Field> fields, String importedBuiltType,
            String builderField) {
        out.newLine();
        out.line("public %s build() {", importedBuiltType);
        out.right();
        String params = fields.stream().map(x -> String.format("this%s.%s", builderField, x.fieldName))
                .collect(Collectors.joining(", "));
        out.line("return new %s(%s);", importedBuiltType, params);
        out.closeParen();
    }

    private static String baseImportedType(Field f, Imports imports) {
        if (f.isArray) {
            return String.format("%s<%s>", imports.add(List.class), imports.add(f.fullClassName));
        } else {
            return imports.add(f.fullClassName);
        }
    }

    private static String enhancedImportedType(Field f, Imports imports) {
        if (f.isArray) {
            if (f.required) {
                return String.format("%s<%s>", imports.add(List.class), imports.add(f.fullClassName));
            } else {
                return String.format("%s<%s<%s>>", imports.add(Optional.class), imports.add(List.class),
                        imports.add(f.fullClassName));
            }
        }
        if (f.required) {
            return imports.add(f.fullClassName);
        } else {
            return String.format("%s<%s>", imports.add(Optional.class), imports.add(f.fullClassName));
        }
    }
}
