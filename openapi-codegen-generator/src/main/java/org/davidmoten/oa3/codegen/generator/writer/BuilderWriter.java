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

        public boolean mandatory() {
            return required && !nullable;
        }

        public boolean isMapType(MapType mt) {
            return mapType.orElse(null) == mt;
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
        sortedFields.sort((a, b) -> Boolean.compare(b.mandatory(), a.mandatory()));

        String builderName = "Builder";
        boolean passBuilderIntoConstructor = false;
        boolean previousWasMandatory = true;
        boolean inFirstBuilder = true;
        Field last = sortedFields.get(sortedFields.size() - 1);
        for (Field f : sortedFields) {
            final String nextBuilderName;
            if (f.mandatory()) {
                nextBuilderName = "BuilderWith" + Names.upperFirst(f.fieldName);
            } else {
                nextBuilderName = builderName;
            }
            if (previousWasMandatory) {
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
                        if (fld.mapType.isPresent()) {
                            // TODO support arrays of map?
                            if (fld.nullable) {
                                if (fld.required) {
                                    out.line("private %s<%s<%s, %s>> %s = %s.empty();", Optional.class, Map.class,
                                            String.class, out.add(fld.fullClassName), fld.fieldName, Optional.class);
                                } else {
                                    out.line("private %s<%s<%s, %s>> %s = %s.undefined();", JsonNullable.class,
                                            Map.class, String.class, out.add(fld.fullClassName), fld.fieldName,
                                            JsonNullable.class);
                                }
                            } else {
                                out.line("private %s<%s, %s> %s = new %s<>();", Map.class, String.class,
                                        out.add(fld.fullClassName), fld.fieldName, HashMap.class);
                            }
                        } else if (fld.nullable) {
                            if (fld.required) {
                                out.line("private %s<%s> %s = %s.empty();", Optional.class, out.add(fld.fullClassName),
                                        fld.fieldName, Optional.class);
                            } else {
                                out.line("private %s<%s> %s = %s.undefined();", JsonNullable.class,
                                        out.add(fld.fullClassName), fld.fieldName, JsonNullable.class);
                            }
                        } else if (fld.required) {
                            if (fld.isArray) {
                                out.line("private %s<%s> %s;", List.class, out.add(fld.fullClassName), fld.fieldName);
                            } else {
                                out.line("private %s %s;", out.add(Util.toPrimitive(fld.fullClassName)), fld.fieldName);
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
            if (f.mapType.isPresent()) {
                if (f.nullable) {
                    if (f.required) {
                        out.line("this%s.%s = %s.of(%s);", builderField, f.fieldName, Optional.class, f.fieldName);
                    } else {
                        out.line("this%s.%s = %s.of(%s);", builderField, f.fieldName, JsonNullable.class, f.fieldName);
                    }
                } else {
                    out.line("this%s.%s = %s;", builderField, f.fieldName, f.fieldName);
                }
            } else if (f.mandatory()) {
                out.line("this%s.%s = %s;", builderField, f.fieldName, f.fieldName);
            } else if (f.nullable && !f.required) {
                out.line("this%s.%s = %s.of(%s);", builderField, f.fieldName, JsonNullable.class, f.fieldName);
            } else {
                out.line("this%s.%s = %s.of(%s);", builderField, f.fieldName, Optional.class, f.fieldName);
            }
            if (f.mandatory()) {
                out.line("return new %s(this%s);", nextBuilderName, builderField);
            } else {
                out.line("return this;");
            }
            out.closeParen();

            if (!f.mandatory() && !f.mapType.isPresent()) {
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
            if (f.mandatory() || f == last) {
                out.closeParen();
            }
            if (f == last && f.mandatory()) {
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
            passBuilderIntoConstructor = f.mandatory();
            builderName = nextBuilderName;
            previousWasMandatory = f.mandatory();
            out.flush();
        }

    }

    private static void writeSimple(CodePrintWriter out, Field field, String importedBuiltType) {
        out.println();
        out.line("public static %s %s(%s %s) {", importedBuiltType, field.fieldName,
                enhancedImportedType(field, out.imports()), field.fieldName);
        out.line("return new %s(%s);", importedBuiltType, field.fieldName);
        out.closeParen();

        if (!field.mandatory() && !(field.isMapType(MapType.ADDITIONAL_PROPERTIES)
                || field.mapType.isPresent() && field.nullable && !field.required)) {
            out.println();
            out.line("public static %s %s(%s %s) {", importedBuiltType, field.fieldName,
                    baseImportedType(field, out.imports()), field.fieldName);
            Class<?> c = field.nullable && !field.required ? JsonNullable.class : Optional.class;
            out.line("return new %s(%s.of(%s));", importedBuiltType, c, field.fieldName);
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
            return mapImportedTypeNonOptional(f, imports);
        } else if (f.isArray) {
            return String.format("%s<%s>", imports.add(List.class), imports.add(f.fullClassName));
        } else {
            return imports.add(Util.toPrimitive(f.fullClassName));
        }
    }

    private static String mapImportedTypeNonOptional(Field f, Imports imports) {
        if (f.isArray) {
            if (f.nullable) {
                if (f.required) {
                    return listMapType(f, imports);
                } else {
                    return jsonNullableListMapType(f, imports);
                }
            } else {
                return listMapType(f, imports);
            }
        } else if (f.nullable && !f.isMapType(MapType.ADDITIONAL_PROPERTIES)) {
            if (f.required) {
                return mapType(f, imports);
            } else {
                return jsonNullableMap(f, imports);
            }
        } else {
            return mapType(f, imports);
        }
    }

    private static String mapImportedTypePublic(Field f, Imports imports) {
        if (f.isArray) {
            if (f.nullable) {
                if (f.required) {
                    return optionalListMapType(f, imports);
                } else {
                    return jsonNullableListMapType(f, imports);
                }
            } else {
                return listMapType(f, imports);
            }
        } else if (f.nullable && !f.isMapType(MapType.ADDITIONAL_PROPERTIES)) {
            if (f.required) {
                return optionalMapType(f, imports);
            } else {
                return jsonNullableMap(f, imports);
            }
        } else {
            return mapType(f, imports);
        }
    }

    private static String jsonNullableListMapType(Field f, Imports imports) {
        return String.format("%s<%s<%s<%s, %s>>>", imports.add(JsonNullable.class), imports.add(List.class),
                imports.add(Map.class), imports.add(String.class), imports.add(f.fullClassName));
    }

    private static String listMapType(Field f, Imports imports) {
        return String.format("%s<%s<%s, %s>>", imports.add(List.class), imports.add(Map.class),
                imports.add(String.class), imports.add(f.fullClassName));
    }

    private static String jsonNullableMap(Field f, Imports imports) {
        return String.format("%s<%s<%s, %s>>", imports.add(JsonNullable.class), imports.add(Map.class),
                imports.add(String.class), imports.add(f.fullClassName));
    }

    private static String mapType(Field f, Imports imports) {
        if (f.isMapType(MapType.ADDITIONAL_PROPERTIES) && f.nullable) {
            return String.format("%s<%s, %s<%s>>", imports.add(Map.class), imports.add(String.class),
                    imports.add(JsonNullable.class), imports.add(f.fullClassName));
        } else {
            return String.format("%s<%s, %s>", imports.add(Map.class), imports.add(String.class),
                    imports.add(f.fullClassName));
        }
    }

    private static String optionalMapType(Field f, Imports imports) {
        return String.format("%s<%s<%s, %s>>", imports.add(Optional.class), imports.add(Map.class),
                imports.add(String.class), imports.add(f.fullClassName));
    }

    private static String optionalListMapType(Field f, Imports imports) {
        return String.format("%s<%s<%s<%s, %s>>>", imports.add(Optional.class), imports.add(List.class),
                imports.add(Map.class), imports.add(String.class), imports.add(f.fullClassName));
    }

    private static String enhancedImportedType(Field f, Imports imports) {
        if (f.mapType.isPresent()) {
            return mapImportedTypePublic(f, imports);
        } else if (f.isArray) {
            if (f.required) {
                return String.format("%s<%s>", imports.add(List.class), imports.add(f.fullClassName));
            } else {
                return String.format("%s<%s<%s>>", imports.add(Optional.class), imports.add(List.class),
                        imports.add(f.fullClassName));
            }
        } else if (f.required && !f.nullable) {
            return imports.add(Util.toPrimitive(f.fullClassName));
        } else if (f.nullable && !f.required) {
            return String.format("%s<%s>", imports.add(JsonNullable.class), imports.add(f.fullClassName));
        } else
            return String.format("%s<%s>", imports.add(Optional.class), imports.add(f.fullClassName));
    }
}
