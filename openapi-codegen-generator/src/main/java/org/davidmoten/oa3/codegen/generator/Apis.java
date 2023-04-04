package org.davidmoten.oa3.codegen.generator;

import java.util.List;
import java.util.Locale;

import org.davidmoten.oa3.codegen.generator.internal.ImmutableList;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Preconditions;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

class Apis {

    static void visitSchemas(OpenAPI api, Visitor visitor) {
        if (api.getPaths() != null) {
            api.getPaths().forEach((name, pathItem) -> visitSchemas(ImmutableList.of("Path", name), pathItem, visitor));
        }
        if (api.getComponents() != null) {
            if (api.getComponents().getParameters() != null)
                api.getComponents().getParameters().forEach(
                        (name, parameter) -> visitSchemas(ImmutableList.of("Parameter").add(name), parameter, visitor));
            if (api.getComponents().getPathItems() != null)
                api.getComponents().getPathItems().forEach(
                        (name, pathItem) -> visitSchemas(ImmutableList.of("PathItem").add(name), pathItem, visitor));
            if (api.getComponents().getRequestBodies() != null)
                api.getComponents().getRequestBodies().forEach((name,
                        requestBody) -> visitSchemas(ImmutableList.of("RequestBody").add(name), requestBody, visitor));
            if (api.getComponents().getResponses() != null)
                api.getComponents().getResponses().forEach(
                        (name, response) -> visitSchemas(ImmutableList.of("Response").add(name), response, visitor));
            if (api.getComponents().getSchemas() != null)
                api.getComponents().getSchemas().forEach((key, value) -> visitSchemas(key, value, visitor));
        }
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

    static void visitSchemas(ImmutableList<String> names, PathItem pathItem, Visitor visitor) {
        if (pathItem.readOperationsMap() != null) {
            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                visitSchemas(names.add(Names.upperFirst(httpMethod.toString().toLowerCase(Locale.ENGLISH))), operation,
                        visitor);
            });
        }
        if (pathItem.getParameters() != null) {
            pathItem.getParameters().forEach(p -> visitSchemas(names.add(p.getName()), p, visitor));
        }
    }

    private static void visitSchemas(ImmutableList<String> list, Operation operation, Visitor visitor) {
        if (operation == null) {
            return;
        }
        if (operation.getParameters() != null) {
            operation.getParameters().forEach(p -> visitSchemas(list.add(p.getName()), p, visitor));
        }
        visitSchemas(list, operation.getRequestBody(), visitor);
        if (operation.getResponses() != null) {
            operation.getResponses().forEach((statusCode, response) -> {
                visitSchemas(list.add(statusCode), response, visitor);
            });
        }
    }

    private static void visitSchemas(ImmutableList<String> list, ApiResponse response, Visitor visitor) {
        visitSchemas(list, response.getContent(), visitor);
    }

    private static void visitSchemas(ImmutableList<String> list, RequestBody requestBody, Visitor visitor) {
        if (requestBody != null) {
            visitSchemas(list, requestBody.getContent(), visitor);
        }
    }

    private static void visitSchemas(ImmutableList<String> list, Content content, Visitor visitor) {
        if (content != null) {
            content.forEach((mimeType, mediaType) -> {
                if (mimeType.equals("application/json")) {
                    visitSchemas(list, mediaType, visitor);
                } else {
                    visitSchemas(list.add(mimeType), mediaType, visitor);
                }
            });
        }
    }

    private static void visitSchemas(ImmutableList<String> list, MediaType mediaType, Visitor visitor) {
        visitSchemas(list, mediaType.getSchema(), visitor);
    }

    private static String toName(ImmutableList<String> list) {
        StringBuilder b = new StringBuilder();
        for (String s : list) {
            if (b.length() > 0) {
                b.append("_");
            }
            b.append(s);
        }
        return b.toString();
    }

    private static void visitSchemas(ImmutableList<String> list, Parameter parameter, Visitor visitor) {
        if (parameter != null) {
            visitSchemas(list.add("Parameter").add(parameter.getName()), parameter.getSchema(), visitor);
        }
    }

    private static void visitSchemas(ImmutableList<String> list, Schema<?> schema, Visitor visitor) {
        if (schema != null) {
            ImmutableList<SchemaWithName> schemaPath = ImmutableList.of(new SchemaWithName(toName(list), schema));
            visitSchemas(schemaPath, visitor);
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
        } 
        // MapSchema and ObjectSchema have nothing to add
        visitor.finishSchema(schemaPath);
    }

    static final boolean isComplexSchema(Schema<?> schema) {
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
