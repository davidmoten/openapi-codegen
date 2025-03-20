package org.davidmoten.oa3.codegen.generator.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.Generator.MapType;
import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
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
        private final Optional<Function<String, String>> valueExpressionFactory;

        public Field(String fieldName, String fullClassName, boolean required, boolean isArray,
                Optional<MapType> mapType, boolean nullable, Optional<Function<String,String>> valueExpressionFactory) {
            this.fieldName = fieldName;
            this.fullClassName = fullClassName;
            this.required = required;
            this.isArray = isArray;
            this.mapType = mapType;
            this.nullable = nullable;
            this.valueExpressionFactory = valueExpressionFactory;
        }

        public boolean mandatory() {
            return required && !nullable;
        }

        public boolean isMapType(MapType mt) {
            return mapType.orElse(null) == mt;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Field [fieldName=").append(fieldName).append(", fullClassName=").append(fullClassName)
                    .append(", required=").append(required).append(", isArray=").append(isArray).append(", mapType=")
                    .append(mapType).append(", nullable=").append(nullable) //
                    .append(", valueExpressionFactory=" + valueExpressionFactory.map(x -> "present").orElse("")).append("]");
            return builder.toString();
        }
        
    }

    public static void write(CodePrintWriter out, List<Field> fields, String importedBuiltType) {
        write(out, fields, importedBuiltType, false);
    }
    
    public static void write(CodePrintWriter out, List<Field> fields, String importedBuiltType, boolean useOf) {
        if (fields.isEmpty()) {
            return;
        }
        List<Field> settableFields = fields.stream().filter(x -> !x.valueExpressionFactory.isPresent())
                .collect(Collectors.toList());
        if (settableFields.size() == 1) {
            writeSingleValueStaticFactoryMethods(out, fields, settableFields.get(0), importedBuiltType);
            return;
        }
        if (settableFields.isEmpty()) {
            writeSingletonInstanceGetter(out, fields, importedBuiltType);
            return;
        }
        List<Field> sortedFields = new ArrayList<>(settableFields);
        // sort so required are first
        sortedFields.sort((a, b) -> Boolean.compare(b.mandatory(), a.mandatory()));

        String builderName = "Builder";
        boolean passBuilderIntoConstructor = false;
        boolean previousWasMandatory = true;
        boolean inFirstBuilder = true;
        Field last = sortedFields.get(sortedFields.size() - 1);
        Optional<String> firstFieldStaticMethod = Optional.empty();
        for (Field f : sortedFields) {
            final String nextBuilderName;
            if (f.mandatory()) {
                nextBuilderName = Names.removeLowerCaseVowels("BuilderWith" + Names.upperFirst(f.fieldName), 80);
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
                    // we are in the first Builder
                    // write the field declarations
                    boolean first = true;
                    for (Field fld : sortedFields) {
                        if (first) {
                            out.println();
                            first = false;
                        }
                        if (fld.mapType.isPresent()) {
                            if (fld.isArray) {
                                if (fld.nullable) {
                                    out.line("private %s<%s<%s<%s, %s> %s = new %s<>();", List.class,
                                            JsonNullable.class, Map.class, String.class, out.add(fld.fullClassName),
                                            fld.fieldName, ArrayList.class);
                                } else {
                                    out.line("private %s<%s<%s, %s>> %s = new %s<>();", List.class, Map.class,
                                            String.class, out.add(fld.fullClassName), fld.fieldName, ArrayList.class);
                                }
                            } else if (fld.nullable) {
                                if (fld.required && fld.isMapType(MapType.FIELD)) {
                                    out.line("private %s<%s<%s, %s>> %s = %s.empty();", Optional.class, Map.class,
                                            String.class, out.add(fld.fullClassName), fld.fieldName, Optional.class);
                                } else {
                                    out.line("private %s<%s<%s, %s>> %s = %s.undefined();", JsonNullable.class,
                                            Map.class, String.class, out.add(fld.fullClassName), fld.fieldName,
                                            JsonNullable.class);
                                }
                            } else {
                                if (fld.required || fld.isMapType(MapType.ADDITIONAL_PROPERTIES)) {
                                    out.line("private %s<%s, %s> %s = new %s<>();", Map.class, String.class,
                                            out.add(fld.fullClassName), fld.fieldName, HashMap.class);
                                } else {
                                    out.line("private %s<%s<%s, %s>> %s = %s.empty();", Optional.class, Map.class,
                                            String.class, out.add(fld.fullClassName), fld.fieldName, Optional.class);
                                }
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
                if (f.isMapType(MapType.FIELD) && !f.required && !f.nullable) {
                    out.line("this%s.%s = %s;", builderField, f.fieldName, f.fieldName);
//                    out.line("this%s.%s = %s.of(%s);", builderField, f.fieldName, Optional.class, f.fieldName);
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
                    writeBuildMethod(out, fields, importedBuiltType, builderField, useOf);
                }
            }
            if (!firstFieldStaticMethod.isPresent()) {
                // only do this if field is mandatory
                if (f.mandatory()) {
                    Indent indent = out.indent().copy().left();
                    String s = String.format("%spublic static %s %s(%s %s) {\n", indent, nextBuilderName, f.fieldName,
                            baseImportedType(f, out.imports()), f.fieldName) //
                            + String.format("%sreturn builder().%s(%s);\n", indent.right(), f.fieldName, f.fieldName) //
                            + String.format("%s}\n", indent.left());
                    firstFieldStaticMethod = Optional.of(s);
                } else {
                    firstFieldStaticMethod = Optional.of("");
                }
            }
            if (f.mapType.isPresent() && f == last) {
                writeBuildMethod(out, fields, importedBuiltType, builderField, useOf);
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
                writeBuildMethod(out, fields, importedBuiltType, ".b", useOf);
                out.closeParen();
            }
            passBuilderIntoConstructor = f.mandatory();
            builderName = nextBuilderName;
            previousWasMandatory = f.mandatory();
            out.flush();
        }
        if (firstFieldStaticMethod.isPresent() && !firstFieldStaticMethod.get().isEmpty()) {
            out.println();
            out.print(firstFieldStaticMethod.get());
        }
    }

    private static void writeSingleValueStaticFactoryMethods(CodePrintWriter out, List<Field> fields, Field field, String importedBuiltType) {
        final String methodName;
        if ("value".equals(field.fieldName)) {
            methodName = "of";
        } else {
            methodName = field.fieldName;
        }
        
        out.println();
        out.line("public static %s %s(%s %s) {", importedBuiltType, methodName, 
                enhancedImportedType(field, out.imports()), field.fieldName);
        
        String params = fields //
                .stream() //
                .map(x -> fieldExpression(x)) //
                .collect(Collectors.joining(", "));
        out.line("return new %s(%s);", importedBuiltType, params);
        out.closeParen();

        if (!field.mandatory() //
                && !field.mapType.isPresent() && !field.isArray) {
            out.println();
            out.line("public static %s %s(%s %s) {", importedBuiltType, field.fieldName,
                    baseImportedType(field, out.imports()), field.fieldName);
            Class<?> c = field.nullable && !field.required ? JsonNullable.class : Optional.class;
            String params2 = fields //
                    .stream() //
                    .map(x -> {
                        if (x.fieldName.equals(field.fieldName)) {
                            return String.format("%s.of(%s)", out.add(c), x.fieldName);
                        } else {
                            return fieldExpression(x);
                        }
                    }) //
                    .collect(Collectors.joining(", "));
            out.line("return new %s(%s);", importedBuiltType, params2);
            out.closeParen();
        }
    }
    
    private static void writeSingletonInstanceGetter(CodePrintWriter out, List<Field> fields, String importedBuiltType) {
        out.println();
        out.line("private static final %s INSTANCE = ", importedBuiltType);
        String params = fields //
                .stream() //
                .map(x -> fieldExpression(x))
                .collect(Collectors.joining(", "));
        out.right().right();
        out.line("new %s(%s);", importedBuiltType, params);
        out.left().left();
        out.println();
        out.line("public static %s instance() {", importedBuiltType);
        out.line("return INSTANCE;");
        out.closeParen();
    }

    private static void writeBuildMethod(CodePrintWriter out, List<Field> fields, String importedBuiltType,
            String builderField, boolean useOf) {
        out.println();
        out.line("public %s build() {", importedBuiltType);
        String params = fields //
                .stream() //
                .map(x -> fieldExpression(builderField, x))
                .collect(Collectors.joining(", "));
        if (useOf) {
            out.line("return %s.of(%s);", importedBuiltType, params);
        } else {
            out.line("return new %s(%s);", importedBuiltType, params);
        }
        out.closeParen();
    }

    private static String fieldExpression(Field x) {
        return x.valueExpressionFactory.map(factory -> factory.apply(x.fieldName)).orElse(x.fieldName);
    }
    
    private static String fieldExpression(String builderField, Field x) {
        String fieldExpression = String.format("this%s.%s", builderField, x.fieldName);
        return x.valueExpressionFactory.map(factory -> factory.apply(fieldExpression)).orElse(fieldExpression);
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
                return listJsonNullableMap(f, imports);
            } else {
                return listMapType(f, imports);
            }
        } else if (f.isMapType(MapType.ADDITIONAL_PROPERTIES)) {
            return mapType(f, imports);
        } else if (f.nullable) {
            if (f.required) {
                return optionalMapType(f, imports);
            } else {
                return jsonNullableMap(f, imports);
            }
        } else {
            return mapType(f, imports);
        }
    }

    private static String listJsonNullableMap(Field f, Imports imports) {
        return String.format("%s<%s<%s<%s, %s>>>", imports.add(List.class), imports.add(JsonNullable.class),
                imports.add(Map.class), imports.add(String.class), imports.add(f.fullClassName));
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
        if (f.isMapType(MapType.ADDITIONAL_PROPERTIES)) {
            if (f.nullable) {
                return String.format("%s<%s, %s<%s>>", imports.add(Map.class), imports.add(String.class),
                        imports.add(JsonNullable.class), imports.add(f.fullClassName));
            } else {
                return String.format("%s<%s, %s>", imports.add(Map.class), imports.add(String.class),
                        imports.add(f.fullClassName));
            }
        } else if (f.nullable) {
            if (f.required && f.isMapType(MapType.FIELD)) {
                return optionalMapType(f, imports);
            } else {
                return jsonNullableMap(f, imports);
            }
        } else {
            if (f.required) {
                return String.format("%s<%s, %s>", imports.add(Map.class), imports.add(String.class),
                        imports.add(f.fullClassName));
            } else {
                return String.format("%s<%s<%s, %s>>", imports.add(Optional.class), imports.add(Map.class),
                        imports.add(String.class), imports.add(f.fullClassName));
            }
        }
    }

    private static String optionalMapType(Field f, Imports imports) {
        return String.format("%s<%s<%s, %s>>", imports.add(Optional.class), imports.add(Map.class),
                imports.add(String.class), imports.add(f.fullClassName));
    }

    private static String enhancedImportedType(Field f, Imports imports) {
        if (f.mapType.isPresent()) {
            return mapImportedTypePublic(f, imports);
        } else if (f.isArray) {
            if (f.nullable) {
                return String.format("%s<%s<%s>>", imports.add(List.class), imports.add(JsonNullable.class),
                        imports.add(f.fullClassName));
            } else if (f.required) {
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
