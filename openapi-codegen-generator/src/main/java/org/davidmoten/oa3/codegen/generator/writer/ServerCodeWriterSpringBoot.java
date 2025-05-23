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
import org.davidmoten.oa3.codegen.spring.runtime.RequestPreconditions;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
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
import com.github.davidmoten.guavamini.Maps;

public final class ServerCodeWriterSpringBoot {

    public static void writeServiceClasses(Names names, List<Method> methods) {
        writeApplicationClass(names);
        writeJacksonConfigurationClass(names);
        List<Method> includedMethods = methods //
                .stream() //
                .filter(x-> x.includeForServerGeneration) //
                .collect(Collectors.toList());
        writeServiceControllerClass(names, includedMethods);
        writeServiceInterfaceClass(names, includedMethods);
    }

    private static void writeApplicationClass(Names names) {
        CodePrintWriter out = CodePrintWriter.create(names.applicationFullClassName(), names.simpleNameInPackage(names.applicationFullClassName()));
        writeApplicationClass(out);
        WriterUtil.writeContent(names, out);
    }

    private static void writeApplicationClass(CodePrintWriter out) {
        out.line("package %s;", out.pkg());
        out.println();
        out.format("%s", IMPORTS_HERE);
        out.println();
        out.line("@%s", SpringBootApplication.class);
        WriterUtil.addGeneratedAnnotation(out);
        out.line("public class %s {", out.simpleClassName());
        out.println();
        out.line("public static void main(%s[] args) {", String.class);
        out.line("%s.run(%s.class, args);", SpringApplication.class, out.simpleClassName());
        out.closeParen();
        out.left();
        out.println();
        out.line("}");
    }

    private static void writeJacksonConfigurationClass(Names names) {
        String fullClassName = names.jacksonConfigurationFullClassName();
        CodePrintWriter out = CodePrintWriter.create(fullClassName, names.simpleNameInPackage(fullClassName));
        writeJacksonConfigurationClass(out, names);
        WriterUtil.writeContent(names, out);
    }

    private static void writeJacksonConfigurationClass(CodePrintWriter out, Names names) {
        out.line("package %s;", out.pkg());
        out.println();
        out.format("%s", IMPORTS_HERE);
        out.println();
        out.line("@%s", Configuration.class);
        WriterUtil.addGeneratedAnnotation(out);
        out.line("public class %s {", out.simpleClassName());
        out.println();
        out.line("private final %s config;", Config.class);
        out.println();
        out.line("public %s(@%s(required = false) %s config) {", out.simpleClassName(), Autowired.class, Config.class);
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
        CodePrintWriter out = CodePrintWriter.create(fullClassName, names.simpleNameInPackage(fullClassName));
        writeServiceControllerClass(out, names, methods);
        WriterUtil.writeContent(names, out);
    }

    private static void writeServiceInterfaceClass(Names names, List<Method> methods) {
        String fullClassName = names.serviceInterfaceFullClassName();
        CodePrintWriter out = CodePrintWriter.create(fullClassName, names.simpleNameInPackage(fullClassName));
        writeServiceInterfaceClass(out, names, methods);
        WriterUtil.writeContent(names, out);
    }

    private static void writeServiceInterfaceClass(CodePrintWriter out, Names names, List<Method> methods) {
        out.line("package %s;", out.pkg());
        out.println();
        out.format("%s", IMPORTS_HERE);
        WriterUtil.writeApiJavadoc(out, names);
        WriterUtil.addGeneratedAnnotation(out);
        out.line("public interface %s extends %s {", out.simpleClassName(), ErrorHandler.class);
        out.println();
        out.line("Service DEFAULT = new Service() {};");
        writeServiceMethods(out, methods, false, names);
        out.closeParen();
    }

    private static void writeServiceControllerClass(CodePrintWriter out, Names names, List<Method> methods) {
        out.line("package %s;", out.pkg());
        out.println();
        out.format("%s", IMPORTS_HERE);
        out.println();
        out.line("@%s", RestController.class);
        WriterUtil.addGeneratedAnnotation(out);
        out.line("public class %s implements %s {", out.simpleClassName(), ControllerExceptionHandler.class);
        out.println();
        out.line("private final %s service;", out.add(names.serviceInterfaceFullClassName()));
        out.println();
        out.line("public %s(@%s(required = false) %s service) {", out.simpleClassName(), Autowired.class,
                out.add(names.serviceInterfaceFullClassName()));
        out.line("this.service = %s.orElse(service, %s.DEFAULT);", org.davidmoten.oa3.codegen.util.Util.class,
                out.add(names.serviceInterfaceFullClassName()));
        out.closeParen();
        writeServiceMethods(out, methods, true, names);
        out.closeParen();
    }

    private static void writeServiceMethods(CodePrintWriter out, List<Method> methods, boolean isController,
            Names names) {
        methods.forEach(m -> {
            final Optional<String> returns;
            if (m.returnFullClassName.isPresent()) {
                returns = m.primaryStatusCode.map(x -> "primary response status code " + x);
            } else {
                returns = Optional.empty();
            }
            writeMethodJavadoc(out, m, returns, Maps.empty());
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
                final String fullClassName;
                if (m.returnFullClassName.orElse("").equals(byte[].class.getCanonicalName())) {
                    fullClassName = Resource.class.getCanonicalName();
                } else {
                    fullClassName = m.returnFullClassName.get();
                }
                importedReturnType = out.add(fullClassName);
            }
//            @RequestMapping(
//                    method = RequestMethod.POST,
//                    value = "/postWithRequestBodyNotRequired",
//                    produces = { "application/json" },
//                    consumes = { "application/json" }
//                )
            if (isController) {
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
                            m.statusCodeFirstInRange().orElse(200), //
                            m.methodName, //
                            m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", ")));
                } else {
                    out.line("service.%s(%s);", m.methodName,
                            m.parameters.stream().map(p -> p.identifier).collect(Collectors.joining(", ")));
                    out.line("return %s.status(%s).build();", ResponseEntity.class, m.statusCodeFirstInRange().orElse(200));
                }
                out.left();
                out.line("} catch (%s e) {", Throwable.class);
                out.line("return service.errorResponse(e);");
                out.closeParen();
                out.closeParen();
            } else {
                out.line("default %s %s(%s) throws %s {", importedReturnType, m.methodName, params,
                        ServiceException.class);
                out.line("throw notImplemented();");
                out.closeParen();
            }
        });
    }

    static void writeMethodJavadoc(CodePrintWriter out, Method m, Optional<String> returns, Map<String, String> throwing) {
        Map<String, String> parameterDescriptions = m.parameters //
                .stream() //
                .collect(Collectors.toMap(x -> x.identifier,
                        x -> WriterUtil.markdownToHtml(x.description.orElse(x.identifier))));
        String html = m.description.map(WriterUtil::markdownToHtml)
                .orElse("<p>Returns response from call to path <i>%s</i>.</p>");
        String more = m.responseDescriptors.stream() //
                .map(rd -> {
                    String full = rd.fullClassName();
                    String link = Util.isPrimitiveFullClassName(full) || full.equals("byte[]") ? full
                            : String.format("{@link %s}", out.add(full));
                    return String.format("\n<p>[status=%s, %s] --&gt; %s</p>", rd.statusCode(), rd.mediaType(), link);
                }).collect(Collectors.joining());
        if (!Javadoc.printJavadoc(out, Optional.of(html + more), Collections.emptyList(), Optional.empty(), returns,
                parameterDescriptions, true, throwing)) {
            out.println();
        }
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
        boolean found = m.parameters.stream().anyMatch(p -> p.constraints.atLeastOnePresent());
        if (found) {
            out.line("if (%s.config().validateInControllerMethod().test(\"%s\")) {",
                    out.add(names.globalsFullClassName()), m.methodName);
            m.parameters.forEach(p -> {
                Constraints x = p.constraints;
                if (x.atLeastOnePresent()) {
                    x.minLength.ifPresent(integer -> out.line("%s.checkMinLength(%s, %s, \"%s\");", RequestPreconditions.class, p.identifier,
                            integer, p.identifier));
                    x.maxLength.ifPresent(integer -> out.line("%s.checkMaxLength(%s, %s, \"%s\");", RequestPreconditions.class, p.identifier,
                            integer, p.identifier));
                    x.pattern.ifPresent(s -> out.line("%s.checkMatchesPattern(%s, \"%s\", \"%s\");", RequestPreconditions.class,
                            p.identifier, WriterUtil.escapePattern(s), p.identifier));
                    x.min.ifPresent(bigDecimal -> out.line("%s.checkMinimum(%s, \"%s\", \"%s\", %s);", RequestPreconditions.class, p.identifier,
                            bigDecimal.toString(), p.identifier, false));
                    x.max.ifPresent(bigDecimal -> out.line("%s.checkMaximum(%s, \"%s\", \"%s\", %s);", RequestPreconditions.class, p.identifier,
                            bigDecimal.toString(), p.identifier, false));
                    x.minExclusive.ifPresent(bigDecimal -> out.line("%s.checkMinimum(%s, \"%s\", \"%s\", %s);", RequestPreconditions.class, p.identifier,
                            bigDecimal.toString(), p.identifier, true));
                    x.maxExclusive.ifPresent(bigDecimal -> out.line("%s.checkMaximum(%s, \"%s\", \"%s\", %s);", RequestPreconditions.class, p.identifier,
                            bigDecimal.toString(), p.identifier, true));
                    if (p.isArray && x.minItems.isPresent()) {
                        out.line("%s.checkMinSize(%s, %s, \"%s\");", RequestPreconditions.class, p.identifier,
                                x.minItems.get(), p.identifier);
                    }
                    if (p.isArray && x.maxItems.isPresent()) {
                        out.line("%s.checkMaxSize(%s, %s, \"%s\");", RequestPreconditions.class, p.identifier,
                                x.maxItems.get(), p.identifier);
                    }
                }
            });
            out.closeParen();
        }
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
