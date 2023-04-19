package org.davidmoten.oa3.codegen.generator;

import java.io.PrintStream;
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
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.generator.internal.Util;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class SpringBootGenerator {

    private final Names names;
    private final Map<String, Cls> refCls;
    private final Map<Schema<?>, Cls> schemaCls;

    public SpringBootGenerator(Names names) {
        this.names = names;
        // make a ref map
        Map<String, Cls> refCls = new HashMap<String, Cls>();
        Map<Schema<?>, Cls> schemaCls = new HashMap<Schema<?>, Cls>();
        MyVisitor v = new MyVisitor(names);
        Apis.visitSchemas(names.api(), v);
        for (Result result : v.results()) {
            Cls c = result.cls;
            if (c.topLevel) {
                String refPrefix = c.category.refPrefix();
                String ref = refPrefix + c.name;
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
        methods.forEach(System.out::println);

        Imports imports = new Imports("Api");
        PrintStream out = System.out;
        Indent indent = new Indent();
        out.println("\npublic interface Api {\n");
        indent.right();
        methods.forEach(m -> {
            indent.right().right();
            String params = m.parameters.stream()
                    .map(p -> String.format("\n%s@%s(name = \"%s\") %s %s",
                            indent,
                            imports.add("org.springframework.web.bind.annotation.RequestParam"),
                            p.name,
                            toImportedType(p, imports), p.identifier))
                    .collect(Collectors.joining());
            indent.left().left();
            out.format("%s %s %s(%s);\n", indent, imports.add(m.returnFullClassName), m.methodName, params);
        });
        indent.left();
        out.println("}");
    }

    private static String toImportedType(Param p, Imports imports) {
        if (p.isArray) {
            return String.format("%s<%s>", imports.add(List.class), imports.add(p.fullClassName));
        } else if (p.required) {
            return imports.add(p.fullClassName);
        } else {
            return String.format("%s<%s>", imports.add(Optional.class), imports.add(p.fullClassName));
        }
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
        String returnFullClassName = Void.class.getCanonicalName();
        if (operation.getParameters() != null) {
            operation.getParameters() //
                    .forEach(p -> {
                        // TODO p.ref$
                        boolean isArray = false;
                        Schema<?> s = p.getSchema();
                        if (Util.isArray(s)) {
                            isArray = true;
                            s = s.getItems();
                        }
                        // handle simple schemas
                        if (s != null && Util.isPrimitive(s)) {
                            Class<?> cls = Util.toClass(s.getType(), s.getFormat(), names.mapIntegerToBigInteger());
                            Optional<Object> defaultValue = Optional.ofNullable(s.getDefault());
                            params.add(new Param(p.getName(), Names.toIdentifier(p.getName()), defaultValue,
                                    p.getRequired(), cls.getCanonicalName(), isArray));
                        }
                        // TODO handle object schema and explode
                        // TODO handle refs
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
                        String fullClassName = schemaCls.get(schema).fullClassName;
                        params.add(new Param("requestBody", "requestBody",
                                Optional.ofNullable((Object) schema.getDefault()), true, fullClassName, false));
                    } else {
                        // TODO unexpected?
                    }
                }
            } else {
                // for each other request mimeType
//                params.add(new Param("requestBody", "requestBody", Optional.ofNullable((Object) schema.getDefault()),
//                        true, byte[].class.getCanonicalName(), false));
            }
        }
        ApiResponse response = operation.getResponses().get("200");
        if (response != null) {
            if (response.getContent() != null) {
                MediaType mediaType = response.getContent().get("application/json");
                if (mediaType != null) {
                    returnFullClassName = schemaCls.get(mediaType.getSchema()).fullClassName;
                }
            }
            // TODO handle other mediaTypes
        }
        Method m = new Method(methodName, params, returnFullClassName, pathName, method);
        methods.add(m);
    }

    private static final class Method {
        final String methodName;
        final List<Param> parameters;
        final String returnFullClassName; // arrays always wrapped ?
        final String path;
        final HttpMethod httpMethod;

        Method(String methodName, List<Param> parameters, String returnFullClassName, String path,
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
                    + returnFullClassName + ", parameters="
                    + parameters.stream().map(Object::toString).map(x -> "\n    " + x).collect(Collectors.joining())
                    + "]";
        }

    }

    private static final class Param {
        final String name;
        final String identifier;
        final Optional<Object> defaultValue;
        final boolean required;
        final String fullClassName;
        final boolean isArray;

        Param(String name, String identifier, Optional<Object> defaultValue, boolean required, String fullClassName,
                boolean isArray) {
            this.name = name;
            this.identifier = identifier;
            this.defaultValue = defaultValue;
            this.required = required;
            this.fullClassName = fullClassName;
            this.isArray = isArray;
        }

        @Override
        public String toString() {
            return "Param [" + identifier + ", name=" + name + ", defaultValue=" + defaultValue + ", required="
                    + required + ", cls=" + fullClassName + ", isArray=" + isArray + "]";
        }
    }

}
