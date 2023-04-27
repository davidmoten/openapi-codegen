package org.davidmoten.oa3.codegen.generator.internal;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.davidmoten.text.utils.WordWrap;

import com.github.davidmoten.guavamini.Preconditions;

public class Javadoc {

    private static final int MAX_JAVADOC_WIDTH = 80;

    public static void printJavadoc(PrintWriter out, Indent indent, String text, boolean isHtml) {
        Preconditions.checkNotNull(text);
        printJavadoc(out, indent, Optional.of(text), Collections.emptyList(), Optional.empty(), Optional.empty(),
                Collections.emptyMap(), isHtml);
    }

    public static void printJavadoc(PrintWriter p, Indent indent, Optional<String> text, List<Annotation> annotations,
            Optional<String> preamble, Optional<String> returns, Map<String, String> parameterDoc, boolean isHtml) {
        boolean hasText = text.isPresent() || !annotations.isEmpty() || returns.isPresent();
        boolean addParagraph = false;
        if (hasText) {
            p.format("\n%s/**\n", indent);
            if (preamble.isPresent()) {
                p.format("%s * %s\n", indent, encodeAndWrapForJavadoc(preamble.get(), indent, false));
                addParagraph = true;
            }
        }
        if (text.isPresent()) {
            if (addParagraph) {
                p.format("%s * <p>\n", indent);
            }
            p.format("%s * %s\n", indent, encodeAndWrapForJavadoc(text.get(), indent, isHtml));
        }
        annotations.forEach(a -> {
            p.format("%s * <p>\n", indent);
            p.format("%s * <b>%s</b>\n", indent, a.getTerm());
            if (a.getString().isPresent()) {
                p.format("%s * <p>\n", indent);
                p.format("%s * %s\n", indent, a.getString().get());
            }
            if (a.getBool().isPresent()) {
                p.format("%s * <p>\n", indent);
                p.format("%s * %s\n", indent, a.getBool().get());
            }
            for (String record : a.getRecords()) {
                p.format("%s * <p>\n", indent);
                p.format("%s * %s\n", indent, record);
            }
        });
        if (hasText) {
            boolean first = true;
            for (Entry<String, String> entry : parameterDoc.entrySet()) {
                if (first) {
                    p.format("%s * \n", indent);
                    first = false;
                }
                p.format("%s * @param %s\n", indent, entry.getKey());
                p.format("%s *            %s\n", indent, entry.getValue());
            }
            if (returns.isPresent()) {
                if (first) {
                    p.format("%s * \n", indent);
                }
                p.format("%s * @return %s\n", indent, returns.get());
            }
            p.format("%s */", indent);
        }
    }

    private static String encodeAndWrapForJavadoc(String s, Indent indent, boolean isHtml) {
        s = s.replace("{@", "zxxz");
        if (!isHtml) {
            s = wrap(s);
        }
        return encodeJavadoc(s, isHtml) //
                .replace("\n", String.format("\n%s * ", indent)) //
                .replace("zxxz", "{@");
    }

    private static String encodeJavadoc(String x, boolean isHtml) {
        x = x.replace("@", "&#064;") //
                .replace("\\", "{@literal \\}");
//                .replace("&", "&amp;");
        if (!isHtml) {
            x = x.replace("<", "&lt;") //
                    .replace(">", "&gt;");
        }
        return x;
    }

    private static String wrap(String s) {
        return WordWrap //
                .from(s) //
                .breakWords(false) //
                .extraWordChars("0123456789") //
                .maxWidth(MAX_JAVADOC_WIDTH) //
                .newLine("\n") //
                .wrap() //
                .trim();
    }

}
