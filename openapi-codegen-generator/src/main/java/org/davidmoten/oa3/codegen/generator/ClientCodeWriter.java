package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.generator.WriterUtil.closeParen;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.davidmoten.oa3.codegen.generator.SpringBootGenerator.Method;
import org.davidmoten.oa3.codegen.generator.internal.ByteArrayPrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.generator.internal.Javadoc;
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
        String text = Stream.of( //
                Optional.ofNullable(names.api().getInfo().getTitle()), //
                Optional.ofNullable(names.api().getInfo().getSummary()), //
                Optional.ofNullable(names.api().getInfo().getDescription())) //
                .filter(Optional::isPresent) //
                .map(Optional::get) //
                .collect(Collectors.joining("\n\n"));
        Javadoc.printJavadoc(out, indent, WriterUtil.markdownToHtml(text), true);
        out.format("\npublic class %s {\n", Names.simpleClassName(fullClassName));
        indent.right();
        writeServiceMethods(out, imports, methods, indent, false, names);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeServiceMethods(PrintWriter out, Imports imports, List<Method> methods,
            Indent indent, boolean isController, Names names) {
        methods.forEach(m -> {
            Map<String, String> parameterDescriptions = m.parameters //
                    .stream() //
                    .collect(Collectors.toMap(x -> x.identifier,
                            x -> x.description.orElse(x.identifier).replaceAll("\\n\\s*", " ")));
            Javadoc.printJavadoc(out, indent, m.description.map(x -> WriterUtil.markdownToHtml(x)),
                    Collections.emptyList(), Optional.empty(), m.primaryStatusCode.map(x -> "status code " + x),
                    parameterDescriptions, true);
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
            out.format("\n%sdefault %s %s(%s) throws %s {\n", indent, importedReturnType, m.methodName, params,
                    imports.add(ServiceException.class));
            out.format("%sthrow notImplemented();\n", indent.right());
            closeParen(out, indent);
        });
    }

}
