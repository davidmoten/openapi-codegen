package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.davidmoten.oa3.codegen.generator.internal.ByteArrayPrintWriter;
import org.davidmoten.oa3.codegen.generator.internal.Imports;
import org.davidmoten.oa3.codegen.generator.internal.Indent;
import org.davidmoten.oa3.codegen.generator.internal.Javadoc;

class WriterUtil {

    static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("debug", "false"));
    static final String IMPORTS_HERE = "IMPORTS_HERE";

    public static void closeParen(PrintWriter out, Indent indent) {
        indent.left();
        out.format("%s}\n", indent);
    }

    static void writeContent(Names names, ByteArrayPrintWriter out, String fullClassName, Imports imports) {
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

    public static String markdownToHtml(String description) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(description);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    static void writeApiJavadoc(PrintWriter out, Names names, Indent indent) {
        String text = Stream.of( //
                Optional.ofNullable(names.api().getInfo().getTitle()), //
                Optional.ofNullable(names.api().getInfo().getSummary()), //
                Optional.ofNullable(names.api().getInfo().getDescription())) //
                .filter(Optional::isPresent) //
                .map(Optional::get) //
                .collect(Collectors.joining("\n\n"));
        Javadoc.printJavadoc(out, indent, WriterUtil.markdownToHtml(text), true);
    }

}
