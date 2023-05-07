package org.davidmoten.oa3.codegen.generator.writer;

import static org.davidmoten.oa3.codegen.generator.internal.WriterUtil.closeParen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.client.runtime.ClientBuilder;
import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.Method;
import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.ParamType;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.generator.internal.WriterUtil;
import org.davidmoten.oa3.codegen.http.Http;
import org.davidmoten.oa3.codegen.http.HttpMethod;
import org.davidmoten.oa3.codegen.http.HttpResponse;
import org.davidmoten.oa3.codegen.http.Interceptor;
import org.davidmoten.oa3.codegen.http.Serializer;

public class ClientCodeWriter {

    public static void writeClientClass(Names names, List<Method> methods) {
        CodePrintWriter out = CodePrintWriter.create();
        String fullClassName = names.clientFullClassName();
        Imports imports = new Imports(fullClassName);
        writeClientClass(out, names, imports, fullClassName, methods);
        WriterUtil.writeContent(names, out, fullClassName, imports);
    }

    private static void writeClientClass(CodePrintWriter out, Names names, Imports imports, String fullClassName,
            List<Method> methods) {
        out.line("package %s;", Names.pkg(fullClassName));
        out.println();
        out.line("%s", WriterUtil.IMPORTS_HERE);
        WriterUtil.writeApiJavadoc(out, names, out.indent());
        out.println();
        out.line("public class %s {", Names.simpleClassName(fullClassName));
        writeClientClassFieldsAndConstructor(out, imports, fullClassName, names);
        writeClientClassMethods(out, imports, methods, out.indent());
        out.closeParen();
    }

    private static void writeClientClassFieldsAndConstructor(CodePrintWriter out, Imports imports, String fullClassName,
            Names names) {
        // add fields
        out.println();
        out.line("private final %s serializer;", imports.add(Serializer.class));
        out.line("private final %s interceptor;", imports.add(Interceptor.class));
        out.line("private final %s basePath;", imports.add(String.class));

        // add constructor
        out.line("private %s(%s serializer, %s interceptor, %s basePath) {", Names.simpleClassName(fullClassName),
                imports.add(Serializer.class), imports.add(Interceptor.class), imports.add(String.class));
        out.line("this.serializer = serializer;");
        out.line("this.interceptor = interceptor;");
        out.line("this.basePath = basePath;");
        out.closeParen();

        out.println();
        out.line("public static %s<%s> basePath(%s basePath) {", imports.add(ClientBuilder.class),
                Names.simpleClassName(fullClassName), imports.add(String.class));
        out.line("return new %s<>(b -> new %s(b.serializer(), b.interceptor(), b.basePath()), %s.config(), basePath);",
                imports.add(ClientBuilder.class), Names.simpleClassName(fullClassName),
                imports.add(names.globalsFullClassName()));
        out.closeParen();
    }

    private static final String FULL_RESPONSE_SUFFIX = "FullResponse";

    private static void writeClientClassMethods(CodePrintWriter out, Imports imports, List<Method> methods,
            Indent indent) {
        methods.forEach(m -> {
            indent.right().right();
            String params = m.parameters //
                    .stream() //
                    .map(p -> String.format("\n%s%s %s", indent, SpringBootServerCodeWriter.toImportedType(p, imports),
                            p.identifier)) //
                    .collect(Collectors.joining(", "));
            indent.left().left();
            final String importedReturnType;
            if (!m.returnFullClassName.isPresent()) {
                importedReturnType = "void";
            } else {
                importedReturnType = imports.add(m.returnFullClassName.get());
            }
            if (m.primaryStatusCode.isPresent() && m.primaryMediaType.isPresent()) {
                SpringBootServerCodeWriter.writeMethodJavadoc(out, indent, m,
                        m.primaryStatusCode.map(x -> "primary response with status code " + x));
                out.format("\n%spublic %s %s(%s) {\n", indent, importedReturnType, m.methodName, params);
                indent.right();
                final String paramIdentifiers;
                if (m.parameters.size() <= 3) {
                    paramIdentifiers = m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", "));
                } else {
                    indent.right().right().right();
                    paramIdentifiers = m.parameters.stream().map(p -> String.format("\n%s%s", indent, p.identifier))
                            .collect(Collectors.joining(","));
                    indent.left().left().left();
                }
                out.format("%sreturn %s%s(%s)\n", indent, m.methodName, FULL_RESPONSE_SUFFIX, paramIdentifiers);
                indent.right().right();
                out.format("%s.assertStatusCodeMatches(\"%s\")\n", indent, m.primaryStatusCode.get());
                out.format("%s.assertContentTypeMatches(\"%s\")\n", indent, m.primaryMediaType.get());
                out.format("%s.dataUnwrapped();\n", indent);
                indent.left().left();
                closeParen(out, indent);
            }
            SpringBootServerCodeWriter.writeMethodJavadoc(out, indent, m,
                    Optional.of("full response with status code, body and headers"));
            out.format("\n%spublic %s %s%s(%s) {\n", indent, imports.add(HttpResponse.class), m.methodName,
                    FULL_RESPONSE_SUFFIX, params);
            indent.right();
            out.format("%sreturn %s\n", indent, imports.add(Http.class));
            indent.right().right();
            out.format("%s.method(%s.%s)\n", indent, imports.add(HttpMethod.class), m.httpMethod.name());
            out.format("%s.basePath(this.basePath)\n", indent);
            out.format("%s.path(\"%s\")\n", indent, m.path);
            out.format("%s.serializer(this.serializer)\n", indent);
            out.format("%s.interceptor(this.interceptor)\n", indent);
            out.format("%s.acceptApplicationJson()\n", indent);
            m.parameters.forEach(p -> {
                if (p.type == ParamType.QUERY) {
                    out.format("%s.queryParam(\"%s\", %s)\n", indent, p.name, p.identifier);
                } else if (p.type == ParamType.PATH) {
                    out.format("%s.pathParam(\"%s\", %s)\n", indent, p.name, p.identifier);
                } else if (p.type == ParamType.BODY) {
                    out.format("%s.body(%s)\n", indent, p.identifier);
                    out.format("%s.contentTypeApplicationJson()\n", indent);
                } else if (p.type == ParamType.COOKIE) {
                    out.format("%s.cookie(\"%s\", %s)\n", indent, p.name, p.identifier);
                } else if (p.type == ParamType.HEADER) {
                    out.format("%s.header(\"%s\", %s)\n", indent, p.name, p.identifier);
                }
            });
            m.responseDescriptors.forEach(r -> {
                out.format("%s.responseAs(%s.class)\n", indent, imports.add(r.fullClassName()));
                out.format("%s.whenStatusCodeMatches(\"%s\")\n", indent, r.statusCode());
                out.format("%s.whenContentTypeMatches(\"%s\")\n", indent, r.mediaType());
            });
            out.format("%s.call();\n", indent);
            indent.left().left();
            closeParen(out, indent);
        });
    }

}
