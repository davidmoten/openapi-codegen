package org.davidmoten.oa3.codegen.generator.writer;

import static org.davidmoten.oa3.codegen.generator.internal.WriterUtil.IMPORTS_HERE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.Constraints;
import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.Method;
import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.Param;
import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.ParamType;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Javadoc;
import org.davidmoten.oa3.codegen.generator.internal.Util;
import org.davidmoten.oa3.codegen.generator.internal.WriterUtil;
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

public final class SpringBootServerCodeWriter {

    public static void writeServiceClasses(Names names, List<Method> methods) {
        writeApplicationClass(names);
        writeJacksonConfigurationClass(names);
        writeServiceControllerClass(names, methods);
        writeServiceInterfaceClass(names, methods);
    }

    private static void writeApplicationClass(Names names) {
        CodePrintWriter out = CodePrintWriter.create(names.applicationFullClassName());
        writeApplicationClass(out);
        WriterUtil.writeContent(names, out);
    }

    private static void writeApplicationClass(CodePrintWriter out) {
        out.line("package %s;", Names.pkg(out.fullClassName()));
        out.println();
        out.format("%s", IMPORTS_HERE);
        out.println();
        out.line("@%s", SpringBootApplication.class);
        String simpleClassName = Names.simpleClassName(out.fullClassName());
        out.line("public class %s {", simpleClassName);
        out.println();
        out.line("public static void main(%s[] args) {", String.class);
        out.line("%s.run(%s.class, args);", SpringApplication.class, simpleClassName);
        out.closeParen();
        out.left();
        out.println();
        out.line("}");
    }

    private static void writeJacksonConfigurationClass(Names names) {
        String fullClassName = names.jacksonConfigurationFullClassName();
        CodePrintWriter out = CodePrintWriter.create(fullClassName);
        writeJacksonConfigurationClass(out,  names );
        WriterUtil.writeContent(names, out);
    }

    private static void writeJacksonConfigurationClass(CodePrintWriter out, Names names
            ) {
        out.line("package %s;", Names.pkg(out.fullClassName()));
        out.println();
        out.format("%s", IMPORTS_HERE);
        out.println();
        out.line("@%s", Configuration.class);
        String simpleClassName = Names.simpleClassName(out.fullClassName());
        out.line("public class %s {", simpleClassName);
        out.println();
        out.line("private final %s config;", Config.class);
        out.println();
        out.line("public %s(@%s(required = false) %s config) {", simpleClassName, Autowired.class,
                Config.class);
        out.line("this.config = config == null ? %s.config() : config;", out.add(names.globalsFullClassName()));
        out.closeParen();
        out.println();
        out.line("@%s", Bean.class);
        out.line("@%s", Primary.class);
        out.line("public %s objectMapper() {", ObjectMapper.class);
        out.line("return config.mapper();");
        out.closeParen();
        out.left();
        out.println();
        out.line("}");
    }

    private static void writeServiceControllerClass(Names names, List<Method> methods) {
        String fullClassName = names.serviceControllerFullClassName();
        CodePrintWriter out = CodePrintWriter.create(fullClassName);
        writeServiceControllerClass(out,  names, methods);
        WriterUtil.writeContent(names, out);
    }

    private static void writeServiceInterfaceClass(Names names, List<Method> methods) {
        String fullClassName = names.serviceInterfaceFullClassName();
        CodePrintWriter out = CodePrintWriter.create(fullClassName);
        writeServiceInterfaceClass(out, names, methods);
        WriterUtil.writeContent(names, out);
    }

    private static void writeServiceInterfaceClass(CodePrintWriter out, Names names,
            List<Method> methods) {
        out.line("package %s;", Names.pkg(out.fullClassName()));
        out.println();
        out.line("%s", IMPORTS_HERE);
        WriterUtil.writeApiJavadoc(out, names);
        out.println();
        out.line("public interface %s extends %s {", Names.simpleClassName(out.fullClassName()),
                ErrorHandler.class);
        writeServiceMethods(out, methods, false, names);
        out.closeParen();
    }

    private static void writeServiceControllerClass(CodePrintWriter out, Names names,
            List<Method> methods) {
        out.line("package %s;", Names.pkg(out.fullClassName()));
        out.println();
        out.line("%s", IMPORTS_HERE);
        out.println();
        out.line("@%s", RestController.class);
        String simpleClassName = Names.simpleClassName(out.fullClassName());
        out.line("public class %s implements %s {", simpleClassName, ControllerExceptionHandler.class);
        out.println();
        out.line("private final %s service;", out.add(names.serviceInterfaceFullClassName()));
        out.println();
        out.line("public %s(@%s(required = false) %s service) {", simpleClassName, Autowired.class,
                out.add(names.serviceInterfaceFullClassName()));
        out.line("this.service = %s.orElse(service, new %s() {});",
                org.davidmoten.oa3.codegen.util.Util.class,
                out.add(names.serviceInterfaceFullClassName()));
        out.closeParen();
        writeServiceMethods(out, methods, true, names);
        out.closeParen();
    }

    private static void writeServiceMethods(CodePrintWriter out,  List<Method> methods,
            boolean isController, Names names) {
        methods.forEach(m -> {
            writeMethodJavadoc(out, m, m.primaryStatusCode.map(x -> "primary response status code " + x));
            out.right().right();
            String params = m.parameters.stream().map(p -> {
                if (p.isRequestBody) {
                    final String annotations;
                    if (isController) {
                        annotations = String.format("@%s ", out.add(RequestBody.class));
                    } else {
                        annotations = "";
                    }
                    return String.format("\n%s%s%s %s", out.indent(), annotations, toImportedType(p, out.imports()),
                            "requestBody");
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
                        annotations = String.format("@%s(name = \"%s\"%s%s) ", out.add(ann), p.name, defValue,
                                required);
                    } else {
                        annotations = "";
                    }
                    return String.format("\n%s%s%s %s", out.indent(), annotations, toImportedType(p, out.imports()),
                            p.identifier);
                }
            }).collect(Collectors.joining(", "));
            out.left().left();
            final String importedReturnType;
            if (isController) {
                importedReturnType = String.format("%s<?>", out.add(ResponseEntity.class));
            } else if (!m.returnFullClassName.isPresent()) {
                importedReturnType = "void";
            } else {
                importedReturnType = out.add(m.returnFullClassName.get());
            }
//            @RequestMapping(
//                    method = RequestMethod.POST,
//                    value = "/postWithRequestBodyNotRequired",
//                    produces = { "application/json" },
//                    consumes = { "application/json" }
//                )
            if (isController) {

                out.println();
                out.line("@%s(", RequestMapping.class);
                out.right();
                String consumes = m.consumes.stream().map(x -> "\"" + x + "\"").collect(Collectors.joining(", "));
                if (!consumes.isEmpty()) {
                    consumes = String.format(",\n%sconsumes = {%s}", out.indent(), consumes);
                }
                String produces = m.produces.stream().map(x -> "\"" + x + "\"").collect(Collectors.joining(", "));
                if (!produces.isEmpty()) {
                    produces = String.format(",\n%sproduces = {%s}", out.indent(), produces);
                }
                out.line("method = %s.%s,", RequestMethod.class, m.httpMethod);
                out.line("value = \"%s\"%s%s)", m.path, consumes, produces);
                out.left();
                out.line("public %s %s(%s) {", importedReturnType, m.methodName, params);
                out.line("try {");
                addValidationChecks(out, m, names);
                if (m.returnFullClassName.isPresent()) {
                    out.line("return %s.status(%s).body(service.%s(%s));", ResponseEntity.class, //
                            m.statusCode.get(), //
                            m.methodName, //
                            m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", ")));
                } else {
                    out.line("service.%s(%s);", m.methodName,
                            m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", ")));
                    out.line("return %s.status(%s).build();", ResponseEntity.class,
                            m.statusCode.orElse(200));
                }
                out.left();
                out.line("} catch (%s e) {", Throwable.class);
                out.line("return service.errorResponse(e);");
                out.closeParen();
                out.closeParen();
            } else {
                out.println();
                out.line("default %s %s(%s) throws %s {", importedReturnType, m.methodName, params,
                        ServiceException.class);
                out.line("throw notImplemented();");
                out.closeParen();
            }
        });
    }

    static void writeMethodJavadoc(CodePrintWriter out, Method m, Optional<String> returns) {
        Map<String, String> parameterDescriptions = m.parameters //
                .stream() //
                .collect(Collectors.toMap(x -> x.identifier,
                        x -> x.description.orElse(x.identifier).replaceAll("\\n\\s*", " ")));
        Optional<String> html = Optional.of(m.description.map(x -> WriterUtil.markdownToHtml(x))
                .orElse("<p>Returns response from call to path <i>%s</i>.</p>"));
        Javadoc.printJavadoc(out, html, Collections.emptyList(), Optional.empty(), returns,
                parameterDescriptions, true);
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

    private static void addValidationChecks(CodePrintWriter out, Method m, Names names) {
        m.parameters.forEach(p -> {
            Constraints x = p.constraints;
            if (x.atLeastOnePresent()) {
                out.line("if (%s.config().validateInControllerMethod().test(\"%s\")) {",
                        out.add(names.globalsFullClassName()), m.methodName);
                if (x.minLength.isPresent()) {
                    out.line("%s.checkMinLength(%s, %s, \"%s\");", RequestPreconditions.class,
                            p.identifier, x.minLength.get(), p.identifier);
                }
                if (x.maxLength.isPresent()) {
                    out.line("%s.checkMaxLength(%s, %s, \"%s\");", RequestPreconditions.class,
                            p.identifier, x.maxLength.get(), p.identifier);
                }
                if (x.pattern.isPresent()) {
                    out.line("%s.checkMatchesPattern(%s, \"%s\", \"%s\");", RequestPreconditions.class,
                            p.identifier, x.pattern.get(), p.identifier);
                }
                if (x.min.isPresent()) {
                    out.line("%s.checkMinimum(%s, \"%s\", \"%s\", %s);", RequestPreconditions.class,
                            p.identifier, x.min.get().toString(), p.identifier, false);
                }
                if (x.max.isPresent()) {
                    out.line("%s.checkMaximum(%s, \"%s\", \"%s\", %s);", RequestPreconditions.class,
                            p.identifier, x.max.get().toString(), p.identifier, false);
                }
                if (x.minExclusive.isPresent()) {
                    out.line("%s.checkMinimum(%s, \"%s\", \"%s\", %s);", RequestPreconditions.class,
                            p.identifier, x.minExclusive.get().toString(), p.identifier, true);
                }
                if (x.maxExclusive.isPresent()) {
                    out.line("%s.checkMaximum(%s, \"%s\", \"%s\", %s);", RequestPreconditions.class,
                            p.identifier, x.maxExclusive.get().toString(), p.identifier, true);
                }
                if (p.isArray && x.minItems.isPresent()) {
                    out.line("%s.checkMinSize(%s, %s, \"%s\");", RequestPreconditions.class, p.identifier,
                            x.minItems.get(), p.identifier);
                }
                if (p.isArray && x.maxItems.isPresent()) {
                    out.line("%s.checkMaxSize(%s, %s, \"%s\");", RequestPreconditions.class, p.identifier,
                            x.maxItems.get(), p.identifier);
                }
                out.closeParen();
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
