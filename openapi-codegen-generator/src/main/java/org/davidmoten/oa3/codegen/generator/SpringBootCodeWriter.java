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
import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.spring.runtime.ErrorHandler;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class SpringBootCodeWriter {

    private static final String IMPORTS_HERE = "IMPORTS_HERE";
    private static final boolean DEBUG = true;

    private static final String SPRING_REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String SPRING_REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
    private static final String SPRING_REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    private static final String SPRING_REQUEST_METHOD = "org.springframework.web.bind.annotation.RequestMethod";
    private static final String SPRING_REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
    private static final String SPRING_RESPONSE_ENTITY = "org.springframework.http.ResponseEntity";

    public static void writeServiceClasses(Names names, List<Method> methods) {
        writeServiceControllerClass(names, methods);
        writeServiceInterfaceClass(names, methods);
    }

    private static void writeServiceControllerClass(Names names, List<Method> methods) {
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        Imports imports = new Imports(names.serviceControllerFullClassName());
        writeServiceControllerClass(out, imports, names, methods);
        String content = out.text().replace(IMPORTS_HERE, imports.toString());
        if (DEBUG) {
            System.out.println("////////////////////////////////////////////////");
            System.out.println(content);
        }
        out.close();
        File file = names.fullClassNameToJavaFile(names.serviceControllerFullClassName());
        file.getParentFile().mkdirs();
        try {
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeServiceInterfaceClass(Names names, List<Method> methods) {
        String fullClassName = names.serviceInterfaceFullClassName();
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        Imports imports = new Imports(fullClassName);
        writeServiceInterfaceClass(out, imports, names, methods);
        String content = out.text().replace(IMPORTS_HERE, imports.toString());
        if (DEBUG) {
            System.out.println("////////////////////////////////////////////////");
            System.out.println(content);
        }
        out.close();
        File file = names.fullClassNameToJavaFile(fullClassName);
        file.getParentFile().mkdirs();
        try {
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeServiceInterfaceClass(ByteArrayPrintWriter out, Imports imports, Names names,
            List<Method> methods) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(names.serviceControllerFullClassName()));
        out.format("\n%s", IMPORTS_HERE);
        out.format("\npublic interface %s extends %s {\n", Names.simpleClassName(names.serviceInterfaceFullClassName()),
                imports.add(ErrorHandler.class));
        indent.right();
        writeMethods(out, imports, methods, indent, false);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeServiceControllerClass(ByteArrayPrintWriter out, Imports imports, Names names,
            List<Method> methods) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(names.serviceControllerFullClassName()));
        out.format("\n%s", IMPORTS_HERE);
        out.format("\n@%s\n", imports.add(SPRING_REST_CONTROLLER));
        String simpleClassName = Names.simpleClassName(names.serviceControllerFullClassName());
        out.format("public class %s {\n", simpleClassName);
        indent.right();
        out.format("\n%sprivate final %s service;\n", indent, imports.add(names.serviceInterfaceFullClassName()));
        out.format("\n%spublic %s(@%s(required = false) %s service) {\n", indent, simpleClassName,
                imports.add(Autowired.class), imports.add(names.serviceInterfaceFullClassName()));
        out.format("%sthis.service = %s.orElse(service, new %s() {});\n", indent.right(),
                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Util.class),
                imports.add(names.serviceInterfaceFullClassName()));
        out.format("%s}\n", indent.left());
        writeMethods(out, imports, methods, indent, true);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeMethods(ByteArrayPrintWriter out, Imports imports, List<Method> methods, Indent indent,
            boolean isController) {
        methods.forEach(m -> {
            indent.right().right();
            String params = m.parameters.stream().map(p -> {
                if (p.isRequestBody) {
                    final String annotations;
                    if (isController) {
                        annotations = String.format("@%s ", imports.add(SPRING_REQUEST_BODY));
                    } else {
                        annotations = "";
                    }
                    return String.format("\n%s%s%s %s", indent, annotations, toImportedType(p, imports), "requestBody");
                } else {
                    final String annotations;
                    if (isController) {
                        annotations = String.format("@%s(name = \"%s\") ", imports.add(SPRING_REQUEST_PARAM), p.name);
                    } else {
                        annotations = "";
                    }
                    return String.format("\n%s%s%s %s", indent, annotations, toImportedType(p, imports), p.identifier);
                }
            }).collect(Collectors.joining(", "));
            indent.left().left();
            final String importedReturnType;
            if (isController) {
                importedReturnType = String.format("%s<?>", imports.add(SPRING_RESPONSE_ENTITY));
            } else if (!m.returnFullClassName.isPresent()) {
                importedReturnType = "void";
            } else {
                importedReturnType = imports.add(m.returnFullClassName.get());
            }
//            @RequestMapping(
//                    method = RequestMethod.POST,
//                    value = "/postWithRequestBodyNotRequired",
//                    produces = { "application/json" },
//                    consumes = { "application/json" }
//                )
            if (isController) {
                out.format("\n%s@%s(\n", indent, imports.add(SPRING_REQUEST_MAPPING));
                indent.right();
                out.format("%smethod = %s.%s,\n", indent, imports.add(SPRING_REQUEST_METHOD), m.httpMethod);
                out.format("%svalue = \"%s\")\n", indent, m.path);
                indent.left();
                out.format("%spublic %s %s(%s) {\n", indent, importedReturnType, m.methodName, params);
                indent.right();
                out.format("%stry {\n", indent);
                indent.right();
                if (m.returnFullClassName.isPresent()) {
                    out.format("%sreturn %s.ok(service.%s(%s));\n", indent, imports.add(ResponseEntity.class),
                            m.methodName,
                            m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", ")));
                } else {
                    out.format("%sservice.%s(%s);\n", indent, m.methodName,
                            m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", ")));
                    out.format("%sreturn %s.ok();\n", indent, imports.add(ResponseEntity.class));
                }
                indent.left();
                out.format("%s} catch (%s e) {\n", indent, imports.add(Throwable.class));
                indent.right();
                out.format("%sreturn service.errorResponse(e);\n", indent);
                indent.left();
                out.format("%s}\n", indent);
                indent.left();
                out.format("%s}\n", indent);
            } else {
                out.format("\n%sdefault %s %s(%s) {\n", indent, importedReturnType, m.methodName, params);
                out.format("%sthrow new %s(501, \"Not implemented\");\n", indent.right(),
                        imports.add(ServiceException.class));
                out.format("%s}\n", indent.left());
            }
        });
    }

    private static String toImportedType(Param p, Imports imports) {
        if (p.isArray) {
            return String.format("%s<%s>", imports.add(List.class), imports.add(p.fullClassName));
        } else if (p.required) {
            return imports.add(Util.toPrimitive(p.fullClassName));
        } else {
            return String.format("%s<%s>", imports.add(Optional.class), imports.add(p.fullClassName));
        }
    }

}
