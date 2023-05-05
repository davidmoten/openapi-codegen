package org.davidmoten.oa3.codegen.generator;

import java.util.List;
import java.util.Locale;

import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.util.ImmutableList;

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
            api.getPaths().forEach((name, pathItem) -> visitSchemas(SchemaCategory.PATH, ImmutableList.of(name),
                    pathItem, visitor, api));
        }
        if (api.getComponents() != null) {
            if (api.getComponents().getParameters() != null)
                api.getComponents().getParameters().forEach((name, parameter) -> visitSchemas(SchemaCategory.PARAMETER,
                        ImmutableList.of(name), parameter, visitor, api));
            if (api.getComponents().getPathItems() != null)
                api.getComponents().getPathItems().forEach((name, pathItem) -> visitSchemas(SchemaCategory.PATH_ITEM,
                        ImmutableList.of(name), pathItem, visitor, api));
            if (api.getComponents().getRequestBodies() != null)
                api.getComponents().getRequestBodies()
                        .forEach((name, requestBody) -> visitSchemas(SchemaCategory.REQUEST_BODY,
                                ImmutableList.of(name), requestBody, visitor, api));
            if (api.getComponents().getResponses() != null)
                api.getComponents().getResponses().forEach((name, response) -> visitSchemas(SchemaCategory.RESPONSE,
                        ImmutableList.of(name), response, visitor, api));
            if (api.getComponents().getSchemas() != null)
                api.getComponents().getSchemas().forEach((key, value) -> visitSchemas(key, value, visitor));
        }
    }

    private static void visitSchemas(String name, Schema<?> schema, Visitor visitor) {
        Preconditions.checkArgument(name != null);
        visitSchemas(SchemaCategory.SCHEMA, ImmutableList.of(new SchemaWithName(stripLeadingSlash(name), schema)),
                visitor);
    }

    private static String stripLeadingSlash(String name) {
        if (name.startsWith("/")) {
            return name.substring(1);
        } else {
            return name;
        }
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> names, PathItem pathItem,
            Visitor visitor, OpenAPI api) {
        pathItem = resolveRefs(api, pathItem);
        if (pathItem.readOperationsMap() != null) {
            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                visitSchemas(category, names.add(Names.upperFirst(httpMethod.toString().toLowerCase(Locale.ENGLISH))),
                        operation, visitor, api);
            });
        }
        if (pathItem.getParameters() != null) {
            pathItem.getParameters().forEach(p -> visitSchemas(category, names.add(p.getName()), p, visitor, api));
        }
    }

    private static PathItem resolveRefs(OpenAPI api, PathItem item) {
        while (item.get$ref() != null) {
            item = api.getComponents().getPathItems().get(Names.lastComponent(item.get$ref()));
        }
        return item;
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> list, Operation operation,
            Visitor visitor, OpenAPI api) {
        if (operation == null) {
            return;
        }
        if (operation.getParameters() != null) {
            operation.getParameters().forEach(p -> visitSchemas(category, list.add(p.getName()), p, visitor, api));
        }
        visitSchemas(category, list, operation.getRequestBody(), visitor, api);
        if (operation.getResponses() != null) {
            operation.getResponses().forEach((statusCode, response) -> {
                visitSchemas(category, list.add(statusCode), response, visitor, api);
            });
        }
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> list, ApiResponse response,
            Visitor visitor, OpenAPI api) {
        response = resolveRefs(api, response);
        visitSchemas(category, category == SchemaCategory.RESPONSE ? list : list.add("Response"), response.getContent(),
                visitor);
    }

    private static ApiResponse resolveRefs(OpenAPI api, ApiResponse response) {
        while (response.get$ref() != null) {
            response = api.getComponents().getResponses().get(Names.lastComponent(response.get$ref()));
        }
        return response;
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> list, RequestBody requestBody,
            Visitor visitor, OpenAPI api) {
        if (requestBody != null) {
            requestBody = resolveRefs(api, requestBody);
            visitSchemas(category, list.add("Request"), requestBody.getContent(), visitor);
        }
    }

    private static RequestBody resolveRefs(OpenAPI api, RequestBody requestBody) {
        while (requestBody.get$ref() != null) {
            requestBody = api.getComponents().getRequestBodies().get(Names.lastComponent(requestBody.get$ref()));
        }
        return requestBody;
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> list, Content content,
            Visitor visitor) {
        if (content != null) {
            content.forEach((mimeType, mediaType) -> {
                if (mimeType.equals("application/json")) {
                    visitSchemas(category, list, mediaType, visitor);
                } else {
                    visitSchemas(category, list.add(mimeType), mediaType, visitor);
                }
            });
        }
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> list, MediaType mediaType,
            Visitor visitor) {
        visitSchemas(category, list, mediaType.getSchema(), visitor);
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> list, Parameter parameter,
            Visitor visitor, OpenAPI api) {
        if (parameter != null) {
            parameter = resolveRefs(api, parameter);
            if (parameter.getSchema() != null && !Util.isPrimitive(parameter.getSchema())) {
                visitSchemas(category, list.add("Parameter").add(parameter.getName()), parameter.getSchema(), visitor);
            }
            visitSchemas(category, list.add("Parameter").add(parameter.getName()), parameter.getContent(), visitor);
        }
    }

    private static Parameter resolveRefs(OpenAPI api, Parameter parameter) {
        while (parameter.get$ref() != null) {
            parameter = api.getComponents().getParameters().get(Names.lastComponent(parameter.get$ref()));
        }
        return parameter;
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> list, Schema<?> schema,
            Visitor visitor) {
        if (schema != null) {
            ImmutableList<SchemaWithName> schemaPath = ImmutableList
                    .of(new SchemaWithName(Names.toIdentifier(list), schema));
            visitSchemas(category, schemaPath, visitor);
        }
    }

    static void visitSchemas(SchemaCategory category, ImmutableList<SchemaWithName> schemaPath, Visitor visitor) {
        Schema<?> schema = schemaPath.last().schema;
        visitor.startSchema(category, schemaPath);
        if (schema.getAdditionalProperties() instanceof Schema) {
            visitSchemas(category,
                    schemaPath.add(
                            new SchemaWithName("additionalProperties", (Schema<?>) schema.getAdditionalProperties())),
                    visitor);
        }
        if (schema.getNot() != null) {
            visitSchemas(category, schemaPath.add(new SchemaWithName("not", schema.getNot())), visitor);
        }
        if (schema.getProperties() != null) {
            schema.getProperties().entrySet().forEach(
                    x -> visitSchemas(category, schemaPath.add(new SchemaWithName(x.getKey(), x.getValue())), visitor));
        }
        if (schema instanceof ArraySchema) {
            ArraySchema a = (ArraySchema) schema;
            if (a.getItems() != null) {
                visitSchemas(category,
                        schemaPath.add(new SchemaWithName(schemaPath.last().name + "Item", a.getItems())), visitor);
            }
        } else if (schema instanceof ComposedSchema) {
            ComposedSchema a = (ComposedSchema) schema;
            if (a.getAllOf() != null) {
                a.getAllOf().forEach(x -> visitSchemas(category, schemaPath.add(new SchemaWithName(null, x)), visitor));
            }
            if (a.getOneOf() != null) {
                a.getOneOf().forEach(x -> visitSchemas(category, schemaPath.add(new SchemaWithName(null, x)), visitor));
            }
            if (a.getAnyOf() != null) {
                a.getAnyOf().forEach(x -> visitSchemas(category, schemaPath.add(new SchemaWithName(null, x)), visitor));
            }
        }
        // MapSchema and ObjectSchema have nothing to add

        visitor.finishSchema(category, schemaPath);
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
