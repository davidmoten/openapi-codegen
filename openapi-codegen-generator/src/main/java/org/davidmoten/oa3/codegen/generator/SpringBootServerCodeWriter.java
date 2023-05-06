package org.davidmoten.oa3.codegen.generator;

import static org.davidmoten.oa3.codegen.generator.WriterUtil.IMPORTS_HERE;
import static org.davidmoten.oa3.codegen.generator.WriterUtil.closeParen;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.Constraints;
import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.Method;
import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.Param;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.generator.internal.Javadoc;
import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.spring.runtime.ControllerExceptionHandler;
import org.davidmoten.oa3.codegen.spring.runtime.ErrorHandler;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.davidmoten.oa3.codegen.spring.runtime.internal.RequestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

class SpringBootServerCodeWriter {

    static void writeServiceClasses(Names names, List<Method> methods) {
        writeApplicationClass(names);
        writeJacksonConfigurationClass(names);
        writeServiceControllerClass(names, methods);
        writeServiceInterfaceClass(names, methods);
    }

    private static void writeApplicationClass(Names names) {
        CodePrintWriter out = CodePrintWriter.create();
        String fullClassName = names.applicationFullClassName();
        Imports imports = new Imports(fullClassName);
        writeApplicationClass(out, imports, fullClassName);
        WriterUtil.writeContent(names, out, fullClassName, imports);
    }

    private static void writeApplicationClass(CodePrintWriter out, Imports imports, String fullClassName) {
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
        closeParen(out, indent);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeJacksonConfigurationClass(Names names) {
        CodePrintWriter out = CodePrintWriter.create();
        String fullClassName = names.jacksonConfigurationFullClassName();
        Imports imports = new Imports(fullClassName);
        writeJacksonConfigurationClass(out, imports, names, fullClassName);
        WriterUtil.writeContent(names, out, fullClassName, imports);
    }

    private static void writeJacksonConfigurationClass(CodePrintWriter out, Imports imports, Names names,
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
        closeParen(out, indent);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeServiceControllerClass(Names names, List<Method> methods) {
        CodePrintWriter out = CodePrintWriter.create();
        String fullClassName = names.serviceControllerFullClassName();
        Imports imports = new Imports(fullClassName);
        writeServiceControllerClass(out, imports, names, methods, fullClassName);
        WriterUtil.writeContent(names, out, fullClassName, imports);
    }

    private static void writeServiceInterfaceClass(Names names, List<Method> methods) {
        String fullClassName = names.serviceInterfaceFullClassName();
        CodePrintWriter out = CodePrintWriter.create();
        Imports imports = new Imports(fullClassName);
        writeServiceInterfaceClass(out, imports, names, methods);
        WriterUtil.writeContent(names, out, fullClassName, imports);
    }

    private static void writeServiceInterfaceClass(CodePrintWriter out, Imports imports, Names names,
            List<Method> methods) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(names.serviceControllerFullClassName()));
        out.format("\n%s", IMPORTS_HERE);
        WriterUtil.writeApiJavadoc(out, names, indent);
        out.format("\npublic interface %s extends %s {\n", Names.simpleClassName(names.serviceInterfaceFullClassName()),
                imports.add(ErrorHandler.class));
        indent.right();
        writeServiceMethods(out, imports, methods, indent, false, names);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeServiceControllerClass(CodePrintWriter out, Imports imports, Names names,
            List<Method> methods, String fullClassName) {
        Indent indent = new Indent();
        out.format("package %s;\n", Names.pkg(fullClassName));
        out.format("\n%s", IMPORTS_HERE);
        out.format("\n@%s\n", imports.add(RestController.class));
        String simpleClassName = Names.simpleClassName(fullClassName);
        out.format("public class %s implements %s {\n", simpleClassName, imports.add(ControllerExceptionHandler.class));
        indent.right();
        out.format("\n%sprivate final %s service;\n", indent, imports.add(names.serviceInterfaceFullClassName()));
        out.format("\n%spublic %s(@%s(required = false) %s service) {\n", indent, simpleClassName,
                imports.add(Autowired.class), imports.add(names.serviceInterfaceFullClassName()));
        out.format("%sthis.service = %s.orElse(service, new %s() {});\n", indent.right(),
                imports.add(org.davidmoten.oa3.codegen.util.Util.class),
                imports.add(names.serviceInterfaceFullClassName()));
        closeParen(out, indent);
        writeServiceMethods(out, imports, methods, indent, true, names);
        indent.left();
        out.println("\n}\n");
    }

    private static void writeServiceMethods(CodePrintWriter out, Imports imports, List<Method> methods,
            Indent indent, boolean isController, Names names) {
        methods.forEach(m -> {
            writeMethodJavadoc(out, indent, m, m.primaryStatusCode.map(x -> "primary response status code " + x));
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
                        Class<?> ann = annotation(p.type);
                        if (ann.equals(RequestParam.class) && p.isComplexQueryParameter) {
                            ann = ModelAttribute.class;
                            required = "";
                            defValue = "";
                        }
                        annotations = String.format("@%s(name = \"%s\"%s%s) ", imports.add(ann), p.name, defValue,
                                required);
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
                String consumes = m.consumes.stream().map(x -> "\"" + x + "\"").collect(Collectors.joining(", "));
                if (!consumes.isEmpty()) {
                    consumes = String.format(",\n%sconsumes = {%s}", indent, consumes);
                }
                String produces = m.produces.stream().map(x -> "\"" + x + "\"").collect(Collectors.joining(", "));
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
                addValidationChecks(out, imports, indent, m, names);
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
                closeParen(out, indent);
                closeParen(out, indent);
            } else {
                out.format("\n%sdefault %s %s(%s) throws %s {\n", indent, importedReturnType, m.methodName, params,
                        imports.add(ServiceException.class));
                out.format("%sthrow notImplemented();\n", indent.right());
                closeParen(out, indent);
            }
        });
    }

    static void writeMethodJavadoc(PrintWriter out, Indent indent, Method m, Optional<String> returns) {
        Map<String, String> parameterDescriptions = m.parameters //
                .stream() //
                .collect(Collectors.toMap(x -> x.identifier,
                        x -> x.description.orElse(x.identifier).replaceAll("\\n\\s*", " ")));
        Optional<String> html = Optional.of(m.description.map(x -> WriterUtil.markdownToHtml(x))
                .orElse("<p>Returns response from call to path <i>%s</i>.</p>"));
        Javadoc.printJavadoc(out, indent, html, Collections.emptyList(), Optional.empty(),
                returns, parameterDescriptions, true);
    }

    private static Class<?> annotation(ParamType t) {
        if (t == ParamType.BODY) {
            return RequestBody.class;
        } else if (t == ParamType.QUERY) {
            return RequestParam.class;
        } else if (t == ParamType.PATH) {
            return PathVariable.class;
        } else if (t == ParamType.HEADER) {
            return RequestHeader.class;
        } else if (t == ParamType.COOKIE) {
            return CookieValue.class;
        } else {
            throw new IllegalArgumentException("unexpected " + t);
        }
    }

    private static void addValidationChecks(CodePrintWriter out, Imports imports, Indent indent, Method m,
            Names names) {
        m.parameters.forEach(p -> {
            Constraints x = p.constraints;
            if (x.atLeastOnePresent()) {
                out.format("%sif (%s.config().validateInControllerMethod().test(\"%s\")) {\n", indent,
                        imports.add(names.globalsFullClassName()), m.methodName);
                indent.right();
                if (x.minLength.isPresent()) {
                    out.format("%s%s.checkMinLength(%s, %s, \"%s\");\n", indent,
                            imports.add(RequestPreconditions.class), p.identifier, x.minLength.get(), p.identifier);
                }
                if (x.maxLength.isPresent()) {
                    out.format("%s%s.checkMaxLength(%s, %s, \"%s\");\n", indent,
                            imports.add(RequestPreconditions.class), p.identifier, x.maxLength.get(), p.identifier);
                }
                if (x.pattern.isPresent()) {
                    out.format("%s%s.checkMatchesPattern(%s, \"%s\", \"%s\");\n", indent,
                            imports.add(RequestPreconditions.class), p.identifier, x.pattern.get(), p.identifier);
                }
                if (x.min.isPresent()) {
                    out.format("%s%s.checkMinimum(%s, \"%s\", \"%s\", %s);\n", indent,
                            imports.add(RequestPreconditions.class), p.identifier, x.min.get().toString(), p.identifier,
                            false);
                }
                if (x.max.isPresent()) {
                    out.format("%s%s.checkMaximum(%s, \"%s\", \"%s\", %s);\n", indent,
                            imports.add(RequestPreconditions.class), p.identifier, x.max.get().toString(), p.identifier,
                            false);
                }
                if (x.minExclusive.isPresent()) {
                    out.format("%s%s.checkMinimum(%s, \"%s\", \"%s\", %s);\n", indent,
                            imports.add(RequestPreconditions.class), p.identifier, x.minExclusive.get().toString(),
                            p.identifier, true);
                }
                if (x.maxExclusive.isPresent()) {
                    out.format("%s%s.checkMaximum(%s, \"%s\", \"%s\", %s);\n", indent,
                            imports.add(RequestPreconditions.class), p.identifier, x.maxExclusive.get().toString(),
                            p.identifier, true);
                }
                if (p.isArray && x.minItems.isPresent()) {
                    out.format("%s%s.checkMinSize(%s, %s, \"%s\");\n", indent, imports.add(RequestPreconditions.class),
                            p.identifier, x.minItems.get(), p.identifier);
                }
                if (p.isArray && x.maxItems.isPresent()) {
                    out.format("%s%s.checkMaxSize(%s, %s, \"%s\");\n", indent, imports.add(RequestPreconditions.class),
                            p.identifier, x.maxItems.get(), p.identifier);
                }
                closeParen(out, indent);
            }
        });
    }

    static String toImportedType(Param p, Imports imports) {
        if (p.isArray) {
            if (p.required) {
                return String.format("%s<%s>", imports.add(List.class), imports.add(p.fullClassName));
            } else {
                return String.format("%s<%s<%s>>", imports.add(Optional.class), imports.add(List.class),
                        imports.add(p.fullClassName));
            }
        } else if (p.required || p.defaultValue.isPresent()) {
            return imports.add(Util.toPrimitive(p.fullClassName));
        } else {
            return String.format("%s<%s>", imports.add(Optional.class), imports.add(p.fullClassName));
        }
    }

}
