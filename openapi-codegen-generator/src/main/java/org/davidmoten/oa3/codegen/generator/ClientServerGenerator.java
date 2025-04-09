package org.davidmoten.oa3.codegen.generator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.Generator.Cls;
import org.davidmoten.oa3.codegen.generator.Generator.MyVisitor;
import org.davidmoten.oa3.codegen.generator.Generator.MyVisitor.Result;
import org.davidmoten.oa3.codegen.generator.internal.Mutable;
import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.generator.writer.ClientCodeWriter;
import org.davidmoten.oa3.codegen.generator.writer.ServerCodeWriterSpringBoot;
import org.davidmoten.oa3.codegen.util.ImmutableList;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

public class ClientServerGenerator {

    private final Names names;
    private final Map<String, Cls> refCls;
    private final Map<Schema<?>, Cls> schemaCls;

    public ClientServerGenerator(Definition definition) {
        this.names = new Names(definition);
        // make a ref map
        Map<String, Cls> refCls = new HashMap<>();
        Map<Schema<?>, Cls> schemaCls = new HashMap<>();
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

    public void generateServer() {
        List<Method> methods = collectMethods();
        ServerCodeWriterSpringBoot.writeServiceClasses(names, methods);
    }

    public void generateClient() {
        List<Method> methods = collectMethods();
        ClientCodeWriter.writeClientClass(names, methods);
    }

    private List<Method> collectMethods() {
        // want a method per path, operation combo
        List<Method> methods = new ArrayList<>();
        if (names.api().getPaths() != null) {
            names.api().getPaths().forEach((pathName, pathItem) -> {
                gatherMethods(pathName, pathItem, methods);
            });
        }
        return methods;
    }

    private void gatherMethods(String pathName, PathItem pathItem, List<Method> methods) {
        pathItem = Apis.resolveRefs(names.api(), pathItem);
        pathItem.readOperationsMap() //
                .forEach((method, operation) -> gatherMethods(pathName, method, operation, methods));
    }
    
    private void gatherMethods(String pathName, HttpMethod method, Operation operation, List<Method> methods) {
        final String methodName;
        if (!Util.isNullOrBlank(operation.getOperationId())) {
            methodName = Names.toIdentifier(operation.getOperationId());
        } else {
            methodName = Names.toIdentifier(ImmutableList.of(pathName, method.toString().toLowerCase(Locale.ENGLISH)));
        }
        Optional<String> statusCode = Optional.empty();
        List<Param> params = new ArrayList<>();
        Optional<String> returnFullClassName = Optional.empty();
        List<String> consumes = new ArrayList<>();
        List<String> produces = new ArrayList<>();
        Set<String> parameterNames = new HashSet<>();
        if (operation.getParameters() != null) {
            operation.getParameters() //
                    .forEach(p -> {
                        // TODO handle refs to components/headers
                        p = resolveParameterRefs(p);
                        boolean isArray = false;
                        Schema<?> s = p.getSchema();
                        if (s == null) {
                            // TODO do something with p.getContent() instead of schema
                            System.out.println("[WARN] parameter skipped because without schema (not yet supported): " //
                                    + operationName(pathName, method, operation));   
                            return;
                        }
                        if (p.getName().isEmpty()) {
                            System.out.println("[WARN] parameter skipped because name empty: " //
                                    + operationName(pathName, method, operation));
                            return;
                        }
                        final Schema<?> resolvedOriginal = resolveRefs(s);
                        if (Util.isArray(s)) {
                            isArray = true;
                            s = s.getItems();
                        } 
                        s = resolveRefs(s);
                        Optional<Object> defaultValue = Optional.ofNullable(s.getDefault());
                        String parameterName = Names.toIdentifier(p.getName());
                        int i = 2;
                        while (parameterNames.contains(parameterName)) {
                            parameterName = Names.toIdentifier(p.getName()) + i;
                            i++;
                        }
                        parameterNames.add(parameterName);
                        final Param param;
                        if (Util.isPrimitive(s) && !Util.isEnum(s)) {
                            // handle simple schemas
                            Class<?> c = Util.toClass(Util.getTypeOrThrow(s), s.getFormat(), s.getExtensions(),
                                    names.mapIntegerToBigInteger(), names.mapNumberToBigDecimal());
                            param = new Param(p.getName(), parameterName, defaultValue, p.getRequired(),
                                    c.getCanonicalName(), isArray, false, constraints(s), toParamType(p), false,
                                    Optional.ofNullable(p.getDescription()), Optional.empty(), Optional.empty());
                        } else {
                            // is complex schema
                            Cls cls = schemaCls.get(resolvedOriginal);
                            param = new Param(p.getName(), parameterName, defaultValue, p.getRequired(),
                                    cls.fullClassName, isArray, false, constraints(s), toParamType(p), true,
                                    Optional.ofNullable(p.getDescription()), Optional.empty(), Optional.empty());
                        }
                        params.add(param);
                    });
        }
        if (operation.getRequestBody() != null) {
            RequestBody b = resolveRefs(operation.getRequestBody());
            MediaType mediaType = mediaType(b.getContent(), "application/json").map(Entry::getValue).orElse(null);
            if (mediaType == null) {
                mediaType = mediaType(b.getContent(), "application/xml").map(Entry::getValue).orElse(null);
            }
            final boolean isMultipartFormData;
            if (mediaType == null) {
                mediaType = mediaType(b.getContent(), "multipart/form-data").map(Entry::getValue).orElse(null);
                isMultipartFormData = mediaType != null;
            } else {
                isMultipartFormData = false;
            }
            final boolean isFormUrlEncoded;
            if (mediaType == null) {
                mediaType = mediaType(b.getContent(), "application/x-www-form-urlencoded").map(Entry::getValue).orElse(null);
                isFormUrlEncoded = mediaType != null;
            } else {
                isFormUrlEncoded = false;
            }
            if (mediaType != null) {
                Schema<?> schema = mediaType.getSchema();
                if (schema != null) {
                    if (schemaCls.get(schema) != null) {
                        String fullClassName = resolveRefsFullClassName(schema);
                        ParamType paramType;
                        if (isMultipartFormData) {
                            paramType = ParamType.MULTIPART_FORM_DATA;
                        } else if (isFormUrlEncoded){
                            paramType = ParamType.FORM_URLENCODED;
                        } else {
                            paramType = ParamType.BODY;
                        }
                        params.add(new Param("requestBody", "requestBody",
                                Optional.ofNullable((Object) schema.getDefault()),
                                org.davidmoten.oa3.codegen.util.Util.orElse(b.getRequired(), true), fullClassName,
                                false, true, constraints(schema), paramType, false,
                                Optional.ofNullable(schema.getDescription()), Optional.empty(), Optional.empty()));
                    } else {
                        throw new RuntimeException("unexpected");
                    }
                } else {
                    addRequestBodyOctetStreamParameter(params, b, Optional.empty());
                }
            } else {
                System.out.println("TODO handle request body with media types " + b.getContent().keySet());
            }
            consumes = new ArrayList<>(b.getContent().keySet());
        }
        Optional<StatusCodeApiResponse> response = primaryResponse(operation.getResponses());
        Optional<String> primaryStatusCode = response.map(x -> x.statusCode);
        Mutable<String> primaryMimeType = Mutable.create(null);
        if (response.isPresent() && response.get().response != null) {
            Content content = resolveResponseRefs(response.get().response).getContent();
            // if content is null then their is no response body
            if (content != null) {
                primaryMimeType.value = "application/json";
                MediaType mediaType = mediaType(content, "application/json").map(Entry::getValue).orElse(null);
                if (mediaType == null) {
                    primaryMimeType.value = "application/xml";
                    mediaType = mediaType(content, primaryMimeType.value).map(Entry::getValue).orElse(null);
                }
                if (mediaType != null) {
                    if (mediaType.getSchema() == null) {
                        // Any schema
                        returnFullClassName = Optional.of(Object.class.getCanonicalName());
                    } else {
                        returnFullClassName = Optional.of(resolveRefsFullClassName(mediaType.getSchema()));
                    }
                } else {
                    // loop through all mime-types and pick first non-default to infer return class
                    // name
                    final String defaultReturnClassFullName = byte[].class.getCanonicalName();
                    returnFullClassName = content. //
                            keySet() //
                            .stream() //
                            .filter(x -> !"default".equals(x)) //
                            .map(x -> {
                                primaryMimeType.value = x;
                                if (x.startsWith("text/")) {
                                    return String.class.getCanonicalName();
                                } else {
                                    return defaultReturnClassFullName;
                                }
                            }) //
                            .findFirst();
                    if (!returnFullClassName.isPresent()) {
                        primaryMimeType.value = "default";
                        MediaType a = content.get(primaryMimeType.value);
                        if (a == null) {
                            returnFullClassName = Optional.empty();
                        } else {
                            returnFullClassName = Optional.of(resolveRefsFullClassName(a.getSchema()));
                        }
                    }
                    if (!returnFullClassName.isPresent()) {
                        primaryMimeType.value = null;
                    }
                }
                statusCode = Optional.of(response.get().statusCode);
                produces = new ArrayList<>(content.keySet());
            }
        }

        List<ResponseDescriptor> responseDescriptors = responseDescriptors(operation);
        boolean includeForServerGeneration;
        if (operation.getExtensions() != null) {
            includeForServerGeneration = Boolean.TRUE.equals(
                    operation.getExtensions().getOrDefault(ExtensionKeys.INCLUDE_FOR_SERVER_GENERATION, ""));
        } else {
            includeForServerGeneration = true;
        }
        Method m = new Method(methodName, statusCode, params, returnFullClassName, pathName, method, consumes, produces,
                Optional.ofNullable(operation.getDescription()), primaryStatusCode,
                Optional.ofNullable(primaryMimeType.value), responseDescriptors, includeForServerGeneration);
        methods.add(m);
    }

    private static String operationName(String pathName, HttpMethod method, Operation operation) {
        return pathName + " " + method
                + Optional.ofNullable(operation.getOperationId()).map(x -> " [" + x + "]").orElse("");
    }

    private static ParamType toParamType(Parameter p) {
        if (p instanceof QueryParameter) {
            return ParamType.QUERY;
        } else if (p instanceof PathParameter) {
            return ParamType.PATH;
        } else if (p instanceof HeaderParameter) {
            return ParamType.HEADER;
        } else if (p instanceof CookieParameter) {
            return ParamType.COOKIE;
        } else {
            return ParamType.valueOf(p.getIn().toUpperCase(Locale.ENGLISH));
        }
    }

    private void addRequestBodyOctetStreamParameter(List<Param> params, RequestBody b, Optional<String> contentType) {
        // if no schema specified then is octet-stream
        String fullClassName = byte[].class.getCanonicalName();
        params.add(new Param("requestBody", "requestBody", Optional.empty(),
                org.davidmoten.oa3.codegen.util.Util.orElse(b.getRequired(), true), fullClassName, false,
                true, Constraints.empty(), ParamType.BODY, false,
                Optional.empty(), contentType, Optional.empty()));
    }

    private Optional<Entry<String, MediaType>> mediaType(Content content, String mimeType) {
        return content.entrySet() //
                .stream() //
                .filter(x -> x.getKey().replaceAll(";.*", "").equalsIgnoreCase(mimeType))
                .findFirst();
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
            return Optional.of(new StatusCodeApiResponse("200", responses.get("200")));
        } else {
            for (Entry<String, ApiResponse> r : responses.entrySet()) {
                if (is2XX(r.getKey())) {
                    return Optional.of(new StatusCodeApiResponse(r.getKey(), r.getValue()));
                }
            }
        }
        return Optional.empty();
    }

    public static final class StatusCodeApiResponse {
        final String statusCode;
        final ApiResponse response;

        StatusCodeApiResponse(String statusCode, ApiResponse response) {
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
            String ref = r.get$ref();
            r = names.lookupResponse(ref);
            if (r == null) {
                throw new RuntimeException("could not find response " + ref);
            }
        }
        return r;
    }

    private String resolveRefsFullClassName(Schema<?> schema) {
        return schemaCls.get(resolveRefs(schema)).fullClassName;
    }

    private Schema<?> resolveRefs(Schema<?> schema) {
        Schema<?> s = schema;
        while (s.get$ref() != null) {
            String ref = s.get$ref();
            Cls c = refCls.get(ref);
            if (c == null) {
                throw new IllegalArgumentException("Cls not found for schema=\n" + schema + "\nrefCls map keys=\n" + refCls.keySet());
            }
            s = c.schema.get();
            if (s == null) {
                throw new IllegalArgumentException("$ref not found: " + ref);
            }
        }
        return s;
    }

    public static final class Method {
        public final String methodName;
        public final List<Param> parameters;
        public final Optional<String> returnFullClassName; // arrays always wrapped ?
        public final String path;
        public final HttpMethod httpMethod;
        public final Optional<String> statusCode; // can be 2XX, 3XX, ..
        public final List<String> consumes;
        public final List<String> produces;
        public final Optional<String> description;
        public final Optional<String> primaryStatusCode;
        public final Optional<String> primaryMediaType;
        public final List<ResponseDescriptor> responseDescriptors;
        public final boolean includeForServerGeneration;

        Method(String methodName, Optional<String> statusCode, List<Param> parameters,
                Optional<String> returnFullClassName, String path, HttpMethod httpMethod, List<String> consumes,
                List<String> produces, Optional<String> description, Optional<String> primaryStatusCode,
                Optional<String> primaryMediaType, List<ResponseDescriptor> responseDescriptors, boolean ignoreForServerGeneration) {
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
            this.primaryMediaType = primaryMediaType;
            this.responseDescriptors = responseDescriptors;
            this.includeForServerGeneration = ignoreForServerGeneration;
        }

        public Optional<Integer> statusCodeFirstInRange() {
            return statusCode //
                    .map(x -> x.toUpperCase(Locale.ENGLISH)) //
                    .map(x -> {
                        if (x.endsWith("XX")) {
                            return Integer.parseInt(x.substring(0, 1)) * 100;
                        } else {
                            return Integer.parseInt(x);
                        }
                    });
        }
        
        @Override
        public String toString() {
            return "Method [path=" + path + ", httpMethod=" + httpMethod + ", methodName=" + methodName + ", returnCls="
                    + returnFullClassName.orElse("") + ", parameters="
                    + parameters.stream().map(Object::toString).map(x -> "\n    " + x).collect(Collectors.joining())
                    + ", ignoreForServerGeneration=" + includeForServerGeneration;
        }

    }

    public static final class ResponseDescriptor {
        private final String statusCode; // can be a pattern like `2*`
        private final String mediaType;
        private final String fullClassName;

        public ResponseDescriptor(String statusCode, String mediaType, String fullClassName) {
            this.statusCode = statusCode;
            this.mediaType = mediaType;
            this.fullClassName = fullClassName;
        }

        public String statusCode() {
            return statusCode;
        }

        public String mediaType() {
            return mediaType;
        }

        public String fullClassName() {
            return fullClassName;
        }

    }

    public static final class Constraints {
        public final Optional<Integer> minLength;
        public final Optional<Integer> maxLength;
        public final Optional<BigDecimal> min;
        public final Optional<BigDecimal> max;
        public final Optional<BigDecimal> minExclusive;
        public final Optional<BigDecimal> maxExclusive;
        public final Optional<Integer> minItems;
        public final Optional<Integer> maxItems;
        public final Optional<String> pattern;

        public Constraints(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<BigDecimal> min,
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

        public static Constraints empty() {
            return new Constraints(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        public boolean atLeastOnePresent() {
            return minLength.isPresent() || maxLength.isPresent() || min.isPresent() || max.isPresent()
                    || minExclusive.isPresent() || maxExclusive.isPresent() || minItems.isPresent()
                    || maxItems.isPresent() || pattern.isPresent();
        }

    }

    public static final class Param {
        public final String name;
        public final String identifier;
        public final Optional<Object> defaultValue;
        public final boolean required;
        public final String fullClassName;
        public final boolean isArray;
        public final boolean isRequestBody;
        public final Constraints constraints;
        public final ParamType type;
        public final boolean isComplexQueryParameter;
        public final Optional<String> description;
        public final Optional<String> contentType;
        public final Optional<String> filename;

        public Param(String name, String identifier, Optional<Object> defaultValue, boolean required,
                String fullClassName, boolean isArray, boolean isRequestBody, Constraints constraints, ParamType type,
                boolean isComplexQueryParameter, Optional<String> description, Optional<String> contentType, Optional<String> filename) {
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
            this.contentType = contentType;
            this.filename = filename;
        }

        @Override
        public String toString() {
            return "Param [" + identifier + ", name=" + name + ", defaultValue=" + defaultValue.orElse("") + ", required="
                    + required + ", cls=" + fullClassName + ", isArray=" + isArray + ", contentType="
                    + contentType.orElse("") + ", filename=" + filename.orElse("") + ", desc='" + description.orElse("") + "']";
                            
        }
    }

}
