package org.davidmoten.oa3.codegen.generator.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.davidmoten.oa3.codegen.generator.Generator;
import org.davidmoten.oa3.codegen.generator.Names;

import jakarta.annotation.Generated;

public final class WriterUtil {

    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("debug", "false"));
    public static final String IMPORTS_HERE = "IMPORTS_HERE";

    private static final String version = readVersion();

    private static String readVersion() {
        Properties p = new Properties();
        try (InputStream in = Generator.class.getResourceAsStream("/application.properties")) {
            p.load(in);
            return p.get("groupId") + ":" + p.get("artifactId") + p.get("version");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void closeParen(PrintWriter out, Indent indent) {
        indent.left();
        out.format("%s}\n", indent);
    }

    public static void writeContent(Names names, CodePrintWriter out) {
        String content = out.text().replace(IMPORTS_HERE, out.imports().toString());
        if (DEBUG) {
            System.out.println("////////////////////////////////////////////////");
            System.out.println(content);
        }
        out.close();
        File file = names.fullClassNameToJavaFile(out.fullClassName());
        file.getParentFile().mkdirs();
        try {
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String markdownToHtml(String description) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(description);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    public static void writeApiJavadoc(CodePrintWriter out, Names names) {
        String text = Stream.of( //
                Optional.ofNullable(names.api().getInfo().getTitle()), //
                Optional.ofNullable(names.api().getInfo().getSummary()), //
                Optional.ofNullable(names.api().getInfo().getDescription())) //
                .filter(Optional::isPresent) //
                .map(Optional::get) //
                .collect(Collectors.joining("\n\n"));
        if (!Javadoc.printJavadoc(out, out.indent(), WriterUtil.markdownToHtml(text), true)) {
            out.println();
        }
    }

    public static void addGeneratedAnnotation(CodePrintWriter out) {
        out.line("@%s(value = \"%s\")", Generated.class, version);
    }

    public static String escapePattern(String pattern) {
        return pattern.replace("\\", "\\\\");
    }

}
