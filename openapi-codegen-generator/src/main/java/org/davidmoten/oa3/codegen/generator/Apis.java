package org.davidmoten.oa3.codegen.generator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.util.ImmutableList;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Maps;
import com.github.davidmoten.guavamini.Preconditions;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Encoding;
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
                // Note that this is an OpenAPI 3.1 feature
                api.getComponents().getPathItems().forEach((name, pathItem) -> visitSchemas(SchemaCategory.PATH_ITEM,
                        ImmutableList.of(name), pathItem, visitor, api));
            if (api.getComponents().getRequestBodies() != null)
                api.getComponents().getRequestBodies()
                        .forEach((name, requestBody) -> visitSchemas(SchemaCategory.REQUEST_BODY,
                                ImmutableList.of(name), requestBody, visitor, api));
            if (api.getComponents().getResponses() != null)
                api.getComponents().getResponses().forEach((name, response) -> visitSchemas(SchemaCategory.RESPONSE,
                        ImmutableList.of(name), response, visitor, api));
            if (api.getComponents().getHeaders() != null) {
                api.getComponents().getHeaders().forEach((key, header) -> visitSchemas(SchemaCategory.HEADER,
                        ImmutableList.of(key), header, visitor, api));
            }
            if (api.getComponents().getSchemas() != null)
                api.getComponents().getSchemas().forEach((key, value) -> visitSchemas(key, value, visitor));
        }
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> names, Header header,
            Visitor visitor, OpenAPI api) {
        if (header != null) {
            header = resolveRefs(api, header);
            visitSchemas(category, names, header.getSchema(), Maps.empty(),  visitor);
        }
    }

    private static Header resolveRefs(OpenAPI api, Header header) {
        while (header.get$ref() != null) {
            header = api.getComponents().getHeaders().get(Names.lastComponent(header.get$ref()));
        }
        return header;
    }

    private static void visitSchemas(String name, Schema<?> schema, Visitor visitor) {
        Preconditions.checkArgument(name != null);
        visitSchemas(SchemaCategory.SCHEMA, ImmutableList.of(new SchemaWithName(stripLeadingSlash(name), schema)), 
                Maps.empty(), visitor);
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
                final ImmutableList<String> names2;
                if (operation.getOperationId() != null) {
                    names2 = ImmutableList.of(operation.getOperationId());
                } else {
                    names2 = names.add(Names.upperFirst(httpMethod.toString().toLowerCase(Locale.ENGLISH)));
                }
                visitSchemas(category, names2, operation, visitor, api);
            });
        }
        if (pathItem.getParameters() != null) {
            pathItem.getParameters().forEach(p -> {
                p = resolveRefs(api, p);
                visitSchemas(category, names.add(p.getName()), p, visitor, api);
            });
        }
    }

    static PathItem resolveRefs(OpenAPI api, PathItem item) {
        // Note that components.pathItems only exists with OpenApi 3.1
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
            operation.getParameters().forEach(p -> {
                p = resolveRefs(api, p);
                visitSchemas(category, list.add(p.getName()), p, visitor, api);
            });
        }
        visitSchemas(category, list, operation.getRequestBody(), visitor, api);
        if (operation.getResponses() != null) {
            operation.getResponses().forEach((statusCode, response) -> {
                response = resolveRefs(api, response);
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
        visitSchemas(category, list, mediaType.getSchema(), mediaType.getEncoding(), visitor);
    }

    private static void visitSchemas(SchemaCategory category, ImmutableList<String> list, Parameter parameter,
            Visitor visitor, OpenAPI api) {
        if (parameter != null) {
            parameter = resolveRefs(api, parameter);
            if (parameter.getSchema() != null && !Util.isPrimitive(parameter.getSchema())) {
                visitSchemas(category, list.add("Parameter").add(parameter.getName()), parameter.getSchema(), Maps.empty(), visitor);
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
            Map<String, Encoding> propertyEncoding, Visitor visitor) {
        if (schema != null) {
            ImmutableList<SchemaWithName> schemaPath = ImmutableList
                    .of(new SchemaWithName(Names.toIdentifier(list), schema));
            visitSchemas(category, schemaPath, propertyEncoding, visitor);
        }
    }

    static void visitSchemas(SchemaCategory category, ImmutableList<SchemaWithName> schemaPath, Map<String, Encoding> propertyEncoding, Visitor visitor) {
        Schema<?> schema = schemaPath.last().schema;
        visitor.startSchema(category, schemaPath);
        if (schema instanceof ObjectSchema && schema.getProperties() == null
                && schema.getAdditionalProperties() == null) {
            schema.setAdditionalProperties(Boolean.TRUE);
        }
        if (Boolean.TRUE.equals(schema.getAdditionalProperties())) {
            schema.setAdditionalProperties(new Schema<>());
        }
        if (schema.getAdditionalProperties() instanceof Schema) {
            visitSchemas(category,
                    schemaPath.add(new SchemaWithName("properties", (Schema<?>) schema.getAdditionalProperties())), Maps.empty(),
                    visitor);
        } else if (schema.getNot() != null) {
            visitSchemas(category, schemaPath.add(new SchemaWithName("not", schema.getNot())), Maps.empty(), visitor);
        }
        if (schema.getProperties() != null) {
            schema.getProperties().entrySet().forEach(
                    x -> {
                        final Schema<?> sch;
                        if (propertyEncoding != null && propertyEncoding.containsKey(x.getKey())) {
                            Encoding encoding = propertyEncoding.get(x.getKey());
                            if (encoding.getContentType() != null) {
                                List<String> contentTypes = Arrays.stream(encoding.getContentType() //
                                        .split(",")) //
                                        .map(y -> y.trim()) //
                                        .collect(Collectors.toList());
                                Schema<String> contentTypeSchema = enumSchema(contentTypes);
                                if (contentTypes.size() == 1) {
                                    contentTypeSchema.setDefault(contentTypes.get(0));
                                }
                                contentTypeSchema.setExtensions(Maps.hashMap().put(ExtensionKeys.HAS_ENCODING, (Object) Boolean.TRUE).build());
                                ObjectSchema combined = new ObjectSchema();
                                combined.setProperties(new LinkedHashMap<>());
                                combined.getProperties().put("contentType", contentTypeSchema);
                                combined.getProperties().put("value", x.getValue());
                                combined.setRequired(Lists.of("value", "contentType"));
                                combined.setExtensions(Maps.hashMap().put(ExtensionKeys.HAS_ENCODING, (Object) Boolean.TRUE).build());
                                
                                sch = combined;
                            } else {
                                sch = x.getValue();
                            }
                        } else {
                            sch = x.getValue();
                        }
                        visitSchemas(category, schemaPath.add(new SchemaWithName(x.getKey(), sch)),
                                Maps.empty(), visitor);
                    });
        }
        if (schema instanceof ArraySchema) {
            ArraySchema a = (ArraySchema) schema;
            if (a.getItems() != null) {
                visitSchemas(category,
                        schemaPath.add(new SchemaWithName(schemaPath.last().name + "Item", a.getItems())), Maps.empty(), visitor);
            }
        } else if (schema instanceof ComposedSchema) {
            ComposedSchema a = (ComposedSchema) schema;
            if (a.getAllOf() != null) {
                a.getAllOf().forEach(x -> visitSchemas(category, schemaPath.add(new SchemaWithName(null, x)), Maps.empty(), visitor));
            }
            if (a.getOneOf() != null) {
                a.getOneOf().forEach(x -> visitSchemas(category, schemaPath.add(new SchemaWithName(null, x)), Maps.empty(), visitor));
            }
            if (a.getAnyOf() != null) {
                a.getAnyOf().forEach(x -> visitSchemas(category, schemaPath.add(new SchemaWithName(null, x)), Maps.empty(), visitor));
            }
        }
        // MapSchema and ObjectSchema have nothing to add

        visitor.finishSchema(category, schemaPath);
    }

    private static Schema<String> enumSchema(List<String> values) {
        Schema<String> schema = new Schema<>();
        schema.setType("string");
        schema.setEnum(values);
        return schema;
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
    private static List<Class<? extends Schema<?>>> COMPLEX_SCHEMA_CLASSES = Lists.of( //
            ObjectSchema.class, MapSchema.class, ComposedSchema.class, ArraySchema.class);
}
