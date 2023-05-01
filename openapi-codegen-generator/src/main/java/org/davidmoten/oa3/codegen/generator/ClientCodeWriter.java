package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.generator.WriterUtil.closeParen;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.SpringBootGenerator.Method;
import org.davidmoten.oa3.codegen.generator.internal.ByteArrayPrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.http.Http;
import org.davidmoten.oa3.codegen.http.HttpMethod;
import org.davidmoten.oa3.codegen.http.HttpResponse;
import org.davidmoten.oa3.codegen.http.Serializer;

public class ClientCodeWriter {

    public static void writeClientClass(Names names, List<Method> methods) {
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        String fullClassName = names.clientFullClassName();
        Imports imports = new Imports(fullClassName);
        writeClientClass(out, names, imports, fullClassName, methods);
        WriterUtil.writeContent(names, out, fullClassName, imports);
    }

    private static void writeClientClass(PrintWriter out, Names names, Imports imports, String fullClassName,
            List<Method> methods) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(fullClassName));
        out.format("\n%s", WriterUtil.IMPORTS_HERE);
        WriterUtil.writeApiJavadoc(out, names, indent);
        out.format("\npublic class %s {\n", Names.simpleClassName(fullClassName));
        indent.right();
        writeClientClassFieldsAndConstructor(out, imports, fullClassName, indent);
        writeClientClassMethods(out, imports, methods, indent);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeClientClassFieldsAndConstructor(PrintWriter out, Imports imports, String fullClassName,
            Indent indent) {
        // add fields
        out.format("\n%sprivate final %s serializer;\n", indent, imports.add(Serializer.class));
        out.format("%sprivate final %s basePath;\n", indent, imports.add(String.class));

        // add constructor
        out.format("\n%spublic %s(%s serializer, %s basePath) {\n", indent, Names.simpleClassName(fullClassName),
                imports.add(Serializer.class), imports.add(String.class));
        indent.right();
        out.format("%sthis.serializer = serializer;\n", indent);
        out.format("%sthis.basePath = basePath;\n", indent);
        closeParen(out, indent);
    }

    private static void writeClientClassMethods(PrintWriter out, Imports imports, List<Method> methods, Indent indent) {
        methods.forEach(m -> {
            SpringBootCodeWriter.writeMethodJavadoc(out, indent, m);
            indent.right().right();
            String params = m.parameters //
                    .stream() //
                    .map(p -> String.format("\n%s%s %s", indent, SpringBootCodeWriter.toImportedType(p, imports),
                            p.identifier)) //
                    .collect(Collectors.joining(", "));
            indent.left().left();
            final String importedReturnType;
            if (!m.returnFullClassName.isPresent()) {
                importedReturnType = "void";
            } else {
                importedReturnType = imports.add(m.returnFullClassName.get());
            }
            out.format("\n%spublic %s %s(%s) {\n", indent, importedReturnType, m.methodName, params);
            indent.right();
            out.format("%s// primary status code:  %s\n", indent, m.primaryStatusCode.orElse(-1));
            out.format("%s// primary media type:   %s\n", indent, m.primaryMediaType.orElse(""));
            out.format("%sthrow new %s();\n", indent, imports.add(UnsupportedOperationException.class));
            closeParen(out, indent);
            out.format("\n%spublic %s %sFullResponse(%s) {\n", indent, imports.add(HttpResponse.class), m.methodName,
                    params);
            indent.right();
            out.format("%sreturn %s\n", indent, imports.add(Http.class));
            indent.right().right();
            out.format("%s.method(%s.%s)\n", indent, imports.add(HttpMethod.class), m.httpMethod.name());
            out.format("%s.basePath(this.basePath)\n", indent);
            out.format("%s.path(\"%s\")\n", indent, m.path);
            out.format("%s.serializer(this.serializer)\n", indent);
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
//            return Http //
//                    .method(HttpMethod.POST) //
//                    .basePath("http://localhost:" + serverPort) //
//                    .path("/thing") //
//                    .serializer(serializer) //
//                    .acceptApplicationJson() //
//                    .body(new Thing("dave", 20)) //
//                    .contentTypeApplicationJson()//
//                    .responseAs(Thing.class) //
//                    .whenStatusCodeMatches("2XX") //
//                    .whenContentTypeMatches("application/json") //
//                    .responseAs(Problem.class) //
//                    .whenStatusCodeDefault() //
//                    .whenContentTypeMatches("application/json") //
//                    .call();
        });
    }

}
