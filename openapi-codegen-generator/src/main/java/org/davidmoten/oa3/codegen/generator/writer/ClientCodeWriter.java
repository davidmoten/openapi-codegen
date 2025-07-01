package org.davidmoten.oa3.codegen.generator.writer;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.client.runtime.ClientBuilder;
import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.Method;
import org.davidmoten.oa3.codegen.generator.ClientServerGenerator.ResponseDescriptor;
import org.davidmoten.oa3.codegen.generator.Names;
import org.davidmoten.oa3.codegen.generator.Names.Server;
import org.davidmoten.oa3.codegen.generator.ParamType;
import org.davidmoten.oa3.codegen.generator.internal.CodePrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.WriterUtil;
import org.davidmoten.oa3.codegen.http.Http;
import org.davidmoten.oa3.codegen.http.Http.RequestBuilder;
import org.davidmoten.oa3.codegen.http.HttpMethod;
import org.davidmoten.oa3.codegen.http.Interceptor;
import org.davidmoten.oa3.codegen.http.MediaType;
import org.davidmoten.oa3.codegen.http.Serializer;
import org.davidmoten.oa3.codegen.http.service.HttpService;
import org.davidmoten.oa3.codegen.runtime.Preconditions;

import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Nonnull;

public class ClientCodeWriter {

    public static void writeClientClass(Names names, List<Method> methods) {
        String fullClassName = names.clientFullClassName();
        CodePrintWriter out = CodePrintWriter.create(fullClassName, names.simpleNameInPackage(fullClassName));
        writeClientClass(out, names, methods);
        WriterUtil.writeContent(names, out);
    }

    private static void writeClientClass(CodePrintWriter out, Names names, List<Method> methods) {
        out.line("package %s;", out.pkg());
        out.println();
        out.format("%s", WriterUtil.IMPORTS_HERE);
        WriterUtil.writeApiJavadoc(out, names);
        WriterUtil.addGeneratedAnnotation(out);
        out.line("public class %s {", out.simpleClassName());
        writeClientClassFieldsConstructorAndBuilder(out, names);
        writeClientUtilityMethods(out);
        writeClientClassMethods(out, methods);
        writeCustomMethod(out);
        out.closeParen();
    }

	private static void writeClientClassFieldsConstructorAndBuilder(CodePrintWriter out, Names names) {
        // add fields
        out.println();
        out.line("private final %s serializer;", Serializer.class);
        out.line("private final %s<%s> interceptors;", List.class, Interceptor.class);
        out.line("private final %s basePath;", String.class);
        out.line("private final %s httpService;", HttpService.class);

        // write constructor
        out.println();
        out.line("private %s(%s serializer, %s<%s> interceptors, %s basePath, %s httpService) {", out.simpleClassName(),
                Serializer.class, List.class, Interceptor.class, String.class, HttpService.class);
        out.line("this.serializer = serializer;");
        out.line("this.interceptors = interceptors;");
        out.line("this.basePath = basePath;");
        out.line("this.httpService = httpService;");
        out.closeParen();

        // write builder
        out.println();
        out.line("public static @%s %s<%s> basePath(@%s %s basePath) {", Nonnull.class, ClientBuilder.class, out.simpleClassName(), Nonnull.class,
                String.class);
        out.line("%s.checkNotNull(basePath, \"basePath\");", Preconditions.class);
        out.line("return new %s<>(b -> new %s(b.serializer(), b.interceptors(), b.basePath(), b.httpService()), %s.config(), basePath);",
                ClientBuilder.class, out.simpleClassName(), out.add(names.globalsFullClassName()));
        out.closeParen();
        
        List<Server> servers = names.servers();
        if (!servers.isEmpty()) {
            
            out.println();
            out.line("public static @%s %s<%s> basePath(@%s Server server) {", Nonnull.class, ClientBuilder.class, out.simpleClassName(), Nonnull.class);
            out.line("%s.checkNotNull(server, \"server\");", Preconditions.class);
            out.line("return new %s<>(b -> new %s(b.serializer(), b.interceptors(), b.basePath(), b.httpService()), %s.config(), server.url());",
                    ClientBuilder.class, out.simpleClassName(), out.add(names.globalsFullClassName()));
            out.closeParen();
            
            out.println();
            out.line("public enum Server {");
            out.println();
            Set<String> enumNames = new HashSet<>();
            for (int i = 0; i < servers.size(); i++) {
                Server server = servers.get(i);
                String fallbackName = "server" + (i + 1);
                String name = Names.enumNameToEnumConstant(server.description.orElse(fallbackName));
                if (enumNames.contains(name)) {
                    name = Names.enumNameToEnumConstant(fallbackName);
                }
                enumNames.add(name);
                final String delimiter = i == servers.size() - 1 ? ";": ",";
                out.line("%s(\"%s\")%s", name, server.url, delimiter);
            }
            out.println();
            out.line("private String url;");
            out.println();
            out.line("Server(String url) {");
            out.line("this.url = url;");
            out.closeParen();
            out.println();
            out.line("public String url() {");
            out.line("return url;");
            out.closeParen();
            out.closeParen();
        }
        
    }
	
    private static void writeClientUtilityMethods(CodePrintWriter out) {
    	out.println();
    	out.line("private %s http(%s method, %s path) {", Http.Builder.class, HttpMethod.class, String.class);
    	out.line("return %s", Http.class);
    	out.right();
    	out.right();
    	out.line(".method(method)");
    	out.line(".basePath(this.basePath)");
    	out.line(".path(path)");
    	out.line(".serializer(this.serializer)");
    	out.line(".interceptors(this.interceptors)");
    	out.line(".httpService(this.httpService);");
    	out.left();
    	out.left();
    	out.closeParen();
	}

    private static void writeClientClassMethods(CodePrintWriter out, List<Method> methods) {
        methods.forEach(m -> {
            out.right().right();
            String params = m.parameters //
                    .stream() //
                    .map(p -> String.format("\n%s@%s %s %s", out.indent(), out.add(Nonnull.class),
                            ServerCodeWriterSpringBoot.toImportedType(p, out.imports()), p.identifier)) //
                    .collect(Collectors.joining(", "));
            out.left().left();
            final String importedReturnType;
            if (!m.returnFullClassName.isPresent()) {
                importedReturnType = out.add(Void.class.getCanonicalName());
            } else {
                importedReturnType = out.add(m.returnFullClassName.get());
            }
            // we don't want the Resource type being used on the client side
            // TODO use actual type (usually wrapped binary)?
            boolean hasPrimaryResponse = m.primaryStatusCode.isPresent() && m.primaryMediaType.isPresent();
            ServerCodeWriterSpringBoot.writeMethodJavadoc(out, m,
                    Optional.of("call builder"), Maps.empty());
            out.line("public @%s %s<%s> %s(%s) {", Nonnull.class, RequestBuilder.class, importedReturnType, m.methodName, params);
            out.line("return http(%s.%s, \"%s\")", HttpMethod.class, m.httpMethod.name(), m.path);
            out.right().right();
            Set<String> used = new HashSet<>();
            for (ResponseDescriptor rd : m.responseDescriptors) {
                if (!used.contains(rd.mediaType())) {
                    if (MediaType.isJson(rd.mediaType())) {
                        out.line(".acceptApplicationJson()");
                    } else {
                        out.line(".accept(\"%s\")", rd.mediaType());
                    }
                    used.add(rd.mediaType());
                }
            }
            m.parameters.forEach(p -> {
                if (p.type == ParamType.QUERY) {
                    out.line(".queryParam(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.PATH) {
                    out.line(".pathParam(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.BODY) {
                    out.line(".body(%s)", p.identifier);
                    out.line(".contentTypeApplicationJson()");
                } else if (p.type == ParamType.COOKIE) {
                    out.line(".cookie(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.HEADER) {
                    out.line(".header(\"%s\", %s)", p.name, p.identifier);
                } else if (p.type == ParamType.MULTIPART_FORM_DATA) {
                    out.line(".multipartFormData(%s)", p.identifier);
                } else if (p.type == ParamType.FORM_URLENCODED) {
                    out.line(".formUrlEncoded(%s)", p.identifier);
                }
            });
            m.responseDescriptors.forEach(r -> {
                out.line(".responseAs(%s.class)", out.add(r.fullClassName()));
                out.line(".whenStatusCodeMatches(\"%s\")", r.statusCode());
                out.line(".whenContentTypeMatches(\"%s\")", r.mediaType());
            });
			if (hasPrimaryResponse) {
				out.line(".<%s>requestBuilder(\"%s\", \"%s\");", importedReturnType, m.primaryStatusCode.get(),
						m.primaryMediaType.get());
			} else {
				out.line(".<%s>requestBuilder();", importedReturnType);
			}
            out.left().left();
            out.closeParen();
        });
    }
    
    private static void writeCustomMethod(CodePrintWriter out) {
        out.println();
        out.line("public @%s %s _custom(%s method, %s path) {" , Nonnull.class, Http.Builder.class, HttpMethod.class, String.class);
        out.line("return %s", Http.class);
        out.right().right();
        out.line(".method(method)");
        out.line(".basePath(this.basePath)");
        out.line(".path(path)");
        out.line(".serializer(this.serializer)");
        out.line(".httpService(this.httpService);");
        out.left().left();
        out.closeParen();
    }

}
