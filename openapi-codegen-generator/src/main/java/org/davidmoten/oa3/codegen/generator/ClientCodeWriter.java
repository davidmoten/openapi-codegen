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
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;

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
            out.format("\n%spublic %s %s(%s) throws %s {\n", indent, importedReturnType, m.methodName, params,
                    imports.add(ServiceException.class));
            indent.right();
            out.format("%sthrow new %s();\n", indent, imports.add(UnsupportedOperationException.class));
            closeParen(out, indent);
            out.format("\n%spublic %s %sWithInfo(%s) throws %s {\n", indent, imports.add(HttpResponse.class),
                    m.methodName, params, imports.add(ServiceException.class));
            indent.right();
            out.format("%sreturn %s\n", indent, imports.add(Http.class));
            indent.right().right();
            out.format("%s.method(%s.%s)\n", indent, imports.add(HttpMethod.class), m.httpMethod.name());
            out.format("%s.basePath(this.basePath)\n", indent);
            out.format("%s.path(\"%s\")\n", indent, m.path);
            out.format("%s.serializer(this.serializer)\n", indent);
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
