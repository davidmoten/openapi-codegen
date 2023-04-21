package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.runtime.internal.Util.orElse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.Generator.Cls;
import org.davidmoten.oa3.codegen.generator.Generator.MyVisitor;
import org.davidmoten.oa3.codegen.generator.Generator.MyVisitor.Result;
import org.davidmoten.oa3.codegen.generator.internal.ImmutableList;
import org.davidmoten.oa3.codegen.generator.internal.Util;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

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
    }

    private void gatherMethods(String pathName, PathItem pathItem, List<Method> methods) {
        // TODO pathItem.get$ref();
        pathItem.readOperationsMap() //
                .forEach((method, operation) -> gatherMethods(pathName, method, operation, methods));
    }

    private void gatherMethods(String pathName, HttpMethod method, Operation operation, List<Method> methods) {
        String methodName = Names
                .toIdentifier(ImmutableList.of(pathName, method.toString().toLowerCase(Locale.ENGLISH)));
        List<Param> params = new ArrayList<>();
        Optional<String> returnFullClassName = Optional.empty();
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
                        // handle simple schemas
                        if (Util.isPrimitive(s)) {
                            Class<?> cls = Util.toClass(s.getType(), s.getFormat(), names.mapIntegerToBigInteger());
                            Optional<Object> defaultValue = Optional.ofNullable(s.getDefault());
                            params.add(new Param(p.getName(), Names.toIdentifier(p.getName()), defaultValue,
                                    p.getRequired(), cls.getCanonicalName(), isArray, false));
                        }
                        // TODO handle object schema and explode
                        // TODO complex schemas?
                    });
        }
        if (operation.getRequestBody() != null) {
            RequestBody b = operation.getRequestBody();
            // TODO handle ref
            MediaType mediaType = b.getContent().get("application/json");
            if (mediaType != null) {
                Schema<?> schema = mediaType.getSchema();
                if (schema != null) {
                    if (schemaCls.get(schema) != null) {
                        String fullClassName = resolveRefsFullClassName(schema);
                        params.add(new Param("requestBody", "requestBody",
                                Optional.ofNullable((Object) schema.getDefault()), orElse(b.getRequired(), false),
                                fullClassName, false, true));
                    } else {
                        throw new RuntimeException("unexpected");
                    }
                }
            }
//            else {
//                // for each other request mimeType
////                params.add(new Param("requestBody", "requestBody", Optional.ofNullable((Object) schema.getDefault()),
////                        true, byte[].class.getCanonicalName(), false));
//                
//            }
        }
        ApiResponse response = operation.getResponses().get("200");
        if (response != null) {
            if (response.getContent() != null) {
                MediaType mediaType = response.getContent().get("application/json");
                if (mediaType != null) {
                    returnFullClassName = Optional.of(resolveRefsFullClassName(mediaType.getSchema()));
                }
            } else {
                System.out.println("TODO handle response ref");
            }
            // TODO handle other mediaTypes
        }
        Method m = new Method(methodName, params, returnFullClassName, pathName, method);
        methods.add(m);
    }

    private Parameter resolveParameterRefs(Parameter p) {
        while (p.get$ref() != null) {
            p = names.lookupParameter(p.get$ref());
        }
        return p;
    }

    private static String lastComponent(String ref) {
        int i = ref.lastIndexOf('/');
        return ref.substring(i + 1);
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

    public static final class Method {
        final String methodName;
        final List<Param> parameters;
        final Optional<String> returnFullClassName; // arrays always wrapped ?
        final String path;
        final HttpMethod httpMethod;

        Method(String methodName, List<Param> parameters, Optional<String> returnFullClassName, String path,
                HttpMethod httpMethod) {
            this.methodName = methodName;
            this.parameters = parameters;
            this.returnFullClassName = returnFullClassName;
            this.path = path;
            this.httpMethod = httpMethod;
        }

        @Override
        public String toString() {
            return "Method [path=" + path + ", httpMethod=" + httpMethod + ", methodName=" + methodName + ", returnCls="
                    + returnFullClassName.orElse("") + ", parameters="
                    + parameters.stream().map(Object::toString).map(x -> "\n    " + x).collect(Collectors.joining())
                    + "]";
        }

    }

    public static final class Param {
        final String name;
        final String identifier;
        final Optional<Object> defaultValue;
        final boolean required;
        final String fullClassName;
        final boolean isArray;
        final boolean isRequestBody;

        Param(String name, String identifier, Optional<Object> defaultValue, boolean required, String fullClassName,
                boolean isArray, boolean isRequestBody) {
            this.name = name;
            this.identifier = identifier;
            this.defaultValue = defaultValue;
            this.required = required;
            this.fullClassName = fullClassName;
            this.isArray = isArray;
            this.isRequestBody = isRequestBody;
        }

        @Override
        public String toString() {
            return "Param [" + identifier + ", name=" + name + ", defaultValue=" + defaultValue + ", required="
                    + required + ", cls=" + fullClassName + ", isArray=" + isArray + "]";
        }
    }

}
