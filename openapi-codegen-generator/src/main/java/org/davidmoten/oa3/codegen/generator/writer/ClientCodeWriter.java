package org.davidmoten.oa3.codegen.generator.writer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.client.runtime.ClientBuilder;
import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.Method;
import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.ParamType;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.WriterUtil;
import org.davidmoten.oa3.codegen.http.Http;
import org.davidmoten.oa3.codegen.http.HttpMethod;
import org.davidmoten.oa3.codegen.http.HttpResponse;
import org.davidmoten.oa3.codegen.http.Interceptor;
import org.davidmoten.oa3.codegen.http.ParameterStyle;
import org.davidmoten.oa3.codegen.http.Serializer;
import org.davidmoten.oa3.codegen.http.service.HttpService;

public class ClientCodeWriter {

    public static void writeClientClass(Names names, List<Method> methods) {
        String fullClassName = names.clientFullClassName();
        CodePrintWriter out = CodePrintWriter.create(fullClassName);
        writeClientClass(out, names, methods);
        WriterUtil.writeContent(names, out);
    }

    private static void writeClientClass(CodePrintWriter out, Names names, List<Method> methods) {
        out.line("package %s;", out.pkg());
        out.println();
        out.format("%s", WriterUtil.IMPORTS_HERE);
        WriterUtil.writeApiJavadoc(out, names);
        WriterUtil.addGeneratedAnnotation(out);
        out.line("public class %s {", out.simpleClassName());
        writeClientClassFieldsConstructorAndBuilder(out, names);
        writeClientClassMethods(out, methods);
        writeCustomMethod(out);
        out.closeParen();
    }

    private static void writeClientClassFieldsConstructorAndBuilder(CodePrintWriter out, Names names) {
        // add fields
        out.println();
        out.line("private final %s serializer;", Serializer.class);
        out.line("private final %s<%s> interceptors;", List.class, Interceptor.class);
        out.line("private final %s basePath;", String.class);
        out.line("private final %s httpService;", HttpService.class);

        // write constructor
        out.println();
        out.line("private %s(%s serializer, %s<%s> interceptors, %s basePath, %s httpService) {", out.simpleClassName(),
                Serializer.class, List.class, Interceptor.class, String.class, HttpService.class);
        out.line("this.serializer = serializer;");
        out.line("this.interceptors = interceptors;");
        out.line("this.basePath = basePath;");
        out.line("this.httpService = httpService;");
        out.closeParen();

        // write builder
        out.println();
        out.line("public static %s<%s> basePath(%s basePath) {", ClientBuilder.class, out.simpleClassName(),
                String.class);
        out.line("return new %s<>(b -> new %s(b.serializer(), b.interceptors(), b.basePath(), b.httpService()), %s.config(), basePath);",
                ClientBuilder.class, out.simpleClassName(), out.add(names.globalsFullClassName()));
        out.closeParen();
    }

    private static final String FULL_RESPONSE_SUFFIX = "FullResponse";

    private static void writeClientClassMethods(CodePrintWriter out, List<Method> methods) {
        methods.forEach(m -> {
            out.right().right();
            String params = m.parameters //
                    .stream() //
                    .map(p -> String.format("\n%s%s %s", out.indent(),
                            ServerCodeWriterSpringBoot.toImportedType(p, out.imports()), p.identifier)) //
                    .collect(Collectors.joining(", "));
            out.left().left();
            final String importedReturnType;
            if (!m.returnFullClassName.isPresent()) {
                importedReturnType = "void";
            } else {
                importedReturnType = out.add(m.returnFullClassName.get());
            }
            if (m.primaryStatusCode.isPresent() && m.primaryMediaType.isPresent()) {
                final Optional<String> returns;
                if (m.returnFullClassName.isPresent()) {
                    returns = m.primaryStatusCode.map(x -> "primary response with status code " + x);
                } else {
                    returns = Optional.empty();
                }
                ServerCodeWriterSpringBoot.writeMethodJavadoc(out, m, returns);
                out.line("public %s %s(%s) {", importedReturnType, m.methodName, params);
                final String paramIdentifiers;
                if (m.parameters.size() <= 3) {
                    paramIdentifiers = m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", "));
                } else {
                    out.right().right().right();
                    paramIdentifiers = m.parameters.stream()
                            .map(p -> String.format("\n%s%s", out.indent(), p.identifier))
                            .collect(Collectors.joining(","));
                    out.left().left().left();
                }
                out.line("return %s%s(%s)", m.methodName, FULL_RESPONSE_SUFFIX, paramIdentifiers);
                out.right().right();
                out.line(".assertStatusCodeMatches(\"%s\")", m.primaryStatusCode.get());
                out.line(".assertContentTypeMatches(\"%s\")", m.primaryMediaType.get());
                out.line(".dataUnwrapped();");
                out.left().left();
                out.closeParen();
            }
            ServerCodeWriterSpringBoot.writeMethodJavadoc(out, m,
                    Optional.of("full response with status code, body and headers"));
            out.line("public %s %s%s(%s) {", HttpResponse.class, m.methodName, FULL_RESPONSE_SUFFIX, params);
            out.line("return %s", Http.class);
            out.right().right();
            out.line(".method(%s.%s)", HttpMethod.class, m.httpMethod.name());
            out.line(".basePath(this.basePath)");
            out.line(".path(\"%s\")", m.path);
            out.line(".serializer(this.serializer)");
            out.line(".interceptors(this.interceptors)");
            out.line(".httpService(this.httpService)");
            out.line(".acceptApplicationJson()");
            m.parameters.forEach(p -> {
                if (p.type == ParamType.QUERY) {
                    out.line(".queryParam(\"%s\", %s, %s.%s, %s)", p.name, p.identifier, ParameterStyle.class,
                            p.style.name(), p.explode);
                } else if (p.type == ParamType.PATH) {
                    out.line(".pathParam(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.BODY) {
                    out.line(".body(%s)", p.identifier);
                    out.line(".contentTypeApplicationJson()");
                } else if (p.type == ParamType.COOKIE) {
                    out.line(".cookie(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.HEADER) {
                    out.line(".header(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.MULTIPART_FORM_DATA) {
                    out.line(".multipartFormData(%s)", p.identifier);
                } else if (p.type == ParamType.FORM_URLENCODED) {
                    out.line(".formUrlEncoded(%s)", p.identifier);
                }
            });
            m.responseDescriptors.forEach(r -> {
                out.line(".responseAs(%s.class)", out.add(r.fullClassName()));
                out.line(".whenStatusCodeMatches(\"%s\")", r.statusCode());
                out.line(".whenContentTypeMatches(\"%s\")", r.mediaType());
            });
            out.line(".call();");
            out.left().left();
            out.closeParen();
        });
    }
    
    private static void writeCustomMethod(CodePrintWriter out) {
        out.println();
        out.line("public %s _custom(%s method, %s path) {" , Http.Builder.class, HttpMethod.class, String.class);
        out.line("return %s", Http.class);
        out.right().right();
        out.line(".method(method)");
        out.line(".basePath(this.basePath)");
        out.line(".path(path)");
        out.line(".serializer(this.serializer)");
        out.line(".httpService(this.httpService);");
        out.left().left();
        out.closeParen();
    }

}
