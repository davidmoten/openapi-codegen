package org.davidmoten.oa3.codegen.generator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.Generator.Cls;
import org.davidmoten.oa3.codegen.generator.Generator.MyVisitor;
import org.davidmoten.oa3.codegen.generator.Generator.MyVisitor.Result;
import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.util.ImmutableList;
import org.springframework.core.io.Resource;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

public class SpringBootGenerator {

    private final Names names;
    private final Map<String, Cls> refCls;
    private final Map<Schema<?>, Cls> schemaCls;

    public SpringBootGenerator(Definition definition) {
        this.names = new Names(definition);
        // make a ref map
        Map<String, Cls> refCls = new HashMap<String, Cls>();
        Map<Schema<?>, Cls> schemaCls = new HashMap<Schema<?>, Cls>();
        MyVisitor v = new MyVisitor(names);
        Apis.visitSchemas(names.api(), v);
        for (Result result : v.results()) {
            Cls c = result.cls;
            if (c.topLevel) {
                String refPrefix = c.category.refPrefix();
                String ref = refPrefix + c.name.get();
                refCls.put(ref, c);
                schemaCls.put(c.schema.get(), c);
            }
        }
        this.refCls = refCls;
        this.schemaCls = schemaCls;
    }

    public void generate() {
        // want a method per path, operation combo
        List<Method> methods = new ArrayList<>();
        // TODO handle path $ref
        names.api().getPaths().forEach((pathName, pathItem) -> {
            gatherMethods(pathName, pathItem, methods);
        });

        SpringBootCodeWriter.writeServiceClasses(names, methods);
        ClientCodeWriter.writeClientClass(names, methods);
    }

    private void gatherMethods(String pathName, PathItem pathItem, List<Method> methods) {
        // TODO pathItem.get$ref();
        pathItem.readOperationsMap() //
                .forEach((method, operation) -> gatherMethods(pathName, method, operation, methods));
    }

    private void gatherMethods(String pathName, HttpMethod method, Operation operation, List<Method> methods) {
        String methodName = Names
                .toIdentifier(ImmutableList.of(pathName, method.toString().toLowerCase(Locale.ENGLISH)));
        Optional<Integer> statusCode = Optional.empty();
        List<Param> params = new ArrayList<>();
        Optional<String> returnFullClassName = Optional.empty();
        List<String> consumes = new ArrayList<>();
        List<String> produces = new ArrayList<>();
        if (operation.getParameters() != null) {
            operation.getParameters() //
                    .forEach(p -> {
                        p = resolveParameterRefs(p);
                        boolean isArray = false;
                        Schema<?> s = p.getSchema();
                        if (Util.isArray(s)) {
                            isArray = true;
                            s = s.getItems();
                        }
                        s = resolveRefs(s);
                        Optional<Object> defaultValue = Optional.ofNullable(s.getDefault());
                        final Param param;
                        if (Util.isPrimitive(s)) {
                            // handle simple schemas
                            Class<?> c = Util.toClass(s.getType(), s.getFormat(), names.mapIntegerToBigInteger());
                            param = new Param(p.getName(), Names.toIdentifier(p.getName()), defaultValue,
                                    p.getRequired(), c.getCanonicalName(), isArray, false, constraints(s),
                                    ParamType.valueOf(p.getIn().toUpperCase(Locale.ENGLISH)), false,
                                    Optional.ofNullable(p.getDescription()));
                        } else {
                            // is complex schema
                            Cls cls = schemaCls.get(s);
                            param = new Param(p.getName(), Names.toIdentifier(p.getName()), defaultValue,
                                    p.getRequired(), cls.fullClassName, isArray, false, constraints(s),
                                    ParamType.valueOf(p.getIn().toUpperCase(Locale.ENGLISH)), true,
                                    Optional.ofNullable(p.getDescription()));
                        }
                        params.add(param);
                    });
        }
        if (operation.getRequestBody() != null) {
            RequestBody b = resolveRefs(operation.getRequestBody());
            MediaType mediaType = b.getContent().get("application/json");
            if (mediaType == null) {
                mediaType = b.getContent().get("application/xml");
            }
            if (mediaType != null) {
                Schema<?> schema = mediaType.getSchema();
                if (schema != null) {
                    if (schemaCls.get(schema) != null) {
                        String fullClassName = resolveRefsFullClassName(schema);
                        params.add(new Param("requestBody", "requestBody",
                                Optional.ofNullable((Object) schema.getDefault()),
                                org.davidmoten.oa3.codegen.util.Util.orElse(b.getRequired(), false), fullClassName,
                                false, true, constraints(schema), ParamType.BODY, false,
                                Optional.ofNullable(schema.getDescription())));
                    } else {
                        throw new RuntimeException("unexpected");
                    }
                }
            } else {
                System.out.println("TODO handle request body with media types " + b.getContent().keySet());
            }
            consumes = new ArrayList<>(b.getContent().keySet());
        }
        Optional<StatusCodeApiResponse> response = primaryResponse(operation.getResponses());
        Optional<Integer> primaryStatusCode = response.map(x -> x.statusCode);
        if (response.isPresent()) {

            Content content = resolveResponseRefs(response.get().response).getContent();
            if (content != null) {
                MediaType mediaType = content.get("application/json");
                if (mediaType == null) {
                    mediaType = content.get("application/xml");
                }
                if (mediaType != null) {
                    returnFullClassName = Optional.of(resolveRefsFullClassName(mediaType.getSchema()));
                } else {
                    // loop through all mime-types and pick first non-default to infer return class
                    // name
                    final String defaultReturnClassFullName = Resource.class.getCanonicalName();
                    returnFullClassName = Optional.of(content. //
                            keySet() //
                            .stream() //
                            .filter(x -> !"default".equals(x)) //
                            .map(x -> {
                                if (x.startsWith("text/")) {
                                    return String.class.getCanonicalName();
                                } else {
                                    return defaultReturnClassFullName;
                                }
                            }) //
                            .findFirst() //
                            .orElse(defaultReturnClassFullName));
                }
                statusCode = Optional.of(response.get().statusCode);
                produces = new ArrayList<>(content.keySet());
            } else {
                System.out.println("TODO handle response ref");
            }
        }

        List<ResponseDescriptor> responseDescriptors = responseDescriptors(operation);

        Method m = new Method(methodName, statusCode, params, returnFullClassName, pathName, method, consumes, produces,
                Optional.ofNullable(operation.getDescription()), primaryStatusCode, responseDescriptors);
        methods.add(m);
    }

    private List<ResponseDescriptor> responseDescriptors(Operation operation) {
        List<ResponseDescriptor> list = new ArrayList<>();
        operation.getResponses().forEach((statusCode, response) -> {
            response = resolveResponseRefs(response);
            if (response.getContent() != null) {
                response.getContent().forEach((contentType, mediaType) -> {
                    final String fullClassName;
                    if (mediaType.getSchema() == null) {
                        fullClassName = byte[].class.getCanonicalName();
                    } else {
                        fullClassName = resolveRefsFullClassName(mediaType.getSchema());
                    }
                    list.add(new ResponseDescriptor(statusCode, contentType, fullClassName));
                });
            }
        });
        return list;
    }

    private RequestBody resolveRefs(RequestBody b) {
        while (b.get$ref() != null) {
            b = names.lookupRequestBody(b.get$ref());
        }
        return b;
    }

    private static Constraints constraints(Schema<?> schema) {
        return new Constraints(Optional.ofNullable(schema.getMinLength()), Optional.ofNullable(schema.getMaxLength()),
                Optional.ofNullable(schema.getMinimum()), Optional.ofNullable(schema.getMaximum()),
                Optional.ofNullable(schema.getExclusiveMinimumValue()),
                Optional.ofNullable(schema.getExclusiveMaximumValue()), Optional.ofNullable(schema.getMinItems()),
                Optional.ofNullable(schema.getMaxItems()), Optional.ofNullable(schema.getPattern()));
    }

    private static Optional<StatusCodeApiResponse> primaryResponse(ApiResponses responses) {
        if (responses.get("200") != null) {
            return Optional.of(new StatusCodeApiResponse(200, responses.get("200")));
        } else {
            for (Entry<String, ApiResponse> r : responses.entrySet()) {
                if (is2XX(r.getKey())) {
                    return Optional.of(new StatusCodeApiResponse(Integer.parseInt(r.getKey()), r.getValue()));
                }
            }
        }
        return Optional.empty();
    }

    private static final class StatusCodeApiResponse {
        final int statusCode;
        final ApiResponse response;

        StatusCodeApiResponse(int statusCode, ApiResponse response) {
            this.statusCode = statusCode;
            this.response = response;
        }
    }

    private static boolean is2XX(String key) {
        return key.length() == 3 && key.startsWith("2");
    }

    private Parameter resolveParameterRefs(Parameter p) {
        while (p.get$ref() != null) {
            p = names.lookupParameter(p.get$ref());
        }
        return p;
    }

    private ApiResponse resolveResponseRefs(ApiResponse r) {
        while (r.get$ref() != null) {
            r = names.lookupResponse(r.get$ref());
        }
        return r;
    }

    private String resolveRefsFullClassName(Schema<?> schema) {
        return schemaCls.get(resolveRefs(schema)).fullClassName;
    }

    private Schema<?> resolveRefs(Schema<?> schema) {
        Schema<?> s = schema;
        while (s.get$ref() != null) {
            s = refCls.get(s.get$ref()).schema.get();
        }
        return s;
    }

    static final class Method {
        final String methodName;
        final List<Param> parameters;
        final Optional<String> returnFullClassName; // arrays always wrapped ?
        final String path;
        final HttpMethod httpMethod;
        final Optional<Integer> statusCode;
        final List<String> consumes;
        final List<String> produces;
        final Optional<String> description;
        final Optional<Integer> primaryStatusCode;
        final List<ResponseDescriptor> responseDescriptors;

        Method(String methodName, Optional<Integer> statusCode, List<Param> parameters,
                Optional<String> returnFullClassName, String path, HttpMethod httpMethod, List<String> consumes,
                List<String> produces, Optional<String> description, Optional<Integer> primaryStatusCode,
                List<ResponseDescriptor> responseDescriptors) {
            this.methodName = methodName;
            this.statusCode = statusCode;
            this.parameters = parameters;
            this.returnFullClassName = returnFullClassName;
            this.path = path;
            this.httpMethod = httpMethod;
            this.consumes = consumes;
            this.produces = produces;
            this.description = description;
            this.primaryStatusCode = primaryStatusCode;
            this.responseDescriptors = responseDescriptors;
        }

        @Override
        public String toString() {
            return "Method [path=" + path + ", httpMethod=" + httpMethod + ", methodName=" + methodName + ", returnCls="
                    + returnFullClassName.orElse("") + ", parameters="
                    + parameters.stream().map(Object::toString).map(x -> "\n    " + x).collect(Collectors.joining())
                    + "]";
        }

    }

    static final class ResponseDescriptor {
        private final String statusCode; // can be a pattern like `2*`
        private final String mediaType;
        private final String fullClassName;

        ResponseDescriptor(String statusCode, String mediaType, String fullClassName) {
            this.statusCode = statusCode;
            this.mediaType = mediaType;
            this.fullClassName = fullClassName;
        }

        String statusCode() {
            return statusCode;
        }

        String mediaType() {
            return mediaType;
        }

        String fullClassName() {
            return fullClassName;
        }

    }

    static final class Constraints {
        final Optional<Integer> minLength;
        final Optional<Integer> maxLength;
        final Optional<BigDecimal> min;
        final Optional<BigDecimal> max;
        final Optional<BigDecimal> minExclusive;
        final Optional<BigDecimal> maxExclusive;
        final Optional<Integer> minItems;
        final Optional<Integer> maxItems;
        final Optional<String> pattern;

        Constraints(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<BigDecimal> min,
                Optional<BigDecimal> max, Optional<BigDecimal> minExclusive, Optional<BigDecimal> maxExclusive,
                Optional<Integer> minItems, Optional<Integer> maxItems, Optional<String> pattern) {
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.min = min;
            this.max = max;
            this.minExclusive = minExclusive;
            this.maxExclusive = maxExclusive;
            this.minItems = minItems;
            this.maxItems = maxItems;
            this.pattern = pattern;
        }

        boolean atLeastOnePresent() {
            return minLength.isPresent() || maxLength.isPresent() || min.isPresent() || max.isPresent()
                    || minExclusive.isPresent() || maxExclusive.isPresent() || minItems.isPresent()
                    || maxItems.isPresent() || pattern.isPresent();
        }

    }

    static final class Param {
        final String name;
        final String identifier;
        final Optional<Object> defaultValue;
        final boolean required;
        final String fullClassName;
        final boolean isArray;
        final boolean isRequestBody;
        final Constraints constraints;
        final ParamType type;
        final boolean isComplexQueryParameter;
        final Optional<String> description;

        Param(String name, String identifier, Optional<Object> defaultValue, boolean required, String fullClassName,
                boolean isArray, boolean isRequestBody, Constraints constraints, ParamType type,
                boolean isComplexQueryParameter, Optional<String> description) {
            this.name = name;
            this.identifier = identifier;
            this.defaultValue = defaultValue;
            this.required = required;
            this.fullClassName = fullClassName;
            this.isArray = isArray;
            this.isRequestBody = isRequestBody;
            this.constraints = constraints;
            this.type = type;
            this.isComplexQueryParameter = isComplexQueryParameter;
            this.description = description;
        }

        @Override
        public String toString() {
            return "Param [" + identifier + ", name=" + name + ", defaultValue=" + defaultValue + ", required="
                    + required + ", cls=" + fullClassName + ", isArray=" + isArray + "]";
        }
    }

}
