package org.davidmoten.oa3.codegen.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.concurrent.NotThreadSafe;

import org.davidmoten.oa3.codegen.generator.Generator.Cls;
import org.davidmoten.oa3.codegen.generator.Generator.MyVisitor;
import org.davidmoten.oa3.codegen.generator.Generator.MyVisitor.Result;
import org.davidmoten.oa3.codegen.generator.internal.ImmutableList;
import org.davidmoten.oa3.codegen.generator.internal.Util;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

@NotThreadSafe
public class SpringBootGenerator {

    private final Names names;
    private Map<String, Cls> refCls;

    public SpringBootGenerator(Names names) {
        this.names = names;
    }

    public void generate() {
        // make a ref map
        Map<String, Cls> refCls = new HashMap<String, Cls>();
        MyVisitor v = new MyVisitor(names);
        Apis.visitSchemas(names.api(), v);
        for (Result result : v.results()) {
            Cls c = result.cls;
            if (c.topLevel) {
                String refPrefix = c.category.refPrefix();
                String ref = refPrefix + c.name;
                refCls.put(ref, c);
            }
        }
        this.refCls = refCls;

        // want a method per path, operation combo
        List<Method> methods = new ArrayList<>();
        // TODO handle path $ref
        names.api().getPaths().forEach((pathName, pathItem) -> {
            gatherMethods(pathName, pathItem, methods);
        });
        methods.forEach(System.out::println);
    }

    private void gatherMethods(String pathName, PathItem pathItem, List<Method> methods) {
        // TODO pathItem.get$ref();
        pathItem.readOperationsMap() //
                .forEach((method, operation) -> gatherMethods(pathName, method, operation, methods));
    }

    private void gatherMethods(String pathName, HttpMethod method, Operation operation, List<Method> methods) {
        String methodName = Names.toIdentifier(ImmutableList.of(pathName, method.toString()));
        List<Param> params = new ArrayList<>();
        Class<?> returnCls = String.class;
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
                                    p.getRequired(), cls, isArray));
                        }
                        // TODO handle object schema and explode
                        // TODO handle refs
                        // TODO complex schemas?
                    });
        }
        Method m = new Method(methodName, params, returnCls, pathName, method);
        methods.add(m);
    }

    private static final class Method {
        final String methodName;
        final List<Param> parameters;
        final Class<?> returnCls;
        final String path;
        final HttpMethod httpMethod;

        Method(String methodName, List<Param> parameters, Class<?> returnCls, String path, HttpMethod httpMethod) {
            this.methodName = methodName;
            this.parameters = parameters;
            this.returnCls = returnCls;
            this.path = path;
            this.httpMethod = httpMethod;
        }

        @Override
        public String toString() {
            return "Method [path=" + path + ", httpMethod=" + httpMethod + ", methodName=" + methodName + ", returnCls="
                    + returnCls + ", parameters="
                    + parameters.stream().map(Object::toString).map(x -> "\n    " + x).collect(Collectors.joining())
                    + "]";
        }

    }

    private static final class Param {
        final String name;
        final String identifier;
        final Optional<Object> defaultValue;
        final boolean required;
        final Class<?> cls;
        final boolean isArray;

        Param(String name, String identifier, Optional<Object> defaultValue, boolean required, Class<?> cls,
                boolean isArray) {
            this.name = name;
            this.identifier = identifier;
            this.defaultValue = defaultValue;
            this.required = required;
            this.cls = cls;
            this.isArray = isArray;
        }

        @Override
        public String toString() {
            return "Param [" + identifier + ", name=" + name + ", defaultValue=" + defaultValue
                    + ", required=" + required + ", cls=" + cls + ", isArray=" + isArray + "]";
        }
    }

    private void handleParameter(ImmutableList<String> list, Parameter parameter) {
//      parameter.get$ref();
        System.out.println(list.add(parameter.getName()));
        if (parameter.getContent() != null) {
            parameter.getContent().forEach((mimeType, mediaType) -> {
                System.out.println(mimeType + ": " + mediaType);
            });
        }
        if (parameter.getSchema() != null) {
            System.out.println(parameter.getSchema());
        }
    }

}
