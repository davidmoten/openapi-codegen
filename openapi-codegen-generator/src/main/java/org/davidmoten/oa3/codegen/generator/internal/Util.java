package org.davidmoten.oa3.codegen.generator.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;
import java.util.Set;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;

public final class Util {

    public static <T> T orElse(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static Set<String> PRIMITIVE_CLASS_NAMES = Sets.newHashSet("int", "double", "float", "long", "boolean",
            "byte", "short");

    public static boolean isPrimitiveFullClassName(String className) {
        return PRIMITIVE_CLASS_NAMES.contains(className);
    }

    public static boolean isPrimitive(Schema<?> schema) {
        String type = schema.getType();
        return type != null && !"array".equals(type) && !"object".equals(type);
    }

    public static boolean isEnum(Schema<?> schema) {
        return schema.getEnum() != null && !schema.getEnum().isEmpty();
    }

    public static boolean isRef(Schema<?> schema) {
        return schema.get$ref() != null;
    }

    public static boolean isObject(Schema<?> schema) {
        return schema.getType() == null && schema.getProperties() != null || "object".equals(schema.getType());
    }

    public static boolean isArray(Schema<?> schema) {
        return schema instanceof ArraySchema;
    }

    public static boolean isOneOf(Schema<?> schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        ComposedSchema sch = (ComposedSchema) schema;
        return sch.getOneOf() != null && !sch.getOneOf().isEmpty();
    }

    public static boolean isAnyOf(Schema<?> schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        ComposedSchema sch = (ComposedSchema) schema;
        return sch.getAnyOf() != null && !sch.getAnyOf().isEmpty();
    }

    public static boolean isAllOf(Schema<?> schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        ComposedSchema sch = (ComposedSchema) schema;
        return sch.getAllOf() != null && !sch.getAllOf().isEmpty();
    }

    public static boolean isMap(Schema<?> schema) {
        return schema instanceof MapSchema;
    }
    
    public static Class<?> toClass(String type, String format, boolean mapIntegerToBigInteger) {
        Preconditions.checkNotNull(type);
        if ("string".equals(type)) {
            if ("date-time".equals(format)) {
                return OffsetDateTime.class;
            } else if ("date".equals(format)) {
                return LocalDate.class;
            } else if ("time".equals(format)) {
                return OffsetTime.class;
            } else if ("byte".equals(format)) {
                return byte[].class;
            } else if ("binary".equals(format)) {
                return byte[].class;
            } else {
                return String.class;
            }
        } else if ("boolean".equals(type)) {
            return Boolean.class;
        } else if ("integer".equals(type)) {
            if ("int32".equals(format)) {
                return Integer.class;
            } else if ("int64".equals(format)) {
                return Long.class;
            } else {
                return mapIntegerToBigInteger ? BigInteger.class : Long.class;
            }
        } else if ("number".equals(type)) {
            if ("float".equals(format)) {
                return Float.class;
            } else if ("double".equals(format)) {
                return Double.class;
            } else {
                return BigDecimal.class;
            }
        } else if ("array".equals(type)) {
            return List.class;
        } else if ("object".equals(type)) {
            return Object.class;
        } else {
            throw new RuntimeException("unexpected type and format: " + type + ", " + format);
        }
    }
    
}
