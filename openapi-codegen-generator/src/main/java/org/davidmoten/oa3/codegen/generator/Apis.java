package org.davidmoten.oa3.codegen.generator;

import java.util.List;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Preconditions;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class Apis {

    static void visitSchemas(OpenAPI api, Visitor visitor) {
        api //
                .getComponents() //
                .getSchemas() //
                .entrySet() //
                .stream() //
                .forEach(x -> visitSchemas(x.getKey(), x.getValue(), visitor));
    }

    static void visitSchemas(String name, Schema<?> schema, Visitor visitor) {
        Preconditions.checkArgument(name != null);
        visitSchemas(ImmutableList.of(new SchemaWithName(stripLeadingSlash(name), schema)), visitor);
    }

    private static String stripLeadingSlash(String name) {
        if (name.startsWith("/")) {
            return name.substring(1);
        } else {
            return name;
        }
    }

    static void visitSchemas(ImmutableList<SchemaWithName> schemaPath, Visitor visitor) {
        Schema<?> schema = schemaPath.last().schema;
        visitor.startSchema(schemaPath);
        if (schema.getAdditionalProperties() instanceof Schema) {
            visitSchemas(
                    schemaPath.add(
                            new SchemaWithName("additionalProperties", (Schema<?>) schema.getAdditionalProperties())),
                    visitor);
        }
        if (schema.getNot() != null) {
            visitSchemas(schemaPath.add(new SchemaWithName("not", schema.getNot())), visitor);
        }
        if (schema.getProperties() != null) {
            schema.getProperties().entrySet()
                    .forEach(x -> visitSchemas(schemaPath.add(new SchemaWithName(x.getKey(), x.getValue())), visitor));
        }
        if (schema instanceof ArraySchema) {
            ArraySchema a = (ArraySchema) schema;
            if (a.getItems() != null) {
                visitSchemas(schemaPath.add(new SchemaWithName(schemaPath.last().name + "Item", a.getItems())),
                        visitor);
            }
        } else if (schema instanceof ComposedSchema) {
            ComposedSchema a = (ComposedSchema) schema;
            if (a.getAllOf() != null) {
                a.getAllOf().forEach(x -> visitSchemas(schemaPath.add(new SchemaWithName(null, x)), visitor));
            }
            if (a.getOneOf() != null) {
                a.getOneOf().forEach(x -> visitSchemas(schemaPath.add(new SchemaWithName(null, x)), visitor));
            }
            if (a.getAnyOf() != null) {
                a.getAnyOf().forEach(x -> visitSchemas(schemaPath.add(new SchemaWithName(null, x)), visitor));
            }
        } else if (schema instanceof MapSchema) {
            // nothing to add here
        } else if (schema instanceof ObjectSchema) {
            // nothing to add here
        }
        visitor.finishSchema(schemaPath);
    }

    public static final boolean isComplexSchema(Schema<?> schema) {
        for (Class<? extends Schema<?>> cls : COMPLEX_SCHEMA_CLASSES) {
            if (cls.isAssignableFrom(schema.getClass())) {
                return true;
            }
        }
        return schema.getProperties() != null;
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Schema<?>>> COMPLEX_SCHEMA_CLASSES = Lists.newArrayList( //
            ObjectSchema.class, MapSchema.class, ComposedSchema.class, ArraySchema.class);
}
