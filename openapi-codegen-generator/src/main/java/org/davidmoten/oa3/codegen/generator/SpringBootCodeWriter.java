package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.SpringBootGenerator.Constraints;
import org.davidmoten.oa3.codegen.generator.SpringBootGenerator.Method;
import org.davidmoten.oa3.codegen.generator.SpringBootGenerator.Param;
import org.davidmoten.oa3.codegen.generator.internal.ByteArrayPrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.spring.runtime.ErrorHandler;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SpringBootCodeWriter {

    private static final String IMPORTS_HERE = "IMPORTS_HERE";
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("debug", "false"));

    public static void writeServiceClasses(Names names, List<Method> methods) {
        writeApplicationClass(names);
        writeJacksonConfigurationClass(names);
        writeServiceControllerClass(names, methods);
        writeServiceInterfaceClass(names, methods);
    }

    private static void writeApplicationClass(Names names) {
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        String fullClassName = names.applicationFullClassName();
        Imports imports = new Imports(fullClassName);
        writeApplicationClass(out, imports, fullClassName);
        writeContent(names, out, fullClassName, imports);
    }

    private static void writeApplicationClass(ByteArrayPrintWriter out, Imports imports, String fullClassName) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(fullClassName));
        out.format("\n%s", IMPORTS_HERE);
        out.format("\n@%s\n", imports.add(SpringBootApplication.class));
        String simpleClassName = Names.simpleClassName(fullClassName);
        out.format("public class %s {\n", simpleClassName);
        indent.right();
        out.format("\n%spublic static void main(%s[] args) {\n", indent, imports.add(String.class));
        indent.right();
        out.format("%s%s.run(%s.class, args);\n", indent, imports.add(SpringApplication.class), simpleClassName);
        indent.left();
        out.format("%s}\n", indent);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeJacksonConfigurationClass(Names names) {
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        String fullClassName = names.jacksonConfigurationFullClassName();
        Imports imports = new Imports(fullClassName);
        writeJacksonConfigurationClass(out, imports, names, fullClassName);
        writeContent(names, out, fullClassName, imports);
    }

    private static void writeJacksonConfigurationClass(ByteArrayPrintWriter out, Imports imports, Names names,
            String fullClassName) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(fullClassName));
        out.format("\n%s", IMPORTS_HERE);
        out.format("\n@%s\n", imports.add(Configuration.class));
        String simpleClassName = Names.simpleClassName(fullClassName);
        out.format("public class %s {\n", simpleClassName);
        indent.right();
        out.format("\n%sprivate final %s config;\n", indent, imports.add(Config.class));
        out.format("\n%spublic %s(@%s(required = false) %s config) {\n", indent, simpleClassName,
                imports.add(Autowired.class), imports.add(Config.class));
        out.format("%sthis.config = config == null ? %s.config() : config;\n", indent.right(),
                imports.add(names.globalsFullClassName()));
        out.format("%s}\n", indent.left());
        out.format("\n%s@%s\n", indent, imports.add(Bean.class));
        out.format("%s@%s\n", indent, imports.add(Primary.class));
        out.format("%spublic %s objectMapper() {\n", indent, imports.add(ObjectMapper.class));
        indent.right();
        out.format("%sreturn config.mapper();\n", indent);
        indent.left();
        out.format("%s}\n", indent);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeServiceControllerClass(Names names, List<Method> methods) {
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        String fullClassName = names.serviceControllerFullClassName();
        Imports imports = new Imports(fullClassName);
        writeServiceControllerClass(out, imports, names, methods, fullClassName);
        writeContent(names, out, fullClassName, imports);
    }

    private static void writeServiceInterfaceClass(Names names, List<Method> methods) {
        String fullClassName = names.serviceInterfaceFullClassName();
        ByteArrayPrintWriter out = ByteArrayPrintWriter.create();
        Imports imports = new Imports(fullClassName);
        writeServiceInterfaceClass(out, imports, names, methods);
        writeContent(names, out, fullClassName, imports);
    }

    private static void writeServiceInterfaceClass(ByteArrayPrintWriter out, Imports imports, Names names,
            List<Method> methods) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(names.serviceControllerFullClassName()));
        out.format("\n%s", IMPORTS_HERE);
        out.format("\npublic interface %s extends %s {\n", Names.simpleClassName(names.serviceInterfaceFullClassName()),
                imports.add(ErrorHandler.class));
        indent.right();
        writeServiceMethods(out, imports, methods, indent, false);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeServiceControllerClass(ByteArrayPrintWriter out, Imports imports, Names names,
            List<Method> methods, String fullClassName) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(fullClassName));
        out.format("\n%s", IMPORTS_HERE);
        out.format("\n@%s\n", imports.add(RestController.class));
        String simpleClassName = Names.simpleClassName(fullClassName);
        out.format("public class %s {\n", simpleClassName);
        indent.right();
        out.format("\n%sprivate final %s service;\n", indent, imports.add(names.serviceInterfaceFullClassName()));
        out.format("\n%spublic %s(@%s(required = false) %s service) {\n", indent, simpleClassName,
                imports.add(Autowired.class), imports.add(names.serviceInterfaceFullClassName()));
        out.format("%sthis.service = %s.orElse(service, new %s() {});\n", indent.right(),
                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Util.class),
                imports.add(names.serviceInterfaceFullClassName()));
        out.format("%s}\n", indent.left());
        writeServiceMethods(out, imports, methods, indent, true);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeServiceMethods(ByteArrayPrintWriter out, Imports imports, List<Method> methods,
            Indent indent, boolean isController) {
        methods.forEach(m -> {
            indent.right().right();
            String params = m.parameters.stream().map(p -> {
                if (p.isRequestBody) {
                    final String annotations;
                    if (isController) {
                        annotations = String.format("@%s ", imports.add(RequestBody.class));
                    } else {
                        annotations = "";
                    }
                    return String.format("\n%s%s%s %s", indent, annotations, toImportedType(p, imports), "requestBody");
                } else {
                    final String annotations;
                    if (isController) {
                        String defValue = p.defaultValue.map(x -> ", defaultValue = \"" + x + "\"").orElse("");
                        String required = ", required = " + p.required;
                        annotations = String.format("@%s(name = \"%s\"%s%s) ", imports.add(RequestParam.class), p.name,
                                defValue, required);
                    } else {
                        annotations = "";
                    }
                    return String.format("\n%s%s%s %s", indent, annotations, toImportedType(p, imports), p.identifier);
                }
            }).collect(Collectors.joining(", "));
            indent.left().left();
            final String importedReturnType;
            if (isController) {
                importedReturnType = String.format("%s<?>", imports.add(ResponseEntity.class));
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
                
                out.format("\n%s@%s(\n", indent, imports.add(RequestMapping.class));
                indent.right();
                String consumes = m.consumes.stream().map(x -> "\"" + x +"\"").collect(Collectors.joining(", "));
                if (!consumes.isEmpty()) {
                    consumes = String.format(",\n%sconsumes = {%s}", indent, consumes);
                }
                String produces = m.produces.stream().map(x -> "\"" + x +"\"").collect(Collectors.joining(", "));
                if (!produces.isEmpty()) {
                    produces = String.format(",\n%sproduces = {%s}", indent, produces);
                }
                out.format("%smethod = %s.%s,\n", indent, imports.add(RequestMethod.class), m.httpMethod);
                out.format("%svalue = \"%s\"%s%s)\n", indent, m.path, consumes, produces);
                indent.left();
                out.format("%spublic %s %s(%s) {\n", indent, importedReturnType, m.methodName, params);
                indent.right();
                out.format("%stry {\n", indent);
                indent.right();
                m.parameters.forEach(p -> {
                    Constraints x = p.constraints;
                    if (x.minLength.isPresent()) {
                        out.format("%s%s.checkMinLength(%s, %s, \"%s\");\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                p.identifier, x.minLength.get(), p.identifier);
                    }
                    if (x.maxLength.isPresent()) {
                        out.format("%s%s.checkMaxLength(%s, %s, \"%s\");\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                p.identifier, x.maxLength.get(), p.identifier);
                    }
                    if (x.pattern.isPresent()) {
                        out.format("%s%s.checkMatchesPattern(%s, \"%s\", \"%s\");\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                p.identifier, x.pattern.get(), p.identifier);
                    }
                    if (x.min.isPresent()) {
                        out.format("%s%s.checkMinimum(%s, \"%s\", \"%s\", %s);\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                p.identifier, x.min.get().toString(), p.identifier, false);
                    }
                    if (x.max.isPresent()) {
                        out.format("%s%s.checkMaximum(%s, \"%s\", \"%s\", %s);\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                p.identifier, x.max.get().toString(), p.identifier, false);
                    }
                    if (x.minExclusive.isPresent()) {
                        out.format("%s%s.checkMinimum(%s, \"%s\", \"%s\", %s);\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                p.identifier, x.minExclusive.get().toString(), p.identifier, true);
                    }
                    if (x.maxExclusive.isPresent()) {
                        out.format("%s%s.checkMaximum(%s, \"%s\", \"%s\", %s);\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                p.identifier, x.maxExclusive.get().toString(), p.identifier, true);
                    }
                    if (p.isArray && x.minItems.isPresent()) {
                        out.format("%s%s.checkMinSize(%s, %s, \"%s\");\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                p.identifier, x.minItems.get(), p.identifier);
                    }
                    if (p.isArray && x.maxItems.isPresent()) {
                        out.format("%s%s.checkMaxSize(%s, %s, \"%s\");\n", indent,
                                imports.add(org.davidmoten.oa3.codegen.runtime.internal.Preconditions.class),
                                p.identifier, x.maxItems.get(), p.identifier);
                    }
                });
                if (m.returnFullClassName.isPresent()) {
                    out.format("%sreturn %s.status(%s).body(service.%s(%s));\n", indent,
                            imports.add(ResponseEntity.class), //
                            m.statusCode.get(), //
                            m.methodName, //
                            m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", ")));
                } else {
                    out.format("%sservice.%s(%s);\n", indent, m.methodName,
                            m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", ")));
                    out.format("%sreturn %s.status(%s).build();\n", indent, imports.add(ResponseEntity.class),
                            m.statusCode.orElse(200));
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
                out.format("\n%sdefault %s %s(%s) throws %s {\n", indent, importedReturnType, m.methodName, params,
                        imports.add(ServiceException.class));
                out.format("%sthrow notImplemented();\n", indent.right());
                out.format("%s}\n", indent.left());
            }
        });
    }

    private static void writeContent(Names names, ByteArrayPrintWriter out, String fullClassName, Imports imports) {
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

    private static String toImportedType(Param p, Imports imports) {
        if (p.isArray) {
            return String.format("%s<%s>", imports.add(List.class), imports.add(p.fullClassName));
        } else if (p.required || p.defaultValue.isPresent()) {
            return imports.add(Util.toPrimitive(p.fullClassName));
        } else {
            return String.format("%s<%s>", imports.add(Optional.class), imports.add(p.fullClassName));
        }
    }

}
