package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.SpringBootGenerator.Method;
import org.davidmoten.oa3.codegen.generator.SpringBootGenerator.Param;
import org.davidmoten.oa3.codegen.generator.internal.ByteArrayPrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;

public class SpringBootCodeWriter {

    private static final String IMPORTS_HERE = "IMPORTS_HERE";
    private static final boolean DEBUG = true;
    
    private static final String SPRING_REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String SPRING_REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
    private static final String SPRING_REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    private static final String SPRING_REQUEST_METHOD = "org.springframework.web.bind.annotation.RequestMethod";
    private static final String SPRING_RESPONSE_ENTITY = "org.springframework.http.ResponseEntity";

    static void writeServiceClass(Names names, List<Method> methods) {
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        Imports imports = new Imports(names.serviceFullClassName());
        writeServiceClass(out, imports, names, methods);
        String content = out.text().replace(IMPORTS_HERE, imports.toString());
        if (DEBUG) {
            System.out.println("////////////////////////////////////////////////");
            System.out.println(content);
        }
        out.close();
        File file = names.fullClassNameToJavaFile(names.serviceFullClassName());
        file.getParentFile().mkdirs();
        try {
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeServiceClass(ByteArrayPrintWriter out, Imports imports, Names names,
            List<Method> methods) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(names.serviceFullClassName()));
        out.format("\n%s", IMPORTS_HERE);
        out.format("\npublic interface %s {", Names.simpleClassName(names.serviceFullClassName()));
        indent.right();
        methods.forEach(m -> {
            indent.right().right();
            String params = m.parameters.stream().map(p -> {
                if (p.isRequestBody) {
                    return String.format("\n%s@%s %s %s", indent, imports.add(SPRING_REQUEST_BODY),
                            toImportedType(p, imports), "requestBody");
                } else {
                    return String.format("\n%s@%s(name = \"%s\") %s %s", indent, imports.add(SPRING_REQUEST_PARAM),
                            p.name, toImportedType(p, imports), p.identifier);
                }
            }).collect(Collectors.joining(", "));
            indent.left().left();
            final String importedReturnType;
            if (!m.returnFullClassName.isPresent()) {
                importedReturnType = "void";
            } else {
                importedReturnType = String.format("%s<%s>", imports.add(SPRING_RESPONSE_ENTITY),
                        imports.add(m.returnFullClassName.get()));
            }
//            @RequestMapping(
//                    method = RequestMethod.POST,
//                    value = "/postWithRequestBodyNotRequired",
//                    produces = { "application/json" },
//                    consumes = { "application/json" }
//                )
            out.format("\n\n%s@%s(\n", indent, imports.add(SPRING_REQUEST_MAPPING));
            indent.right();
            out.format("%smethod = %s.%s,\n", indent, imports.add(SPRING_REQUEST_METHOD), m.httpMethod);
            out.format("%svalue = \"%s\"\n", indent, m.path);
            indent.left();
            out.format("%s)\n", indent);
            out.format("%s%s %s(%s);", indent, importedReturnType, m.methodName, params);
        });
        indent.left();
        out.println("\n}\n");
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
}
