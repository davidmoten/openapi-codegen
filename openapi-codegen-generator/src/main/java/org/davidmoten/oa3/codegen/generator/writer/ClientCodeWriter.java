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
import org.davidmoten.oa3.codegen.http.Serializer;

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
        out.line("public class %s {", out.simpleClassName());
        writeClientClassFieldsConstructorAndBuilder(out, names);
        writeClientClassMethods(out, methods);
        out.closeParen();
    }

    private static void writeClientClassFieldsConstructorAndBuilder(CodePrintWriter out, Names names) {
        // add fields
        out.println();
        out.line("private final %s serializer;", Serializer.class);
        out.line("private final %s interceptor;", Interceptor.class);
        out.line("private final %s basePath;", String.class);

        // write constructor
        out.println();
        out.line("private %s(%s serializer, %s interceptor, %s basePath) {", out.simpleClassName(),
                Serializer.class, Interceptor.class, String.class);
        out.line("this.serializer = serializer;");
        out.line("this.interceptor = interceptor;");
        out.line("this.basePath = basePath;");
        out.closeParen();

        // write builder
        out.println();
        out.line("public static %s<%s> basePath(%s basePath) {", ClientBuilder.class,
                out.simpleClassName(), String.class);
        out.line("return new %s<>(b -> new %s(b.serializer(), b.interceptor(), b.basePath()), %s.config(), basePath);",
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
                            SpringBootServerCodeWriter.toImportedType(p, out.imports()), p.identifier)) //
                    .collect(Collectors.joining(", "));
            out.left().left();
            final String importedReturnType;
            if (!m.returnFullClassName.isPresent()) {
                importedReturnType = "void";
            } else {
                importedReturnType = out.add(m.returnFullClassName.get());
            }
            if (m.primaryStatusCode.isPresent() && m.primaryMediaType.isPresent()) {
                SpringBootServerCodeWriter.writeMethodJavadoc(out, m,
                        m.primaryStatusCode.map(x -> "primary response with status code " + x));
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
            SpringBootServerCodeWriter.writeMethodJavadoc(out, m,
                    Optional.of("full response with status code, body and headers"));
            out.line("public %s %s%s(%s) {", HttpResponse.class, m.methodName, FULL_RESPONSE_SUFFIX, params);
            out.line("return %s", Http.class);
            out.right().right();
            out.line(".method(%s.%s)", HttpMethod.class, m.httpMethod.name());
            out.line(".basePath(this.basePath)");
            out.line(".path(\"%s\")", m.path);
            out.line(".serializer(this.serializer)");
            out.line(".interceptor(this.interceptor)");
            out.line(".acceptApplicationJson()");
            m.parameters.forEach(p -> {
                if (p.type == ParamType.QUERY) {
                    out.line(".queryParam(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.PATH) {
                    out.line(".pathParam(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.BODY) {
                    out.line(".body(%s)", p.identifier);
                    out.line(".contentTypeApplicationJson()");
                } else if (p.type == ParamType.COOKIE) {
                    out.line(".cookie(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.HEADER) {
                    out.line(".header(\"%s\", %s)", p.name, p.identifier);
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

}
