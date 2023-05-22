package org.davidmoten.oa3.codegen.generator.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.Generator.MapType;
import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.runtime.MapBuilder;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.openapitools.jackson.nullable.JsonNullable;

public class BuilderWriter {

    public static final class Field {
        private final String fieldName;
        private final String fullClassName;
        private final boolean required;
        private final boolean isArray;
        private final Optional<MapType> mapType;
        private final boolean nullable;

        public Field(String fieldName, String fullClassName, boolean required, boolean isArray,
                Optional<MapType> mapType, boolean nullable) {
            this.fieldName = fieldName;
            this.fullClassName = fullClassName;
            this.required = required;
            this.isArray = isArray;
            this.mapType = mapType;
            this.nullable = nullable;
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
        sortedFields.sort((a, b) -> Boolean.compare(b.required || b.nullable, a.required || a.nullable));

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
                        if (fld.nullable) {
                            if (fld.required) {
                                out.line("private %s<%s> %s;", Optional.class, out.add(fld.fullClassName),
                                        fld.fieldName);
                            } else {
                                out.line("private %s<%s> %s;", JsonNullable.class, out.add(fld.fullClassName),
                                        fld.fieldName);
                            }
                        } else if (fld.mapType.isPresent()) {
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
            if (f.mapType.isPresent() && f.mapType.get() == MapType.ADDITIONAL_PROPERTIES) {
                out.line("public %s<%s, %s> addTo%s(%s key, %s value) {", MapBuilder.class, out.add(f.fullClassName),
                        nextBuilderName, Names.upperFirst(f.fieldName), String.class, out.add(f.fullClassName));
                out.line("%s.checkNotNull(value, \"value\");", Preconditions.class);
                out.line("return new %s<%s, %s>(this, x -> this%s.%s = x).add(key, value);", MapBuilder.class,
                        out.add(f.fullClassName), nextBuilderName, builderField, f.fieldName);
                out.closeParen();
                out.println();
                out.line("public %s<%s, %s> addAllTo%s(%s<%s, %s> map) {", MapBuilder.class, out.add(f.fullClassName),
                        nextBuilderName, Names.upperFirst(f.fieldName), Map.class, String.class,
                        out.add(f.fullClassName));
                out.line("return new %s<%s, %s>(this, x -> this%s.%s = x).addAll(map);", MapBuilder.class,
                        out.add(f.fullClassName), nextBuilderName, builderField, f.fieldName);
                out.closeParen();
                out.println();
            }
            out.line("public %s %s(%s %s) {", nextBuilderName, f.fieldName, baseImportedType(f, out.imports()),
                    f.fieldName);
            if (f.nullable) {
                if (f.required) {
                    out.line("this%s.%s = %s.of(%s);", builderField, f.fieldName, Optional.class, f.fieldName);
                } else {
                    out.line("this%s.%s = %s;", builderField, f.fieldName, f.fieldName);
                }
            } else if (f.required || f.mapType.isPresent()) {
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
            if (f.required && f.nullable) {
                out.println();
                out.line("public %s %sNull() {", nextBuilderName, f.fieldName);
                out.line("this%s.%s = %s.empty();", builderField, f.fieldName, Optional.class);
                out.line("return new %s(this%s);", nextBuilderName, builderField);
            }

            if (!f.required && !f.nullable && !f.mapType.isPresent()) {
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
            if (f.mapType.isPresent() && f == last) {
                writeBuildMethod(out, fields, importedBuiltType, builderField);
            }
            if (f.required || f.nullable || f == last) {
                out.closeParen();
            }
            if (f == last && (f.required || f.nullable)) {
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

        if (!field.required && !field.mapType.isPresent()) {
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
        if (f.mapType.isPresent()) {
            return mapImportedType(f, imports);
        } else if (f.isArray) {
            return String.format("%s<%s>", imports.add(List.class), imports.add(f.fullClassName));
        } else if (f.nullable && !f.required) {
            return String.format("%s<%s>", imports.add(JsonNullable.class), imports.add(f.fullClassName));
        } else {
            return imports.add(Util.toPrimitive(f.fullClassName));
        }
    }

    private static String mapImportedType(Field f, Imports imports) {
        if (f.isArray) {
            return String.format("%s<%s<%s, %s>>", imports.add(List.class), imports.add(Map.class),
                    imports.add(String.class), imports.add(f.fullClassName));
        } else {
            return String.format("%s<%s, %s>", imports.add(Map.class), imports.add(String.class),
                    imports.add(f.fullClassName));
        }
    }

    private static String enhancedImportedType(Field f, Imports imports) {
        if (f.mapType.isPresent()) {
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
