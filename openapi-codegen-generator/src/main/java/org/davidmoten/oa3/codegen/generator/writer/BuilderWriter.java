package org.davidmoten.oa3.codegen.generator.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Util;

public class BuilderWriter {

    public static final class Field {
        private final String fieldName;
        private final String fullClassName;
        private final boolean required;
        private final boolean isArray;
        private final boolean isMap;

        public Field(String fieldName, String fullClassName, boolean required, boolean isArray, boolean isMap) {
            this.fieldName = fieldName;
            this.fullClassName = fullClassName;
            this.required = required;
            this.isArray = isArray;
            this.isMap = isMap;
        }
    }

    public static void write(CodePrintWriter out, List<Field> fields, String importedBuiltType) {
        if (fields.isEmpty()) {
            return;
        }
        if (fields.size() == 1) {
            writeSimple(out, fields.get(0), importedBuiltType);
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
                    out.println();
                    out.line("public static %s builder() {", builderName);
                    out.line("return new %s();", builderName);
                    out.closeParen();
                }
                out.println();
                out.line("public static final class %s {", builderName);
                if (passBuilderIntoConstructor) {
                    out.println();
                    out.line("private final Builder b;");
                    out.println();
                    out.line("%s(Builder b) {", builderName);
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
                        if (fld.isMap) {
                            out.line("private %s<%s, %s> %s = new %s<>();", Map.class, String.class,
                                    out.add(fld.fullClassName), fld.fieldName, HashMap.class);
                        } else if (fld.required) {
                            if (fld.isArray) {
                                out.line("private %s<%s> %s;", List.class, out.add(fld.fullClassName), fld.fieldName);
                            } else {
                                out.line("private %s %s;", out.add(fld.fullClassName), fld.fieldName);
                            }
                        } else {
                            out.line("private %s %s = %s.empty();", enhancedImportedType(fld, out.imports()),
                                    fld.fieldName, Optional.class);
                        }
                    }
                    out.println();
                    out.line("%s() {", builderName);
                    out.closeParen();
                }
            }
            String builderField = inFirstBuilder ? "" : ".b";
            out.println();
            out.line("public %s %s(%s %s) {", nextBuilderName, f.fieldName, baseImportedType(f, out.imports()),
                    f.fieldName);
            if (f.required) {
                out.line("this%s.%s = %s;", builderField, f.fieldName, f.fieldName);
            } else {
                out.line("this%s.%s = %s.of(%s);", builderField, f.fieldName, Optional.class, f.fieldName);
            }
            if (f.required) {
                out.line("return new %s(this%s);", nextBuilderName, builderField);
            } else {
                out.line("return this;");
            }
            out.closeParen();

            if (!f.required) {
                out.println();
                out.line("public %s %s(%s %s) {", nextBuilderName, f.fieldName, enhancedImportedType(f, out.imports()),
                        f.fieldName);
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
                out.println();
                out.line("public static final class %s {", nextBuilderName);
                out.println();
                out.line("private final Builder b;");
                out.println();
                out.line("%s(%s b) {", nextBuilderName, "Builder");
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

    private static void writeSimple(CodePrintWriter out, Field field, String importedBuiltType) {
        out.println();
        out.line("public static %s %s(%s %s) {", importedBuiltType, field.fieldName,
                enhancedImportedType(field, out.imports()), field.fieldName);
        out.line("return new %s(%s);", importedBuiltType, field.fieldName);
        out.closeParen();

        if (!field.required) {
            out.println();
            out.line("public static %s %s(%s %s) {", importedBuiltType, field.fieldName,
                    baseImportedType(field, out.imports()), field.fieldName);
            out.line("return new %s(%s.of(%s));", importedBuiltType, Optional.class, field.fieldName);
            out.closeParen();
        }
    }

    private static void writeBuildMethod(CodePrintWriter out, List<Field> fields, String importedBuiltType,
            String builderField) {
        out.println();
        out.line("public %s build() {", importedBuiltType);
        String params = fields.stream().map(x -> String.format("this%s.%s", builderField, x.fieldName))
                .collect(Collectors.joining(", "));
        out.line("return new %s(%s);", importedBuiltType, params);
        out.closeParen();
    }

    private static String baseImportedType(Field f, Imports imports) {
        if (f.isMap) {
            return mapImportedType(f, imports);
        } else if (f.isArray) {
            return String.format("%s<%s>", imports.add(List.class), imports.add(f.fullClassName));
        } else {
            return imports.add(Util.toPrimitive(f.fullClassName));
        }
    }

    private static String mapImportedType(Field f, Imports imports) {
        return String.format("%s<%s, %s>", imports.add(Map.class), imports.add(String.class),
                imports.add(f.fullClassName));
    }

    private static String enhancedImportedType(Field f, Imports imports) {
        if (f.isMap) {
            return mapImportedType(f, imports);
        } else if (f.isArray) {
            if (f.required) {
                return String.format("%s<%s>", imports.add(List.class), imports.add(f.fullClassName));
            } else {
                return String.format("%s<%s<%s>>", imports.add(Optional.class), imports.add(List.class),
                        imports.add(f.fullClassName));
            }
        }
        if (f.required) {
            return imports.add(Util.toPrimitive(f.fullClassName));
        } else {
            return String.format("%s<%s>", imports.add(Optional.class), imports.add(f.fullClassName));
        }
    }
}
