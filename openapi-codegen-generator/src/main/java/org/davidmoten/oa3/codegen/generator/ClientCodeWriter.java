package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.generator.WriterUtil.closeParen;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.SpringBootGenerator.Method;
import org.davidmoten.oa3.codegen.generator.internal.ByteArrayPrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
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
        writeServiceMethods(out, imports, methods, indent);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeServiceMethods(PrintWriter out, Imports imports, List<Method> methods, Indent indent) {
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
            out.format("%sthrow new %s();\n", indent.right(), imports.add(UnsupportedOperationException.class));
            closeParen(out, indent);
        });
    }

}
